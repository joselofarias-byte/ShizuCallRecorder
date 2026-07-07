/*
 * ShizuCallRecorder: FOSS Call recording powered through ADB/Shizuku!
 *  Copyright (C) 2026-present kitsumed (Med)
 *  This software is licensed under the GNU General Public License v3 or later, with additional terms as permitted under Section 7.
 *  The full license text is available in the LICENSE file at the root of this project.
 *  This software is distributed WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.kitsumed.shizucallrecorder.services.shell

import android.content.Context
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.annotation.Keep
import com.kitsumed.shizucallrecorder.ILogCallback
import com.kitsumed.shizucallrecorder.IShellService
import com.kitsumed.shizucallrecorder.integrations.scrcpy.ScrcpyConfig
import com.kitsumed.shizucallrecorder.utils.AppLogger
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess

/**
 * ShellService runs inside the privileged shell process (UID 2000 or 0) managed by Shizuku.
 *
 * By running under the app shell as ADB or root via Shizuku, we can
 * launch scrcpy-server with app_process and capture audio that a normal app cannot access.
 *
 * AI generated Overview:
 *
 *   ┌─────────────────────────────────────────────────────────────┐
 *   │  Shell Process (UID 2000 or 0)                              │
 *   │                                                             │
 *   │  ShellService (this class, AIDL stub)                       │
 *   │    │                                                        │
 *   │    ├── launches  scrcpy-server (app_process)                │
 *   │    │     └── connects to  LocalServerSocket                 │
 *   │    │                          │                             │
 *   │    ├── AudioRelayCoroutine ◄──┘  (socket → pipe)            │
 *   │    │         │                                              │
 *   │    │     Pipe[1] write-end (kept in Shell)                  │
 *   │    │     Pipe[0] read-end  ───────────────► App Process     │
 *   │    │                                          ScrcpyClient  │
 *   │    ├── LogConsumerCoroutine   (drain stdout)                │
 *   │    └── ProcessMonitorCoroutine (wait for exit)              │
 *   └─────────────────────────────────────────────────────────────┘
 *
 * Shizuku requirements:
 *  • Must have a no-arg constructor AND a single-Context constructor (Shizuku v13+).
 *  • Must be annotated with [@Keep] so ProGuard/R8 does not remove/rename the class.
 *  • [destroy] must call [exitProcess] to terminate the shell process when Shizuku asks.
 */
@Keep
class ShellService : IShellService.Stub {

    private companion object {
        const val TAG = "SCR:ShellService"
    }

    /**
     * Atomic flag that controls concurrent-session guards and is read by [ShellAudioPipeline]
     * coroutines to classify I/O errors.
     *
     * Why [AtomicBoolean]? [stopRecording] can be called from any thread (e.g. the AIDL
     * thread pool) while the relay coroutine runs on Dispatchers.IO. AtomicBoolean provides
     * a lock-free compare-and-set that is visible across threads.
     */
    private val isRecordingActive = AtomicBoolean(false)

    /**
     * The audio I/O pipeline for the current recording session.
     * Null when no session is active.
     */
    private var pipeline: ShellAudioPipeline? = null

    // ---- Shizuku-required constructors

    /**
     * No-arg constructor required by older versions of Shizuku.
     */
    @Keep constructor() : this(null)

    /**
     * Context constructor required by Shizuku v13+ for user-service instantiation.
     * The context is the shell process's context (not the app's).
     *
     * @param context The shell-process [Context] provided by Shizuku, or null on older versions.
     */
    @Keep constructor(context: Context?) {
        Log.i(TAG,"===============================\n" +
             "ShellService process started!\n" +
             "Running as UID=(${android.os.Process.myUid()})\n" +
             "===============================")
    }

    // -------- IShellService AIDL implementation

