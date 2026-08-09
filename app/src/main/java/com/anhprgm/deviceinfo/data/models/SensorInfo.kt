package com.anhprgm.deviceinfo.data.models

data class SensorInfo(
    val sensorCount: Int,
    val sensors: List<SensorDetail>
)

data class SensorDetail(
    val name: String,
    val kind: SensorKind,
    /** Raw platform type constant, kept so [SensorKind.OTHER] can still be labelled. */
    val rawType: Int,
    val vendor: String,
    val version: Int,
    val powerMilliAmps: Float,
    val maximumRange: Float,
    val resolution: Float,
    val isWakeUpSensor: Boolean
)
