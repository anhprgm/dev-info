package com.anhprgm.deviceinfo.data.models

data class DisplayInfo(
    val widthPixels: Int,
    val heightPixels: Int,
    /** Computed from xdpi/ydpi; approximate, and null when the device reports bogus dpi. */
    val diagonalInches: Float?,
    val densityDpi: Int,
    val densityScale: Float,
    val densityBucket: DensityBucket,
    val refreshRateHz: Float?,
    val hdrSupported: Boolean
)
