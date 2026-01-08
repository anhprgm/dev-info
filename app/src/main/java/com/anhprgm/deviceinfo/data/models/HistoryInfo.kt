package com.anhprgm.deviceinfo.data.models

data class HistoryData(
    val timestamp: Long,
    val batteryLevel: Int,
    val availableRam: Long,
    val cpuUsage: Float
)

data class HistoryInfo(
    val history: List<HistoryData>
)
