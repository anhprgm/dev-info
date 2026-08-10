package com.anhprgm.deviceinfo.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import com.anhprgm.deviceinfo.ui.format.Formatters
import com.anhprgm.deviceinfo.ui.theme.Dimens

/** Leaves a gap at the bottom so the arc reads as a gauge, not a closed ring. */
private const val START_ANGLE = 135f
private const val SWEEP_ANGLE = 270f

/**
 * Circular progress gauge.
 *
 * [progress] is nullable on purpose: an unmeasurable value (system-wide CPU,
 * thermal headroom on devices with no HAL) draws an empty track and reads
 * "N/A" rather than a confident 0%.
 */
@Composable
fun CircularGauge(
    progress: Float?,
    label: String,
    modifier: Modifier = Modifier,
    valueText: String = Formatters.percent(progress?.times(100f), decimals = 0),
    size: Dp = Dimens.gaugeSize,
    strokeWidth: Dp = Dimens.gaugeStroke,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest
) {
    val animated by animateFloatAsState(
        targetValue = progress?.coerceIn(0f, 1f) ?: 0f,
        animationSpec = tween(durationMillis = 600),
        label = "gauge"
    )
    val strokePx = with(LocalDensity.current) { strokeWidth.toPx() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        // One spoken label instead of the canvas and text being read separately.
        modifier = modifier.clearAndSetSemantics {
            contentDescription = "$label: $valueText"
        }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(size)) {
                val inset = strokePx / 2
                val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
                val topLeft = Offset(inset, inset)
                val stroke = Stroke(width = strokePx, cap = StrokeCap.Round)

                drawArc(
                    color = trackColor,
                    startAngle = START_ANGLE,
                    sweepAngle = SWEEP_ANGLE,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke
                )
                if (progress != null) {
                    drawArc(
                        color = color,
                        startAngle = START_ANGLE,
                        sweepAngle = SWEEP_ANGLE * animated,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = stroke
                    )
                }
            }
            Text(
                text = valueText,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Dimens.spaceSm)
        )
    }
}
