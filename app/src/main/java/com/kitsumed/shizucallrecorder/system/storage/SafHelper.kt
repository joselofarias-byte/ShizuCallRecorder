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

    data class SafResult(
        val uri: Uri,
        val descriptor: ParcelFileDescriptor,
        val displayName: String
    )

    /**
     * Creates an audio file below the user-selected SAF tree. The supplied path may contain
     * subdirectories, which are reused when present or created when missing.
     */
    fun createAudioFile(context: Context, folderUri: Uri, filePath: String, mimeType: String): SafResult? {
        val rootDir = DocumentFile.fromTreeUri(context, folderUri) ?: return null
        var currentDir = rootDir
        if (!currentDir.canWrite()) return null

        val pathSegments = filePath.split('/').filter { it.isNotBlank() }
        if (pathSegments.isEmpty()) return null

        val fileName = pathSegments.last()
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
        val fileDescriptor = context.contentResolver.openFileDescriptor(newFile.uri, "rw") ?: return null
        val rootName = rootDir.name ?: "Recordings"
        val displayName = "$rootName/${pathSegments.joinToString("/")}"
        return SafResult(newFile.uri, fileDescriptor, displayName)
    }

    @OptIn(ExperimentalContracts::class)
    fun isFolderValid(context: Context, folderUri: Uri?): Boolean {
        contract {
            returns(true) implies (folderUri != null)
        }
        if (folderUri == null) return false
        val directory = DocumentFile.fromTreeUri(context, folderUri)
        return directory != null && directory.exists() && directory.canWrite()
    }

    fun getFolderDisplayNameOrNull(context: Context, folderUri: Uri?): String? {
        if (folderUri == null) return null
        return DocumentFile.fromTreeUri(context, folderUri)?.name
    }
}
