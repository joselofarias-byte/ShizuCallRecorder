/*
 * ShizuCallRecorder: FOSS Call recording powered through ADB/Shizuku!
 *  Copyright (C) 2026-present kitsumed (Med)
 *  This software is licensed under the GNU General Public License v3 or later, with additional terms as permitted under Section 7.
 *  The full license text is available in the LICENSE file at the root of this project.
 *  This software is distributed WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.kitsumed.shizucallrecorder.services.shell

import com.kitsumed.shizucallrecorder.integrations.scrcpy.ScrcpyAudioCodec
import com.kitsumed.shizucallrecorder.integrations.scrcpy.ScrcpyAudioSource
import com.kitsumed.shizucallrecorder.integrations.scrcpy.ScrcpyConfig
import com.kitsumed.shizucallrecorder.integrations.scrcpy.ServerExtractor
import com.kitsumed.shizucallrecorder.utils.AppLogger
import java.io.File

/**
 * Encapsulates the privileged shell commands required to start the scrcpy-server child process.
 *
 * Runs inside the shell process (UID 2000 or 0) managed by Shizuku.
 * Has no dependency on Android Service or UI components — it is purely responsible for
 * verifying the server binary and constructing/launching the app_process command.
 *
 * Shizuku loads this class in the same process as [ShellService]; keeping it [internal]
 * prevents accidental use from the app-side process.
 */
internal object ShellCommandExecutor {

    private const val TAG = "SCR:ShellCommandExecutor"

    /**
     * Verifies that the scrcpy-server JAR at [serverPath] exists and that its SHA-256 digest
     * matches the expected value bundled in [ScrcpyConfig].
     *
     * This check runs inside the shell process to reduce TOCTOU exposure, though it is not a
     * perfect guarantee.
     *
     * @param serverPath Absolute path to scrcpy-server.jar on shared storage.
     * @return `true` if the file exists and the hash matches; `false` otherwise.
     */
    fun verifyServerJar(serverPath: String): Boolean {
        val file = File(serverPath)
        return file.exists() && ServerExtractor.verifyServerHash(file)
    }

    /**
     * Converts the raw AIDL parameters into type-safe enums, builds the app_process argument
     * list, and starts the scrcpy-server child process.
     *
     * The resulting [Process] has its stderr merged into stdout so a single log-consumer
     * coroutine can drain both streams.
     *
     * @param serverPath   Absolute path to scrcpy-server.jar; used as the CLASSPATH environment variable.
     * @param socketName   8-hex-digit abstract socket name that scrcpy-server will connect back to.
     * @param audioSource  Raw audio-source key received over AIDL (e.g. "mic-voice-communication").
     * @param audioCodec   Raw audio-codec key received over AIDL (e.g. "opus", "aac").
     * @param audioBitRate Bit rate in bps; values <= 0 are omitted and the codec default is used.
     * @return The running [Process].
     * @throws Exception if the process cannot be started (propagated to [ShellService.startRecording]).
     */
    fun launchScrcpyServer(
        serverPath: String,
        socketName: String,
        audioSource: String,
        audioCodec: String,
        audioBitRate: Int
    ): Process {
        val audioSourceEnum = ScrcpyAudioSource.fromKey(audioSource)
        val audioCodecEnum  = ScrcpyAudioCodec.fromKey(audioCodec)
        val serverArgs      = ScrcpyConfig.buildServerArgs(socketName, audioSourceEnum, audioCodecEnum, audioBitRate)

        val launchCommand = mutableListOf("app_process", "/", ScrcpyConfig.SERVER_MAIN_CLASS)
        launchCommand.addAll(serverArgs)

        AppLogger.d(TAG, "Launching scrcpy-server with command: ${launchCommand.joinToString(" ")}")

        return ProcessBuilder(launchCommand).apply {
            // CLASSPATH tells app_process where to find the server binary file.
            environment()["CLASSPATH"] = serverPath
            // Merge stderr into stdout so the log-consumer coroutine only needs one stream.
            redirectErrorStream(true)
        }.start()
    }
}
