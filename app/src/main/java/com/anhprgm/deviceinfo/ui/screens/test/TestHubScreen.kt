package com.anhprgm.deviceinfo.ui.screens.test

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.anhprgm.deviceinfo.R
import com.anhprgm.deviceinfo.navigation.ButtonTest
import com.anhprgm.deviceinfo.navigation.MicrophoneTest
import com.anhprgm.deviceinfo.navigation.MultiTouchTest
import com.anhprgm.deviceinfo.navigation.ScreenTest
import com.anhprgm.deviceinfo.navigation.SensorLive
import com.anhprgm.deviceinfo.navigation.SpeakerTest
import com.anhprgm.deviceinfo.navigation.VibrationTest
import com.anhprgm.deviceinfo.ui.components.ActionTile
import com.anhprgm.deviceinfo.ui.components.NoticeCard
import com.anhprgm.deviceinfo.ui.theme.Dimens

private data class TestEntry(
    val icon: ImageVector,
    val titleRes: Int,
    val descRes: Int,
    val route: Any
)

private val tests = listOf(
    TestEntry(Icons.Default.Tv, R.string.test_screen, R.string.test_screen_desc, ScreenTest),
    TestEntry(Icons.Default.TouchApp, R.string.test_touch, R.string.test_touch_desc, MultiTouchTest),
    // Accelerometer is the default sensor to open; the screen can switch.
    TestEntry(Icons.Default.Sensors, R.string.test_sensors, R.string.test_sensors_desc, SensorLive(1)),
    TestEntry(Icons.Default.VolumeUp, R.string.test_speaker, R.string.test_speaker_desc, SpeakerTest),
    TestEntry(Icons.Default.Mic, R.string.test_microphone, R.string.test_microphone_desc, MicrophoneTest),
    TestEntry(Icons.Default.Vibration, R.string.test_vibration, R.string.test_vibration_desc, VibrationTest),
    TestEntry(Icons.Default.Keyboard, R.string.test_buttons, R.string.test_buttons_desc, ButtonTest)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestHubScreen(
    contentPadding: PaddingValues,
    onNavigate: (Any) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.test_hub_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Dimens.screenPadding,
                end = Dimens.screenPadding,
                top = innerPadding.calculateTopPadding() + Dimens.spaceSm,
                bottom = contentPadding.calculateBottomPadding() + Dimens.spaceXl
            ),
            horizontalArrangement = Arrangement.spacedBy(Dimens.cardSpacing),
            verticalArrangement = Arrangement.spacedBy(Dimens.cardSpacing)
        ) {
            items(tests, span = { GridItemSpan(1) }) { test ->
                ActionTile(
                    icon = test.icon,
                    title = stringResource(test.titleRes),
                    subtitle = stringResource(test.descRes),
                    onClick = { onNavigate(test.route) }
                )
            }

            // Stating what cannot be tested is more useful than omitting it and
            // leaving people to wonder why there is no power-button test.
            item(span = { GridItemSpan(maxLineSpan) }) {
                NoticeCard(
                    title = stringResource(R.string.test_buttons),
                    message = stringResource(R.string.test_buttons_power_note),
                    icon = Icons.Default.Fingerprint
                )
            }
        }
    }
}
