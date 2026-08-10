package com.anhprgm.deviceinfo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.anhprgm.deviceinfo.ui.theme.Dimens

/**
 * Line chart, promoted out of HistoryScreen so the dashboard can use it too.
 *
 * Fixes a divide-by-zero in the original: `range = max - min` is 0 for flat
 * data, so every y became NaN and the chart rendered blank — exactly the case
 * you hit when the battery holds steady.
 */
@Composable
fun LineChart(
    data: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillGradient: Boolean = true,
    showPoints: Boolean = true
) {
    if (data.isEmpty()) return
    val fillColor = lineColor.copy(alpha = 0.18f)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val minValue = data.min()
        val maxValue = data.max()
        val range = maxValue - minValue

        // Flat series: centre the line instead of producing NaN.
        fun yOf(value: Float) =
            if (range <= 0f) h / 2f else h - ((value - minValue) / range * h)

        if (data.size == 1) {
            drawCircle(color = lineColor, radius = 4f, center = Offset(w / 2, yOf(data[0])))
            return@Canvas
        }

        val stepX = w / (data.size - 1)
        val line = Path().apply {
            moveTo(0f, yOf(data[0]))
            data.forEachIndexed { i, v -> lineTo(i * stepX, yOf(v)) }
        }

        if (fillGradient) {
            val area = Path().apply {
                addPath(line)
                lineTo((data.size - 1) * stepX, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = area,
                brush = Brush.verticalGradient(listOf(fillColor, Color.Transparent))
            )
        }

        drawPath(path = line, color = lineColor, style = Stroke(width = 3f))

        if (showPoints) {
            data.forEachIndexed { i, v ->
                drawCircle(color = lineColor, radius = 3f, center = Offset(i * stepX, yOf(v)))
            }
        }
    }
}

/** Compact inline trend for dashboard tiles — no points, no axis. */
@Composable
fun Sparkline(
    data: List<Float>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(Dimens.sparklineHeight),
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    LineChart(
        data = data,
        modifier = modifier,
        lineColor = lineColor,
        fillGradient = true,
        showPoints = false
    )
}
