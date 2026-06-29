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
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlaybackViewModel(application: Application) : AndroidViewModel(application) {

    private val notesPrefs = application.getSharedPreferences("recording_notes", Context.MODE_PRIVATE)

    private val player: ExoPlayer = ExoPlayer.Builder(application).build()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note

    private var progressJob: Job? = null
    private var currentUri: Uri? = null

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) startProgressTracking() else progressJob?.cancel()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _duration.value = player.duration.coerceAtLeast(0L)
                }
            }
        })
    }

    fun load(uri: Uri) {
        if (uri == currentUri) return

        currentUri = uri
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
        _currentPosition.value = 0L
        _duration.value = 0L
        _note.value = notesPrefs.getString(uri.toString(), "") ?: ""
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun seekForward() {
        val duration = player.duration.takeIf { it > 0L } ?: Long.MAX_VALUE
        player.seekTo((player.currentPosition + 5_000L).coerceAtMost(duration))
    }

    fun seekBack() {
        player.seekTo((player.currentPosition - 5_000L).coerceAtLeast(0L))
    }

    fun seekTo(ms: Long) {
        player.seekTo(ms.coerceAtLeast(0L))
        val duration = _duration.value
        _currentPosition.value = if (duration > 0L) ms.coerceIn(0L, duration) else ms.coerceAtLeast(0L)
    }

    fun updateNote(text: String) {
        _note.value = text
        currentUri?.let { uri ->
            notesPrefs.edit().putString(uri.toString(), text).apply()
        }
    }

    fun resetOnLeave() {
        progressJob?.cancel()
        player.pause()
        player.clearMediaItems()
        currentUri = null
        _isPlaying.value = false
        _currentPosition.value = 0L
        _duration.value = 0L
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                _currentPosition.value = player.currentPosition.coerceAtLeast(0L)
                if (player.duration > 0L) _duration.value = player.duration
                delay(200L)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        progressJob?.cancel()
        player.release()
    }
}

