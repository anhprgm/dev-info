package com.anhprgm.deviceinfo.ui.screens.test

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anhprgm.deviceinfo.R
import com.anhprgm.deviceinfo.data.source.AudioChannel
import com.anhprgm.deviceinfo.ui.components.DetailColumn
import com.anhprgm.deviceinfo.ui.components.DevInfoScaffold
import com.anhprgm.deviceinfo.ui.components.InfoCard
import com.anhprgm.deviceinfo.ui.components.NoticeCard
import com.anhprgm.deviceinfo.ui.format.Formatters
import com.anhprgm.deviceinfo.ui.theme.Dimens
import com.anhprgm.deviceinfo.ui.viewmodel.HardwareTestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeakerTestScreen(
    onNavigateBack: () -> Unit,
    viewModel: HardwareTestViewModel = hiltViewModel()
) {
    val playing by viewModel.playingChannel.collectAsStateWithLifecycle()
    val muted = remember { viewModel.isMuted() }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopTone() }
    }

    DevInfoScaffold(
        title = stringResource(R.string.test_speaker),
        onNavigateBack = onNavigateBack
    ) { padding ->
        DetailColumn(padding) {
            // A silent speaker test with the volume at zero looks like broken
            // hardware; say so before the user concludes that.
            if (muted) {
                NoticeCard(
                    title = stringResource(R.string.test_speaker),
                    message = stringResource(R.string.test_speaker_muted),
                    icon = Icons.Default.VolumeUp
                )
                Spacer(Modifier.height(Dimens.cardSpacing))
            }

            InfoCard(title = stringResource(R.string.test_speaker)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
                ) {
                    ChannelButton(
                        labelRes = R.string.test_speaker_left,
                        channel = AudioChannel.LEFT,
                        playing = playing,
                        onClick = viewModel::playTone,
                        modifier = Modifier.weight(1f)
                    )
                    ChannelButton(
                        labelRes = R.string.test_speaker_both,
                        channel = AudioChannel.BOTH,
                        playing = playing,
                        onClick = viewModel::playTone,
                        modifier = Modifier.weight(1f)
                    )
                    ChannelButton(
                        labelRes = R.string.test_speaker_right,
                        channel = AudioChannel.RIGHT,
                        playing = playing,
                        onClick = viewModel::playTone,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (playing != null) {
                    Spacer(Modifier.height(Dimens.spaceMd))
                    Text(
                        text = stringResource(R.string.test_speaker_playing),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun ChannelButton(
    labelRes: Int,
    channel: AudioChannel,
    playing: AudioChannel?,
    onClick: (AudioChannel) -> Unit,
    modifier: Modifier = Modifier
) {
    if (playing == channel) {
        Button(onClick = { onClick(channel) }, modifier = modifier) {
            Text(stringResource(labelRes))
        }
    } else {
        OutlinedButton(onClick = { onClick(channel) }, modifier = modifier) {
            Text(stringResource(labelRes))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MicrophoneTestScreen(
    onNavigateBack: () -> Unit,
    viewModel: HardwareTestViewModel = hiltViewModel()
) {
    val level by viewModel.micLevel.collectAsStateWithLifecycle()
    val running by viewModel.micRunning.collectAsStateWithLifecycle()
    var hasPermission by remember { mutableStateOf(viewModel.hasMicPermission()) }
    val animatedLevel by animateFloatAsState(targetValue = level, label = "mic")

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) viewModel.startMicMeter()
    }

    // Releasing the mic when the screen closes is not optional — holding it
    // would block other apps and show a persistent privacy indicator.
    DisposableEffect(Unit) {
        onDispose { viewModel.stopMicMeter() }
    }

    DevInfoScaffold(
        title = stringResource(R.string.test_microphone),
        onNavigateBack = onNavigateBack
    ) { padding ->
        DetailColumn(padding) {
            if (!viewModel.hasMicrophone) {
                NoticeCard(
                    title = stringResource(R.string.test_microphone),
                    message = stringResource(R.string.test_mic_absent)
                )
                return@DetailColumn
            }

            if (!hasPermission) {
                NoticeCard(
                    title = stringResource(R.string.test_mic_permission_title),
                    message = stringResource(R.string.test_mic_permission_message),
                    icon = Icons.Default.Mic,
                    action = {
                        Button(onClick = {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }) {
                            Text(stringResource(R.string.action_grant))
                        }
                    }
                )
                return@DetailColumn
            }

            InfoCard(title = stringResource(R.string.test_mic_level)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = Formatters.percent(animatedLevel * 100f, decimals = 0),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(Dimens.spaceMd))
                    LinearProgressIndicator(
                        progress = { animatedLevel },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                    )
                    Spacer(Modifier.height(Dimens.spaceLg))
                    if (running) {
                        Button(onClick = { viewModel.stopMicMeter() }) {
                            Text(stringResource(R.string.test_mic_stop))
                        }
                    } else {
                        Button(onClick = { viewModel.startMicMeter() }) {
                            Text(stringResource(R.string.test_mic_start))
                        }
                    }
                }
            }

            Spacer(Modifier.height(Dimens.cardSpacing))
            Text(
                text = stringResource(R.string.test_mic_permission_message),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Dimens.spaceXs)
            )
        }
    }
}
