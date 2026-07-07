/*
 * ShizuCallRecorder: FOSS Call recording powered through ADB/Shizuku!
 *  Copyright (C) 2026-present kitsumed (Med)
 *  This software is licensed under the GNU General Public License v3 or later, with additional terms as permitted under Section 7.
 *  The full license text is available in the LICENSE file at the root of this project.
 *  This software is distributed WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.kitsumed.shizucallrecorder.services.shell

import android.net.LocalServerSocket
import android.net.LocalSocket
import android.os.ParcelFileDescriptor
import com.kitsumed.shizucallrecorder.integrations.scrcpy.ScrcpyConfig
import com.kitsumed.shizucallrecorder.utils.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InterruptedIOException
import java.util.concurrent.TimeUnit

/**
 * Manages the audio I/O pipeline that bridges scrcpy-server's Unix-domain socket output
 * to the kernel pipe consumed by the app process.
 *
 * **Lifecycle**: one instance per recording session. Create a new instance for each call to
 * [ShellService.startRecording]; discard it after [stop] returns.
 *
 * Responsibilities:
 *  - Creates the kernel pipe ([ParcelFileDescriptor.createPipe]) and the [LocalServerSocket].
 *  - Spawns three background coroutines:
 *      - **AudioRelayCoroutine**: accepts the scrcpy-server socket connection, then copies
 *        bytes from the socket input stream into the pipe write-end.
 *      - **LogConsumerCoroutine**: drains scrcpy-server's merged stdout/stderr so the child
 *        process is never blocked on its output buffer.
 *      - **ProcessMonitorCoroutine**: watches for scrcpy-server exit and triggers cleanup on
 *        unexpected crashes.
 *  - Shuts everything down in the correct order via [stop].
 *
 * This class has **no dependency** on [ShellService] or Android Service classes. Interaction
 * with [ShellService] happens exclusively through the two lambdas passed to [start]:
 *  - [onPipelineEnded] — called when the relay or monitor coroutine detects that the pipeline
 *    has ended (e.g. scrcpy-server crash or EOF), so [ShellService] can finalize the session.
 *  - [isRecordingActive] — read by the relay coroutine to distinguish expected I/O errors
 *    (during a planned shutdown) from unexpected ones.
 */
internal class ShellAudioPipeline {

    private companion object {
        const val TAG = "SCR:ShellAudioPipeline"

        /**
         * Size of the byte buffer used when copying data from the socket to the pipe.
         * 32 KB is a balance between latency (larger = more delay per flush) and syscall
         * overhead (smaller = more read/write pairs per second). At 16 kbps Opus the server
         * produces ≈ 2 KB/s, so 32 KB means ≈ 16 s of audio per buffer; this is fine since
         * the relay loop flushes on every read().
         */
        const val RELAY_BUFFER_SIZE = 32 * 1024

        /**
         * How long to wait for scrcpy-server to finish writing its final bytes after we call
         * [Process.destroy]. Giving it a short grace period avoids truncating the last audio
         * frame if the server is encoding when the stop request arrives.
         */
        const val PROCESS_STOP_GRACE_PERIOD_SEC = 2L
    }

    // -- Session-scoped resources (all null when not active)

    /** The running scrcpy-server child process. */
    private var scrcpyProcess: Process? = null

    /** The Unix-domain server socket that waits for scrcpy-server to connect. */
    private var serverSocket: LocalServerSocket? = null

    /** The accepted connection from scrcpy-server after it dials our server socket. */
    private var clientConnection: LocalSocket? = null

    /**
     * Write end of the kernel pipe. The relay coroutine copies bytes from the scrcpy-server
     * socket into this end; the app process holds the read end wrapped in a [ParcelFileDescriptor].
     *
     * **IMPORTANT**: Do NOT close this before the scrcpy-server process exits. The server may be
     * buffering its final audio frame and will write it after receiving SIGTERM. Closing the
     * write end early would cause a broken-pipe error in the relay coroutine and truncate the
     * recording.
     */
    private var audioWriteEnd: ParcelFileDescriptor? = null

    /** Coroutine scope for all background work in this recording session. */
    private var shellScope: CoroutineScope? = null

