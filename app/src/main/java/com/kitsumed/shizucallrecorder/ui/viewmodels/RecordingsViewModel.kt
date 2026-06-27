/*
 * ShizuCallRecorder: FOSS Call recording powered through ADB/Shizuku!
 *  Copyright (C) 2026-present kitsumed (Med)
 *  This software is licensed under the GNU General Public License v3 or later, with additional terms as permitted under Section 7.
 *  The full license text is available in the LICENSE file at the root of this project.
 *  This software is distributed WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.kitsumed.shizucallrecorder.ui.viewmodels

import android.app.Application
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kitsumed.shizucallrecorder.data.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class RecordingItem(
    val uri: Uri,
    val displayName: String,
    val mimeType: String?,
    val sizeBytes: Long,
    val lastModified: Long
)

data class RecordingsUiState(
    val isLoading: Boolean = false,
    val folderConfigured: Boolean = true,
    val recordings: List<RecordingItem> = emptyList(),
    val errorMessage: String? = null
)

class RecordingsViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val preferences = AppPreferences(appContext)

    private val _uiState = MutableStateFlow(RecordingsUiState(isLoading = true))
    val uiState: StateFlow<RecordingsUiState> = _uiState.asStateFlow()

    fun loadRecordings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val folderUri = readRecordingFolderUri()
                    if (folderUri == null) {
                        return@runCatching LoadResult(folderConfigured = false)
                    }

                    val folder = DocumentFile.fromTreeUri(appContext, folderUri)
                    if (folder == null || !folder.exists() || !folder.isDirectory) {
                        return@runCatching LoadResult(folderConfigured = false)
                    }

                    val recordings = folder
                        .listFiles()
                        .asSequence()
                        .filter { file -> file.isFile && file.isSupportedAudioFile() }
                        .map { file ->
                            RecordingItem(
                                uri = file.uri,
                                displayName = file.name ?: file.uri.lastPathSegment ?: "Recording",
                                mimeType = file.type,
                                sizeBytes = file.length(),
                                lastModified = file.lastModified()
                            )
                        }
                        .sortedByDescending { it.lastModified }
                        .toList()

                    LoadResult(folderConfigured = true, recordings = recordings)
                }
            }

            result.fold(
                onSuccess = { loadResult ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            folderConfigured = loadResult.folderConfigured,
                            recordings = loadResult.recordings,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message
                        )
                    }
                }
            )
        }
    }

    private fun readRecordingFolderUri(): Uri? {
        val method = preferences.javaClass.methods.firstOrNull { method ->
            method.name == "getRecordingFolderUri" && method.parameterTypes.isEmpty()
        } ?: return null

        return when (val value = method.invoke(preferences)) {
            is Uri -> value
            is String -> value.takeIf { it.isNotBlank() }?.let(Uri::parse)
            else -> null
        }
    }

    private data class LoadResult(
        val folderConfigured: Boolean,
        val recordings: List<RecordingItem> = emptyList()
    )
}

private fun DocumentFile.isSupportedAudioFile(): Boolean {
    val mimeType = type.orEmpty()
    if (mimeType.startsWith("audio/")) return true

    val extension = name
        ?.substringAfterLast('.', missingDelimiterValue = "")
        ?.lowercase()
        .orEmpty()

    return extension in supportedAudioExtensions
}

private val supportedAudioExtensions = setOf(
    "aac",
    "amr",
    "flac",
    "m4a",
    "mp3",
    "mp4",
    "oga",
    "ogg",
    "opus",
    "wav",
    "3gp"
)
