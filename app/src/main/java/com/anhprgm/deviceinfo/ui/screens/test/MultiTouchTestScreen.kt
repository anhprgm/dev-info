package com.anhprgm.deviceinfo.ui.screens.test

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anhprgm.deviceinfo.R

/**
 * Multi-touch tracker: draws every active pointer and records the maximum
 * simultaneous count, which is the number people actually want to know.
 */
@Composable
fun MultiTouchTestScreen(onExit: () -> Unit) {
    val view = LocalView.current
    val pointers = remember { mutableStateMapOf<PointerId, Offset>() }
    var maxPointers by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        val window = (view.context as Activity).window
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    BackHandler { onExit() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        pointers.clear()
                        event.changes
                            .filter { it.pressed }
                            .forEach { pointers[it.id] = it.position }
                        if (pointers.size > maxPointers) maxPointers = pointers.size
                        // Consume so the gesture does not propagate to the
                        // navigation host and pop this destination mid-test.
                        event.changes.forEach { it.consume() }
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            pointers.entries.forEachIndexed { index, (_, offset) ->
                val color = POINTER_COLORS[index % POINTER_COLORS.size]
                drawCircle(color = color, radius = 90f, center = offset, alpha = 0.35f)
                drawCircle(color = color, radius = 12f, center = offset)
                // Crosshair makes the exact contact point readable.
                drawLine(
                    color = color,
                    start = Offset(0f, offset.y),
                    end = Offset(size.width, offset.y),
                    strokeWidth = 1.5f
                )
                drawLine(
                    color = color,
                    start = Offset(offset.x, 0f),
                    end = Offset(offset.x, size.height),
                    strokeWidth = 1.5f
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 72.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.test_touch_active, pointers.size),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.test_touch_max, maxPointers),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.test_touch_instructions),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

private val POINTER_COLORS = listOf(
    Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047), Color(0xFFFB8C00),
    Color(0xFF8E24AA), Color(0xFF00ACC1), Color(0xFFFDD835), Color(0xFF6D4C41),
    Color(0xFF3949AB), Color(0xFFD81B60)
)
