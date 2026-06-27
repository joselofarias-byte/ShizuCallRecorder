/*
 * ShizuCallRecorder: FOSS Call recording powered through ADB/Shizuku!
 *  Copyright (C) 2026-present kitsumed (Med)
 *  This software is licensed under the GNU General Public License v3 or later, with additional terms as permitted under Section 7.
 *  The full license text is available in the LICENSE file at the root of this project.
 *  This software is distributed WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.kitsumed.shizucallrecorder.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kitsumed.shizucallrecorder.R
import com.kitsumed.shizucallrecorder.ui.viewmodels.PlaybackViewModel
import java.util.Locale
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackScreen(
    recordingUri: Uri,
    recordingName: String?,
    onBack: () -> Unit,
    viewModel: PlaybackViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(recordingUri) {
        viewModel.prepare(recordingUri)
    }

    DisposableEffect(recordingUri) {
        onDispose {
            viewModel.pause()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.playback_title)) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(text = stringResource(R.string.general_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = recordingName ?: recordingUri.lastPathSegment ?: stringResource(R.string.playback_title),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(24.dp))

            val durationMs = uiState.durationMs
            val progress = if (durationMs > 0L) {
                uiState.positionMs.toFloat() / durationMs.toFloat()
            } else {
                0f
            }

            Slider(
                value = progress.coerceIn(0f, 1f),
                enabled = durationMs > 0L,
                onValueChange = { value ->
                    viewModel.seekTo((value * durationMs).roundToLong())
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = formatTime(uiState.positionMs))
                Text(text = if (durationMs > 0L) formatTime(durationMs) else stringResource(R.string.playback_duration_unknown))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.seekBy(-5_000L) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(R.string.playback_seek_back))
                }

                Button(
                    onClick = { viewModel.togglePlayPause() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (uiState.isPlaying) {
                            stringResource(R.string.general_pause)
                        } else {
                            stringResource(R.string.playback_play)
                        }
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.seekBy(5_000L) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(R.string.playback_seek_forward))
                }
            }
        }
    }
}

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = (milliseconds / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d".format(Locale.getDefault(), minutes, seconds)
}
