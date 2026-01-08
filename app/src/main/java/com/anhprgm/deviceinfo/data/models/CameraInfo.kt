package com.anhprgm.deviceinfo.data.models

data class CameraInfo(
    val cameraCount: Int,
    val cameras: List<CameraDetail>
)

data class CameraDetail(
    val cameraId: String,
    val facing: String,
    val flashAvailable: Boolean,
    val sensorOrientation: Int,
    val megapixels: String,
    val focalLength: String,
    val aperture: String,
    val opticalStabilization: Boolean,
    val autoExposureLock: Boolean,
    val autoWhiteBalanceLock: Boolean,
    val outputFormats: String,
    val imageSize: String
)