    /**
     * The relay job that copies bytes from the socket to the pipe for the downstream app.
     * Kept so [stop] can wait for late bytes to be relayed before closing the pipe write-end.
     */
    @Volatile
    private var audioPipeRelayJob: Job? = null

    // -------------------------------------------------------------------------

    /**
     * Initialises the audio pipeline for a new recording session.
     *
     * Steps:
     *  1. Creates the kernel pipe.
     *  2. Opens the [LocalServerSocket] using the provided [socketName] (with the scrcpy prefix).
     *  3. Creates the coroutine scope.
     *  4. Spawns the relay coroutine *before* the server process connects so [LocalServerSocket.accept]
     *     is already waiting when scrcpy-server dials in.
     *  5. Spawns the log-consumer and process-monitor coroutines.
     *
     * The [socketName] must be the **bare** 8-hex-digit name (without the scrcpy prefix) because
     * [ShellCommandExecutor] also needs it. [start] appends [ScrcpyConfig.SERVER_SOCKET_NAME_PREFIX]
     * internally when creating the [LocalServerSocket].
     *
     * @param process            The already-started scrcpy-server [Process].
     * @param socketName         8-hex-digit socket name (without prefix) passed to both
     *                           [ShellCommandExecutor] and this pipeline.
     * @param verbose            When true, logs relay throughput roughly every second.
     * @param isRecordingActive  Lambda returning the current recording-active flag; used by the
     *                           relay coroutine to classify I/O errors as expected or unexpected.
     * @param onPipelineEnded    Invoked by relay/monitor coroutines when the pipeline terminates
     *                           (EOF, crash, or scope cancellation). Should call
     *                           [ShellService.stopRecording] to finalize the session.
     * @return The read-end [ParcelFileDescriptor] to return to the app process via Binder IPC.
     * @throws Exception if socket or pipe creation fails; caller should invoke [stop] for cleanup.
     */
    fun start(
        process: Process,
        socketName: String,
        verbose: Boolean,
        isRecordingActive: () -> Boolean,
        onPipelineEnded: () -> Unit
    ): ParcelFileDescriptor {
        scrcpyProcess = process

        // 1. Create kernel pipe: keep write-end here, return read-end to ShellService → app process.
        val pipe = ParcelFileDescriptor.createPipe()
        val pipeReadEnd  = pipe[0] // → returned to app process via Binder
        val pipeWriteEnd = pipe[1] // → written by relay coroutine
        audioWriteEnd = pipeWriteEnd

        // 2. Create Unix-domain socket server. scrcpy-server will dial this after launch.
        // The prefix is added here; the bare socketName is shared with ShellCommandExecutor.
        val serverFullSocketName = ScrcpyConfig.SERVER_SOCKET_NAME_PREFIX + socketName
        serverSocket = LocalServerSocket(serverFullSocketName)
        AppLogger.d(TAG, "Listening on abstract socket '$serverFullSocketName'")

        // 3. Create coroutine scope for all session work.
        shellScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        // 4. Start relay BEFORE scrcpy-server connects so accept() is already waiting.
        spawnAudioRelayCoroutine(verbose, isRecordingActive, onPipelineEnded)

        // 5. Start helper coroutines (log consumer + process monitor).
        spawnLogConsumerCoroutine(process)
        spawnProcessMonitorCoroutine(process, isRecordingActive, onPipelineEnded)

        AppLogger.i(TAG, "Audio pipeline started. Returning pipe read-end to ShellService.")
        return pipeReadEnd
    }

