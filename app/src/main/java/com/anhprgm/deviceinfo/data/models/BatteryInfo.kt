package com.anhprgm.deviceinfo.data.models

data class BatteryInfo(
    val level: Int,
    val chargingStatus: String,
    val temperature: String,
    val voltage: String,
    val health: String,
    val technology: String
)
