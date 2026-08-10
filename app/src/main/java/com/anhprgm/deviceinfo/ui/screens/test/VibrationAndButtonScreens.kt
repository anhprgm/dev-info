package com.anhprgm.deviceinfo.ui.screens.test

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.anhprgm.deviceinfo.R
import com.anhprgm.deviceinfo.ui.components.DetailColumn
import com.anhprgm.deviceinfo.ui.components.DetailRow
import com.anhprgm.deviceinfo.ui.components.DevInfoScaffold
import com.anhprgm.deviceinfo.ui.components.InfoCard
import com.anhprgm.deviceinfo.ui.components.NoticeCard
import com.anhprgm.deviceinfo.ui.format.yesNo
import com.anhprgm.deviceinfo.ui.theme.Dimens
import com.anhprgm.deviceinfo.ui.viewmodel.HardwareTestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VibrationTestScreen(
    onNavigateBack: () -> Unit,
    viewModel: HardwareTestViewModel = hiltViewModel()
) {
    DisposableEffect(Unit) {
        onDispose { viewModel.cancelVibration() }
    }

    DevInfoScaffold(
        title = stringResource(R.string.test_vibration),
        onNavigateBack = onNavigateBack
    ) { padding ->
        DetailColumn(padding) {
            if (!viewModel.hasVibrator) {
                NoticeCard(
                    title = stringResource(R.string.test_vibration),
                    message = stringResource(R.string.test_vibration_absent)
                )
                return@DetailColumn
            }

            InfoCard(title = stringResource(R.string.test_vibration)) {
                DetailRow(
                    stringResource(R.string.test_vibration_amplitude),
                    viewModel.hasAmplitudeControl.yesNo()
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceSm)) {
                    Button(
                        onClick = { viewModel.vibrate(80) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.test_vibration_short))
                    }
                    Button(
                        onClick = { viewModel.vibrate(800) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.test_vibration_long))
                    }
                    // The ramp is only distinguishable when the motor supports
                    // amplitude control; otherwise it degrades to on/off.
                    Button(
                        onClick = { viewModel.vibratePattern() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.test_vibration_pattern))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButtonTestScreen(onNavigateBack: () -> Unit) {
    val presses = remember { mutableStateListOf<String>() }
    val focusRequester = remember { FocusRequester() }
    val volumeUp = stringResource(R.string.test_buttons_volume_up)
    val volumeDown = stringResource(R.string.test_buttons_volume_down)

    // Key events only arrive at a focused composable.
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    DevInfoScaffold(
        title = stringResource(R.string.test_buttons),
        onNavigateBack = onNavigateBack
    ) { padding ->
        Column(
            modifier = Modifier
                .focusRequester(focusRequester)
                .focusable()
                .onKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                    when (event.key) {
                        Key.VolumeUp -> {
                            presses.add(0, volumeUp)
                            // Returning true consumes the key, otherwise the
                            // system volume panel swallows it first.
                            true
                        }
                        Key.VolumeDown -> {
                            presses.add(0, volumeDown)
                            true
                        }
                        else -> false
                    }
                }
        ) {
            DetailColumn(padding) {
                InfoCard(title = stringResource(R.string.test_buttons)) {
                    Text(
                        text = stringResource(R.string.test_buttons_instructions),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(Dimens.spaceMd))
                    if (presses.isEmpty()) {
                        Text(
                            text = stringResource(R.string.test_buttons_none),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        presses.take(10).forEachIndexed { index, label ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (index == 0) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Dimens.cardSpacing))

                NoticeCard(
                    title = stringResource(R.string.test_buttons),
                    message = stringResource(R.string.test_buttons_power_note),
                    icon = Icons.Default.Info
                )
            }
        }
    }
}
