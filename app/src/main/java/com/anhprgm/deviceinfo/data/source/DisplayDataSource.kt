package com.anhprgm.deviceinfo.data.source

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import com.anhprgm.deviceinfo.data.models.DensityBucket
import com.anhprgm.deviceinfo.data.models.DisplayInfo
import com.anhprgm.deviceinfo.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow
import kotlin.math.sqrt

@Singleton
class DisplayDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val io: CoroutineDispatcher
) {
    /**
     * The display is resolved through [DisplayManager], not `Context.display`.
     *
     * `Context.getDisplay()` throws UnsupportedOperationException when called
     * on a non-visual Context, and this data source is a singleton holding the
     * *application* context. DisplayManager works from any context and returns
     * the same default display.
     */
    private fun defaultDisplay(): Display? =
        runCatching {
            context.getSystemService(DisplayManager::class.java)
                ?.getDisplay(Display.DEFAULT_DISPLAY)
        }.getOrNull()

    @Suppress("DEPRECATION")
    suspend fun getDisplayInfo(): DisplayInfo = withContext(io) {
        val display = defaultDisplay()
        val metrics = DisplayMetrics()

        // getRealMetrics is deprecated from API 30, but its replacement
        // (WindowMetrics) requires a visual context and does not expose the
        // physical xdpi/ydpi needed for the diagonal calculation.
        display?.getRealMetrics(metrics)
            ?: metrics.setTo(context.resources.displayMetrics)

        DisplayInfo(
            widthPixels = metrics.widthPixels,
            heightPixels = metrics.heightPixels,
            diagonalInches = diagonalInches(metrics),
            densityDpi = metrics.densityDpi,
            densityScale = metrics.density,
            densityBucket = bucketOf(metrics.density),
            refreshRateHz = display?.refreshRate?.takeIf { it > 0f },
            // Display.isHdr is API 26, not 24.
            hdrSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                display?.isHdr == true
        )
    }

    /** Some devices report nonsense xdpi/ydpi; guard rather than emit Infinity. */
    private fun diagonalInches(metrics: DisplayMetrics): Float? {
        val xdpi = metrics.xdpi
        val ydpi = metrics.ydpi
        if (xdpi <= 0f || ydpi <= 0f) return null
        val widthInches = metrics.widthPixels / xdpi
        val heightInches = metrics.heightPixels / ydpi
        val diagonal = sqrt(widthInches.pow(2) + heightInches.pow(2))
        return diagonal.takeIf { it.isFinite() && it > 0f }
    }

    private fun bucketOf(density: Float): DensityBucket = when {
        density >= 4.0f -> DensityBucket.XXXHDPI
        density >= 3.0f -> DensityBucket.XXHDPI
        density >= 2.0f -> DensityBucket.XHDPI
        density >= 1.5f -> DensityBucket.HDPI
        density >= 1.0f -> DensityBucket.MDPI
        else -> DensityBucket.LDPI
    }
}