    /**
     * Stops the pipeline and releases all session resources in the correct order.
     *
     * Cleanup order (must not change — see inline comments for why):
     *  1. Destroy scrcpy-server process; give it a grace period to flush final audio bytes.
     *  2. Wait for [audioPipeRelayJob] to drain the last bytes from the socket into the pipe.
     *  3. Cancel [shellScope] to interrupt any remaining blocking I/O (accept, readLine).
     *  4. Close [clientConnection] so the relay coroutine's read() unblocks if still running.
     *  5. Close [serverSocket].
     *  6. Close [audioWriteEnd] **last** — the write-end signals EOF to the app-side muxer;
     *     closing it before the relay finishes would truncate the recording.
     *  7. Null all references so the GC can collect them and the instance signals "idle".
     */
    fun stop() {
        AppLogger.d(TAG, "Stopping audio pipeline and releasing resources...")

        // 1. Destroy scrcpy-server and wait for it to flush final bytes.
        runCatching {
            scrcpyProcess?.let { process ->
                process.destroy()
                // Give the server up to PROCESS_STOP_GRACE_PERIOD_SEC to send its final audio bytes.
                process.waitFor(PROCESS_STOP_GRACE_PERIOD_SEC, TimeUnit.SECONDS)
            }
        }

        // 2. Wait for the relay job to copy remaining socket data to the pipe (time-bounded).
        if (audioPipeRelayJob?.isActive == true) {
            AppLogger.d(TAG, "Waiting for relay coroutine to finish copying late bytes...")
            runCatching {
                kotlinx.coroutines.runBlocking {
                    kotlinx.coroutines.withTimeoutOrNull(2000L) {
                        audioPipeRelayJob?.join()
                    }
                }
            }
        }

        // 3. Cancel the coroutine scope to interrupt blocking I/O in any remaining coroutines.
        AppLogger.d(TAG, "Cancelling shell coroutine scope...")
        runCatching { shellScope?.cancel() }

        // 4–6. Close connections in reverse allocation order.
        AppLogger.d(TAG, "Closing sockets and pipes...")
        runCatching { clientConnection?.close() }
        runCatching { serverSocket?.close() }
        runCatching { audioWriteEnd?.close() }  // closes write-end → EOF to app-side muxer

        // 7. Null references to signal idle state and allow GC.
        scrcpyProcess = null
        clientConnection = null
        serverSocket = null
        audioWriteEnd = null
        shellScope = null
        audioPipeRelayJob = null

        AppLogger.i(TAG, "Audio pipeline resource cleanup complete.")
    }

    // -------------------------------------------------------------------------
    // Private coroutine helpers

    /**
     * Launches a coroutine that:
     *  1. Calls [LocalServerSocket.accept] to wait for scrcpy-server to connect.
     *  2. Copies all bytes from the socket input stream into the pipe write-end.
     *  3. Invokes [onPipelineEnded] when the loop exits so [ShellService] finalizes the session.
     *
     * @param verbose            When true, logs relay throughput roughly every second.
     * @param isRecordingActive  Lambda returning the current recording-active flag.
     * @param onPipelineEnded    Callback invoked when relay exits (EOF, crash, or cancellation).
     */
    private fun spawnAudioRelayCoroutine(
        verbose: Boolean,
        isRecordingActive: () -> Boolean,
        onPipelineEnded: () -> Unit
    ) {
        audioPipeRelayJob = shellScope?.launch(Dispatchers.IO) {
            try {
                // accept() blocks until scrcpy-server dials our socket.
                // This is safe on Dispatchers.IO because IO threads are designed for blocking calls.
                AppLogger.d(TAG, "AudioRelayCoroutine: waiting for scrcpy-server connection...")
                val connection = serverSocket?.accept() ?: run {
                    AppLogger.w(TAG, "AudioRelayCoroutine: server socket was null or closed")
                    return@launch
                }
                clientConnection = connection
                AppLogger.i(TAG, "AudioRelayCoroutine: scrcpy-server connected to our socket server")

                val sourceStream = connection.inputStream
                // AutoCloseOutputStream closes the underlying ParcelFileDescriptor on close(),
                // which will make the read-end pipe report EOF to the downstream app.
                // We do NOT use .use{} here because audioWriteEnd is shared — it is closed
                // explicitly in stop() AFTER the scrcpy-server process exits.
                val destinationStream = ParcelFileDescriptor.AutoCloseOutputStream(audioWriteEnd)

                val buffer = ByteArray(RELAY_BUFFER_SIZE)
                var lastLogTimeMs = System.currentTimeMillis()

                // We read while isActive. We don't check isRecordingActive() here since
                // we want to keep reading late bytes from scrcpy until EOF is reached or
                // the scope is cancelled.
                while (isActive) {
                    val bytesRead = sourceStream.read(buffer)
                    if (bytesRead == -1) {
                        AppLogger.d(TAG, "AudioRelayCoroutine: socket EOF - scrcpy-server disconnected")
                        break
                    }
                    destinationStream.write(buffer, 0, bytesRead)

                    // Verbose throughput logging (≈ once per second) to aid debugging.
                    if (verbose && bytesRead > 0) {
                        val now = System.currentTimeMillis()
                        if (now - lastLogTimeMs >= 1000) {
                            lastLogTimeMs = now
                            AppLogger.v(TAG, "AudioRelayCoroutine: relayed $bytesRead bytes. (Wrote to pipe).")
                        }
                    }
                }
            } catch (e: IOException) {
                // IOException here is expected when:
                //  a) stop() closes the socket mid-read (produces a "Socket closed" error)
                //  b) scrcpy-server crashes and the socket is reset by peer
                if (isRecordingActive()) {
                    AppLogger.e(TAG, "AudioRelayCoroutine: unexpected I/O error: ${e.message}", e)
                } else {
                    AppLogger.d(TAG, "AudioRelayCoroutine: I/O error during shutdown (expected): ${e.message}")
                }
            } finally {
                // If we exit the relay (e.g. server crash or EOF), notify ShellService so the
                // app-side muxer can finalise the recording file.
                AppLogger.d(TAG, "AudioRelayCoroutine finished")
                onPipelineEnded()
            }
        }
    }

