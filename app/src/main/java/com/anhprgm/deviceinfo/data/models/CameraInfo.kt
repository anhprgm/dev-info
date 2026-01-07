package com.anhprgm.deviceinfo.data.models

data class CameraInfo(
    val cameraCount: Int,
    val cameras: List<CameraDetail>
)

data class CameraDetail(
    val cameraId: String,
    val facing: String,
    val flashAvailable: Boolean,
    val sensorOrientation: Int
)
