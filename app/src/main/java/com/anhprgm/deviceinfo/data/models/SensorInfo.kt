package com.anhprgm.deviceinfo.data.models

data class SensorInfo(
    val sensorCount: Int,
    val sensors: List<SensorDetail>
)

data class SensorDetail(
    val name: String,
    val type: String,
    val vendor: String,
    val power: String,
    val maxRange: String,
    val resolution: String
)
