package com.anhprgm.deviceinfo.data.models

data class MonitoringInfo(
    val cpuUsage: Float,
    val ramUsage: Float,
    val ramUsed: String,
    val ramTotal: String,
    val timestamp: Long
)
