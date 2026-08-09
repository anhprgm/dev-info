package com.anhprgm.deviceinfo.data.models

data class CameraInfo(
    val cameraCount: Int,
    val cameras: List<CameraDetail>
)

data class CameraDetail(
    val cameraId: String,
    val facing: LensFacing,
    val flashAvailable: Boolean,
    val sensorOrientation: Int,
    val pixelWidth: Int?,
    val pixelHeight: Int?,
    val focalLengthsMm: List<Float>,
    val aperturesFStop: List<Float>,
    val opticalStabilization: Boolean,
    val autoExposureLock: Boolean,
    val autoWhiteBalanceLock: Boolean,
    val outputFormats: List<String>
) {
    val megapixels: Double?
        get() {
            val w = pixelWidth ?: return null
            val h = pixelHeight ?: return null
            return (w.toLong() * h) / 1_000_000.0
        }
}
