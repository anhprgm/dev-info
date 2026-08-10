package com.anhprgm.deviceinfo.ui.screens.test

import android.app.Activity
import android.os.Build
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.anhprgm.deviceinfo.R

/**
 * Full-screen colour sweep for spotting dead or stuck pixels.
 *
 * Everything about this screen exists to get the panel fully visible:
 * system bars hidden, brightness forced to maximum, screen kept on, and the
 * display cutout area drawn into so the region beside a notch is testable too.
 * It is declared outside the tab scaffold so no navigation bar covers the panel.
 */
@Composable
fun ScreenTestScreen(onExit: () -> Unit) {
    val view = LocalView.current
    var index by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        val window = (view.context as Activity).window
        val controller = WindowCompat.getInsetsController(window, view)
        val previousBrightness = window.attributes.screenBrightness
        // The cutout field itself only exists from API 28, so both the save and
        // the restore have to be guarded, not just the assignment.
        val supportsCutoutMode = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
        val previousCutoutMode = if (supportsCutoutMode) {
            window.attributes.layoutInDisplayCutoutMode
        } else {
            0
        }

        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.attributes = window.attributes.apply {
            screenBrightness = 1f
            if (supportsCutoutMode) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        onDispose {
            controller.show(WindowInsetsCompat.Type.systemBars())
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            window.attributes = window.attributes.apply {
                screenBrightness = previousBrightness
                if (supportsCutoutMode) {
                    layoutInDisplayCutoutMode = previousCutoutMode
                }
            }
        }
    }

    BackHandler { onExit() }

    val color = TEST_COLORS[index]
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .pointerInput(Unit) {
                detectTapGestures(
                    // Tap advances; long-press leaves. A tap cannot exit or the
                    // user could not step through the colours.
                    onTap = { index = (index + 1) % TEST_COLORS.size },
                    onLongPress = { onExit() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Only shown on the first colour so it never obscures a later panel.
        if (index == 0) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = stringResource(R.string.test_screen_instructions),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Pure primaries first — a stuck sub-pixel is most obvious on a solid red,
 * green or blue field — then black and white for dead pixels and backlight
 * bleed, then greys for banding.
 */
private val TEST_COLORS = listOf(
    Color.White,
    Color.Black,
    Color.Red,
    Color.Green,
    Color.Blue,
    Color.Yellow,
    Color.Cyan,
    Color.Magenta,
    Color(0xFF808080),
    Color(0xFF404040),
    Color(0xFFC0C0C0)
)
