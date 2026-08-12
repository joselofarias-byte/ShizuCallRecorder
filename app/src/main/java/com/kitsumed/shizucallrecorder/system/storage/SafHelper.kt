/*
 * ShizuCallRecorder: FOSS Call recording powered through ADB/Shizuku!
 *  Copyright (C) 2026-present kitsumed (Med)
 *  This software is licensed under the GNU General Public License v3 or later, with additional terms as permitted under Section 7.
 *  The full license text is available in the LICENSE file at the root of this project.
 *  This software is distributed WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.kitsumed.shizucallrecorder.system.storage

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.documentfile.provider.DocumentFile
import com.kitsumed.shizucallrecorder.system.storage.SafHelper.createAudioFile
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * SafHelper provides utility functions for working with the Android Storage Access Framework (SAF).
 */
object SafHelper {

    /**
     * Holds the result of a successful [createAudioFile] call.
     *
     * @param uri         The content URI of the newly created file (e.g. content://...).
     * @param descriptor  An open [ParcelFileDescriptor] in read-write mode.
     *                    Must be closed after use (after [ScrcpyAudioMuxer] finalises the container).
     * @param displayName A human-readable path for logging (e.g. "Recordings/2026/call_incoming.mp3").
     */
    data class SafResult(
        val uri: Uri,
        val descriptor: ParcelFileDescriptor,
        val displayName: String
    )

    /**
     * Creates a new audio file inside the user-chosen SAF folder, creating any requested subfolders.
     *
     * @param context    App context used to resolve the [DocumentFile] and open the FD.
     * @param folderUri  The tree URI of the destination folder (from the document-tree picker).
     * @param filePath   The desired relative file path including extension (e.g. "2026/08/call.mp3").
     * @param mimeType   The MIME type of the file (e.g. "audio/webm" for Opus, "audio/mp4" for AAC).
     * @return A [SafResult] with the URI, open FD, and display name; or null on failure/invalid path.
     */
    fun createAudioFile(context: Context, folderUri: Uri, filePath: String, mimeType: String): SafResult? {
        val rootDir = DocumentFile.fromTreeUri(context, folderUri) ?: return null
        if (!rootDir.exists() || !rootDir.isDirectory || !rootDir.canWrite()) return null

        val pathSegments = filePath
            .split('/')
            .filter { it.isNotBlank() }

        if (pathSegments.isEmpty()) return null
        if (pathSegments.any { it == "." || it == ".." }) return null

        val fileName = pathSegments.last()
        if (fileName.isBlank()) return null

        var currentDir = rootDir
        for (dirName in pathSegments.dropLast(1)) {
            val existing = currentDir.findFile(dirName)
            currentDir = when {
                existing == null -> currentDir.createDirectory(dirName) ?: return null
                existing.isDirectory -> existing
                else -> return null
            }
            if (!currentDir.canWrite()) return null
        }

        val newFile = currentDir.createFile(mimeType, fileName) ?: return null
        // Open the file in read-write mode so MediaMuxer can seek back to write headers.
        val fileDescriptor = context.contentResolver.openFileDescriptor(newFile.uri, "rw") ?: return null
        val normalizedPath = pathSegments.joinToString("/")
        val displayName = listOfNotNull(rootDir.name, normalizedPath).joinToString("/")
        return SafResult(newFile.uri, fileDescriptor, displayName)
    }

    /**
     * Returns true if [folderUri] points to an existing, writable SAF folder.
     * Used to validate the user's chosen recording folder before starting a session.
     *
     * @param context   App context used to resolve the [DocumentFile].
     * @param folderUri The tree URI to validate, or null.
     * @return true if the folder exists and is writable; false if null or inaccessible.
     */
    @OptIn(ExperimentalContracts::class)
    fun isFolderValid(context: Context, folderUri: Uri?): Boolean {
        // Tells the compiler: if we returns true, folderUri is not null. Prevent false compiler error and warnings.
        contract {
            returns(true) implies (folderUri != null)
        }
        if (folderUri == null) return false
        val directory = DocumentFile.fromTreeUri(context, folderUri)
        return directory != null && directory.exists() && directory.canWrite()
    }

    /**
     * Returns a human-readable display name for a SAF folder URI.
     * Used in the Settings screen to show which folder recordings are saved to.
     *
     * @param context   App context used to resolve the [DocumentFile].
     * @param folderUri The tree URI, or null.
     * @return The folder name (e.g. "Recordings"), or null.
     */
    fun getFolderDisplayNameOrNull(context: Context, folderUri: Uri?): String? {
        if (folderUri == null) return null
        val directory = DocumentFile.fromTreeUri(context, folderUri)
        return directory?.name
    }
}