    /**
     * Starts the audio-capture pipeline.  Called from the app process via Binder IPC.
     *
     * Steps performed in this method (all in the shell process):
     *  1. Guard: reject if already recording.
     *  2. Verify scrcpy-server JAR hash
     *  3. Create a kernel pipe; keep the write-end here, return the read-end to the app.
     *  4. Open a [LocalServerSocket] and start an audio-relay coroutine that calls accept().
     *  5. Build the `app_process` launch command with scrcpy arguments.
     *  6. Start the scrcpy-server child process.
     *  7. Start log-consumer and process-monitor coroutines as background helpers.
     *
     * @param audioSource        scrcpy audio_source parameter (e.g. "mic-voice-communication").
     * @param audioCodec         scrcpy audio_codec parameter (e.g. "opus", "aac").
     * @param audioBitRate       scrcpy audio_bit_rate in bps (e.g. 16000 for 16 kbps Opus).
     * @param serverPath      Absolute path to scrcpy-server.jar in shared storage.
     * @param enableVerboseLogging  When true, logs relay throughput every second.
     * @return The read-end [ParcelFileDescriptor] of the audio pipe, or null on failure.
     */
    override fun startRecording(
        audioSource: String,
        audioCodec: String,
        audioBitRate: Int,
        serverPath: String,
        isDebuggingModeEnabled: Boolean,
        listener: ILogCallback
    ): ParcelFileDescriptor? {
        AppLogger.initAsRemote(listener, isDebuggingModeEnabled)

        if (isRecordingActive.get()) {
            AppLogger.w(TAG, "startRecording() rejected: a session is already active")
            return null
        }

        try {
            AppLogger.i(TAG, "Initialising the ShellService recording pipeline...")

            // 1. Security check: verify the JAR's SHA-256 before exec.
            if (!ShellCommandExecutor.verifyServerJar(serverPath)) {
                AppLogger.w(TAG, "Server JAR absent or SHA-256 mismatch at $serverPath - aborting")
                return null
            }

            // 2. Generate socket name shared by the pipeline (socket creation) and
            //    the executor (server argument). Generated here so both sides use the same name.
            val socketName = ScrcpyConfig.getRandomSocketName()

            // 3. Launch scrcpy-server process.
            val process = ShellCommandExecutor.launchScrcpyServer(
                serverPath, socketName, audioSource, audioCodec, audioBitRate
            )

            try {
                // 4. Create and start the audio pipeline. The pipeline creates the pipe and socket,
                //    spawns relay/log/monitor coroutines, and returns the pipe read-end.
                val activePipeline = ShellAudioPipeline()
                pipeline = activePipeline
                val pipeReadEnd = activePipeline.start(
                    process      = process,
                    socketName   = socketName,
                    verbose      = isDebuggingModeEnabled,
                    isRecordingActive = { isRecordingActive.get() },
                    onPipelineEnded   = { stopRecording() }
                )
                isRecordingActive.set(true)
                AppLogger.i(TAG, "Recording pipeline established. Returning pipe read-end to app process.")
                return pipeReadEnd
            } catch (e: Exception) {
                runCatching { process.destroy() }
                throw e
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Critical failure during pipeline startup: ${e.message}", e)
            stopRecording() // Best-effort cleanup of any partially-allocated resources.
            return null
        }
    }

    /**
     * Stops the recording pipeline and releases all resources.  Called from the app process
     * via Binder IPC, and also from internal coroutines when an error is detected.
     *
     * Cleanup order matters:
     *  1. Set the atomic flag to false first so the relay loop exits on its next iteration.
     *  2. Destroy scrcpy-server; give it a grace period to flush its last audio bytes.
     *  3. Cancel the coroutine scope (interrupts blocking I/O calls in relay/log/monitor).
     *  4. Close the client connection so the relay coroutine's read() unblocks.
     *  5. Close the server socket.
     *  6. Close the pipe write-end LAST so scrcpy-server can write its final bytes first.
     */
    override fun stopRecording() {
        // compareAndSet(true, false): atomically checks that we ARE recording and clears the flag.
        if (isRecordingActive.compareAndSet(true, false)) {
            AppLogger.i(TAG, "Stopping active recording session...")
        } else {
            AppLogger.d(TAG, "stopRecording() called: ensuring all background resources are released...")
        }

        // Delegate all audio resource cleanup to the pipeline.
        pipeline?.stop()
        pipeline = null

        AppLogger.i(TAG, "ShellService resource cleanup complete.")
    }

    /** Returns whether a recording session is currently active (thread-safe via AtomicBoolean). */
    override fun isRecording(): Boolean = isRecordingActive.get()

    override fun grantAppOps(packageName: String, opName: String, userProfileId: Int): Boolean {
        try {
            AppLogger.i(TAG, "Executing AppOps set --user $userProfileId $packageName $opName allow")
            val process = ProcessBuilder("appops", "set", "--user", userProfileId.toString(), packageName, opName, "allow").start()
            val errorOutput = process.errorStream.bufferedReader().readText().trim()
            val inputOutput = process.inputStream.bufferedReader().readText().trim()
            val exitCode = process.waitFor()
            AppLogger.i(TAG, "grantAppOps completed with exit code $exitCode. Output: ${inputOutput.ifBlank { "Empty" }}, Error: ${errorOutput.ifBlank { "Empty" }}")
            // We return false if the exit code is non-zero or if there was any error output, indicating that the operation failed.
            return (exitCode == 0 && errorOutput.isBlank())
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error granting AppOps $opName to $packageName: ${e.message}", e)
        }
        return false
    }

    /**
     * Called by Shizuku when it wants to shut down this user service.
     * MUST call [exitProcess] so the entire shell process is terminated; otherwise Shizuku may
     * be unable to clean up the process, and it will linger in memory.
     */
    override fun destroy() {
        AppLogger.i(TAG,"ShellService.destroy() – terminating shell process")
        stopRecording()
        exitProcess(0)
    }

    // grantAppOps and destroy are unchanged — they are independent shell commands.
}