    /**
     * Launches a daemon coroutine that drains scrcpy-server's stdout/stderr.
     *
     * This prevents the child process from blocking when its output buffer fills up.
     *
     * @param process The running scrcpy-server [Process] whose output stream to consume.
     */
    private fun spawnLogConsumerCoroutine(process: Process) {
        shellScope?.launch(Dispatchers.IO) {
            try {
                process.inputStream.bufferedReader().use { reader ->
                    // Read one line at a time until EOF or the coroutine is cancelled.
                    var line = reader.readLine()
                    while (isActive && line != null) {
                        AppLogger.i(TAG, "[scrcpy-server] $line")
                        line = reader.readLine()
                    }
                }
            } catch (_: InterruptedIOException) {
                // Normal shutdown path: the scope was cancelled and the stream was closed.
                AppLogger.d(TAG, "LogConsumerCoroutine: interrupted (expected during shutdown)")
            } catch (e: IOException) {
                AppLogger.e(TAG, "LogConsumerCoroutine: I/O error: ${e.message}", e)
            } finally {
                AppLogger.d(TAG, "LogConsumerCoroutine finished")
            }
        }
    }

    /**
     * Launches a daemon coroutine that waits for scrcpy-server to exit.
     *
     * If scrcpy-server exits with a non-zero code while recording is still active, it crashed
     * unexpectedly. [onPipelineEnded] is invoked so the muxer can finalise the file with
     * whatever audio was already captured.
     *
     * @param process            The running scrcpy-server [Process] to monitor.
     * @param isRecordingActive  Lambda returning the current recording-active flag.
     * @param onPipelineEnded    Callback invoked on unexpected crash.
     */
    private fun spawnProcessMonitorCoroutine(
        process: Process,
        isRecordingActive: () -> Boolean,
        onPipelineEnded: () -> Unit
    ) {
        shellScope?.launch(Dispatchers.IO) {
            try {
                // waitFor() blocks until the child process exits.
                val exitCode = process.waitFor()
                if (exitCode != 0 && isRecordingActive()) {
                    AppLogger.e(TAG, "ProcessMonitorCoroutine: scrcpy-server crashed (exit code $exitCode)")
                    onPipelineEnded() // Trigger cleanup and file finalisation.
                } else {
                    AppLogger.i(TAG, "ProcessMonitorCoroutine: scrcpy-server exited normally (code $exitCode)")
                }
            } catch (_: InterruptedException) {
                // Normal: the scope was cancelled (service stopped) before the process exited.
                AppLogger.d(TAG, "ProcessMonitorCoroutine: interrupted (expected during shutdown)")
            } finally {
                AppLogger.d(TAG, "ProcessMonitorCoroutine finished")
            }
        }
    }
}
