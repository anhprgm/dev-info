package com.anhprgm.deviceinfo.data.models

data class HardwareInfo(
    val totalRam: String,
    val availableRam: String,
    val totalStorage: String,
    val availableStorage: String,
    val cpuInfo: String,
    val cpuCores: Int
)
