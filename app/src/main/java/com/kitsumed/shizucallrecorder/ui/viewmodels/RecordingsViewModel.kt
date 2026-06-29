/*
 * ShizuCallRecorder: FOSS Call recording powered through ADB/Shizuku!
 *  Copyright (C) 2026-present kitsumed (Med)
 *  This software is licensed under the GNU General Public License v3 or later, with additional terms as permitted under Section 7.
 *  The full license text is available in the LICENSE file at the root of this project.
 *  This software is distributed WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.kitsumed.shizucallrecorder.ui.viewmodels

import android.app.Application
import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.ContactsContract
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kitsumed.shizucallrecorder.data.AppPreferences
import com.kitsumed.shizucallrecorder.utils.RecordingFileNameFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RecordingItem(
    val uri: Uri,
    val displayName: String,
    val phoneNumber: String,
    val contactName: String?,
    val direction: String,
    val date: Date?,
    val mimeType: String?,
    val sizeBytes: Long,
    val lastModified: Long,
    val durationMs: Long = 0L,
    val extension: String,
    val isFavourite: Boolean = false,
    val noteText: String = ""
)

enum class RecordingsSortField { TIME, NAME, SIZE }
enum class RecordingsSortOrder { ASC, DESC }
enum class RecordingsFilterTab { ALL, FAVOURITES }

data class RecordingsSortConfig(
    val field: RecordingsSortField = RecordingsSortField.TIME,
    val order: RecordingsSortOrder = RecordingsSortOrder.DESC
)

data class RecordingsUiState(
    val isLoading: Boolean = false,
    val folderConfigured: Boolean = true,
    val recordings: List<RecordingItem> = emptyList(),
    val searchQuery: String = "",
    val filterTab: RecordingsFilterTab = RecordingsFilterTab.ALL,
    val sortConfig: RecordingsSortConfig = RecordingsSortConfig(),
    val selectedUris: Set<Uri> = emptySet(),
    val errorMessage: String? = null
)

class RecordingsViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val preferences = AppPreferences(appContext)
    private val favouritesPrefs = appContext.getSharedPreferences("recording_favourites", Context.MODE_PRIVATE)
    private val notesPrefs = appContext.getSharedPreferences("recording_notes", Context.MODE_PRIVATE)
    private val durationCache = appContext.getSharedPreferences("recording_duration", Context.MODE_PRIVATE)

    private val allRecordings = MutableStateFlow<List<RecordingItem>>(emptyList())

    private val _uiState = MutableStateFlow(RecordingsUiState(isLoading = true))
    val uiState: StateFlow<RecordingsUiState> = _uiState.asStateFlow()

    fun loadRecordings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val folderUri = preferences.getRecordingFolderUri()
                    if (folderUri == null) {
                        return@runCatching LoadResult(folderConfigured = false)
                    }

                    val folder = DocumentFile.fromTreeUri(appContext, folderUri)
                    if (folder == null || !folder.exists() || !folder.isDirectory || !folder.canRead()) {
                        return@runCatching LoadResult(folderConfigured = false)
                    }

                    // List only ShizuCallRecorder's own recordings to avoid surfacing files
                    // created by other apps (EverCallRecorder, Cally, ...) that may share the
                    // same SAF folder. New recordings carry the "shizucall_" ownership prefix.
                    // Legacy recordings (created before the prefix existed) are included alongside
                    // owned ones so the user never loses access to older recordings.
                    val sourceFiles = folder
                        .listFiles()
                        .asSequence()
                        .filter { file -> file.isFile && file.isSupportedAudioFile() }
                        .filter { file ->
                            // Parentheses are required: without them, `||` would evaluate
                            // isLegacyRecording() even when file.name is null, causing a NPE.
                            file.name?.let { name ->
                                RecordingFileNameFormatter.isOwnedRecording(name) ||
                                    RecordingFileNameFormatter.isLegacyRecording(name)
                            } ?: false
                        }
                        .toList()

                    val recordings = sourceFiles
                        .mapNotNull { file -> file.toRecordingItem(appContext) }
                        .toList()

                    LoadResult(folderConfigured = true, recordings = recordings)
                }
            }

            result.fold(
                onSuccess = { loadResult ->
                    allRecordings.value = loadResult.recordings
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            folderConfigured = loadResult.folderConfigured,
                            errorMessage = null
                        )
                    }
                    applyFilters()
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

    fun refresh() {
        if (!_uiState.value.isLoading) loadRecordings()
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun clearSearch() {
        setSearchQuery("")
    }

    fun setFilterTab(tab: RecordingsFilterTab) {
        _uiState.update { it.copy(filterTab = tab) }
        applyFilters()
    }

    fun setSortConfig(config: RecordingsSortConfig) {
        _uiState.update { it.copy(sortConfig = config) }
        applyFilters()
    }

    fun toggleSelection(uri: Uri) {
        _uiState.update { state ->
            val updated = state.selectedUris.toMutableSet()
            if (uri in updated) updated.remove(uri) else updated.add(uri)
            state.copy(selectedUris = updated)
        }
    }

    fun selectAllVisible() {
        _uiState.update { state ->
            state.copy(selectedUris = state.selectedUris + state.recordings.map { it.uri })
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedUris = emptySet()) }
    }

    fun toggleFavourite(item: RecordingItem) {
        val key = item.uri.toString()
        val newValue = !favouritesPrefs.getBoolean(key, false)
        favouritesPrefs.edit().putBoolean(key, newValue).apply()

        allRecordings.value = allRecordings.value.map {
            if (it.uri == item.uri) it.copy(isFavourite = newValue) else it
        }
        applyFilters()
    }

    fun deleteRecording(item: RecordingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { DocumentFile.fromSingleUri(appContext, item.uri)?.delete() }
            cleanupMetadata(item.uri)
            withContext(Dispatchers.Main) {
                allRecordings.value = allRecordings.value.filter { it.uri != item.uri }
                _uiState.update { it.copy(selectedUris = it.selectedUris - item.uri) }
                applyFilters()
            }
        }
    }

    fun deleteSelected() {
        val targets = _uiState.value.selectedUris.toSet()
        if (targets.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            targets.forEach { uri ->
                runCatching { DocumentFile.fromSingleUri(appContext, uri)?.delete() }
                cleanupMetadata(uri)
            }
            withContext(Dispatchers.Main) {
                allRecordings.value = allRecordings.value.filter { it.uri !in targets }
                _uiState.update { it.copy(selectedUris = emptySet()) }
                applyFilters()
            }
        }
    }

    fun getNote(uri: Uri): String {
        return notesPrefs.getString(uri.toString(), "") ?: ""
    }

    fun saveNote(uri: Uri, note: String) {
        notesPrefs.edit().putString(uri.toString(), note).apply()
        allRecordings.value = allRecordings.value.map {
            if (it.uri == uri) it.copy(noteText = note) else it
        }
        applyFilters()
    }

    suspend fun loadContactPhoto(context: Context, phoneNumber: String): ImageBitmap? =
        withContext(Dispatchers.IO) {
            try {
                val lookupUri = Uri.withAppendedPath(
                    ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    phoneNumber
                )
                context.contentResolver.query(
                    lookupUri,
                    arrayOf(ContactsContract.PhoneLookup.PHOTO_URI),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (!cursor.moveToFirst()) return@withContext null
                    val photoUri = cursor.getString(0)?.let(Uri::parse) ?: return@withContext null
                    context.contentResolver.openInputStream(photoUri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
                }
            } catch (_: Exception) {
                null
            }
        }

    private fun applyFilters() {
        val state = _uiState.value
        val query = state.searchQuery.trim().lowercase(Locale.getDefault())

        var filtered = allRecordings.value

        if (query.isNotBlank()) {
            filtered = filtered.filter { item ->
                item.phoneNumber.lowercase(Locale.getDefault()).contains(query) ||
                    item.displayName.lowercase(Locale.getDefault()).contains(query) ||
                    (item.contactName?.lowercase(Locale.getDefault())?.contains(query) == true) ||
                    item.noteText.lowercase(Locale.getDefault()).contains(query)
            }
        }

        if (state.filterTab == RecordingsFilterTab.FAVOURITES) {
            filtered = filtered.filter { it.isFavourite }
        }

        filtered = when (state.sortConfig.field) {
            RecordingsSortField.TIME -> filtered.sortedBy { it.date?.time ?: it.lastModified }
            RecordingsSortField.NAME -> filtered.sortedBy { (it.contactName ?: it.phoneNumber).lowercase(Locale.getDefault()) }
            RecordingsSortField.SIZE -> filtered.sortedBy { it.sizeBytes }
        }

        if (state.sortConfig.order == RecordingsSortOrder.DESC) {
            filtered = filtered.reversed()
        }

        _uiState.update { it.copy(recordings = filtered) }
    }

    private fun DocumentFile.toRecordingItem(context: Context): RecordingItem? {
        val name = name ?: return null
        // Strip the "shizucall_" ownership prefix before parsing so both the new and the legacy
        // naming schemes parse identically. The user never sees the prefix.
        val normalizedName = RecordingFileNameFormatter.stripOwnershipPrefix(name)
        val extension = name.substringAfterLast('.', missingDelimiterValue = "").lowercase(Locale.getDefault())
        val baseName = normalizedName.substringBeforeLast('.', missingDelimiterValue = normalizedName)
        val parts = baseName.split("_")

        val direction = parts.getOrNull(2).orEmpty()
        val phoneNumber = parts.drop(3).joinToString("_").trim().ifBlank { "Unknown" }
        val dateRaw = if (parts.size >= 2) "${parts[0]}_${parts[1]}" else ""
        val parsedDate = parseDate(dateRaw)
        val contactName = if (phoneNumber != "Unknown") resolveContactName(context, phoneNumber) else null
        val fileSize = length()
        val duration = resolveAudioDuration(context, uri, fileSize)
        val note = notesPrefs.getString(uri.toString(), "") ?: ""

        return RecordingItem(
            uri = uri,
            displayName = normalizedName,
            phoneNumber = phoneNumber,
            contactName = contactName,
            direction = direction,
            date = parsedDate,
            mimeType = type,
            sizeBytes = fileSize,
            lastModified = lastModified(),
            durationMs = duration,
            extension = extension,
            isFavourite = favouritesPrefs.getBoolean(uri.toString(), false),
            noteText = note
        )
    }

    private fun parseDate(raw: String): Date? {
        for (format in dateFormats) {
            runCatching { return format.parse(raw) }
        }
        return null
    }

    private fun resolveContactName(context: Context, phoneNumber: String): String? {
        return try {
            val lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, phoneNumber)
            context.contentResolver.query(
                lookupUri,
                arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun resolveAudioDuration(context: Context, uri: Uri, fileSizeBytes: Long): Long {
        val cacheKey = "${uri}_$fileSizeBytes"
        val cached = durationCache.getLong(cacheKey, -1L)
        if (cached >= 0L) return cached

        val duration = try {
            val retriever = MediaMetadataRetriever()
            try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                    retriever.setDataSource(descriptor.fileDescriptor)
                }
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLongOrNull()
                    ?: 0L
            } finally {
                retriever.release()
            }
        } catch (_: Exception) {
            0L
        }

        durationCache.edit().putLong(cacheKey, duration).apply()
        return duration
    }

    private fun cleanupMetadata(uri: Uri) {
        val key = uri.toString()
        favouritesPrefs.edit().remove(key).apply()
        notesPrefs.edit().remove(key).apply()
    }

    private data class LoadResult(
        val folderConfigured: Boolean,
        val recordings: List<RecordingItem> = emptyList()
    )

    private companion object {
        val dateFormats = listOf(
            SimpleDateFormat("yyyyMMdd_HHmmss.SSSZ", Locale.CANADA),
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CANADA)
        )
    }
}

private fun DocumentFile.isSupportedAudioFile(): Boolean {
    val mimeType = type.orEmpty()
    if (mimeType.startsWith("audio/")) return true
    if (mimeType == "application/ogg") return true

    val extension = name
        ?.substringAfterLast('.', missingDelimiterValue = "")
        ?.lowercase(Locale.getDefault())
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

