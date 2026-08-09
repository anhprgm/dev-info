package com.anhprgm.deviceinfo.data.models

/**
 * One sampled point of device state.
 *
 * [cpuPercent] and [batteryTemperatureCelsius] are nullable on purpose: the
 * old CSV store coerced unavailable readings to 0f, and the history chart then
 * plotted those zeros as if they were real measurements.
 */
data class HistorySample(
    val timestamp: Long,
    val batteryLevel: Int,
    val availableRamBytes: Long,
    val totalRamBytes: Long,
    val cpuPercent: Float?,
    val batteryTemperatureCelsius: Float?
)

data class HistoryInfo(
    val samples: List<HistorySample>
)
