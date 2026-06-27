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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlaybackUiState(
    val isPrepared: Boolean = false,
    val isPlaying: Boolean = false,
    val durationMs: Long = 0L,
    val positionMs: Long = 0L
)

class PlaybackViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext

    private var player: ExoPlayer? = null
    private var currentUri: Uri? = null
    private var progressJob: Job? = null

    private val _uiState = MutableStateFlow(PlaybackUiState())
    val uiState: StateFlow<PlaybackUiState> = _uiState.asStateFlow()

    fun prepare(recordingUri: Uri) {
        if (currentUri == recordingUri && player != null) return

        progressJob?.cancel()
        player?.release()

        currentUri = recordingUri

        val newPlayer = ExoPlayer.Builder(appContext).build()
        player = newPlayer

        newPlayer.addListener(
            object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    updateStateFromPlayer()
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    updateStateFromPlayer()
                }
            }
        )

        newPlayer.setMediaItem(MediaItem.fromUri(recordingUri))
        newPlayer.prepare()

        startProgressUpdates()
        updateStateFromPlayer()
    }

    fun togglePlayPause() {
        val currentPlayer = player ?: return
        if (currentPlayer.isPlaying) {
            currentPlayer.pause()
        } else {
            currentPlayer.play()
        }
        updateStateFromPlayer()
    }

    fun pause() {
        player?.pause()
        updateStateFromPlayer()
    }

    fun seekTo(positionMs: Long) {
        val currentPlayer = player ?: return
        val duration = currentPlayer.duration.takeIf { it > 0L } ?: Long.MAX_VALUE
        currentPlayer.seekTo(positionMs.coerceIn(0L, duration))
        updateStateFromPlayer()
    }

    fun seekBy(deltaMs: Long) {
        val currentPlayer = player ?: return
        seekTo(currentPlayer.currentPosition + deltaMs)
    }

    private fun startProgressUpdates() {
        progressJob = viewModelScope.launch {
            while (isActive) {
                updateStateFromPlayer()
                delay(500L)
            }
        }
    }

    private fun updateStateFromPlayer() {
        val currentPlayer = player ?: return
        val duration = currentPlayer.duration.takeIf { it > 0L } ?: 0L
        val position = currentPlayer.currentPosition.coerceAtLeast(0L)

        _uiState.update {
            it.copy(
                isPrepared = currentPlayer.playbackState != Player.STATE_IDLE,
                isPlaying = currentPlayer.isPlaying,
                durationMs = duration,
                positionMs = if (duration > 0L) position.coerceAtMost(duration) else position
            )
        }
    }

    override fun onCleared() {
        progressJob?.cancel()
        player?.release()
        player = null
        super.onCleared()
    }
}
