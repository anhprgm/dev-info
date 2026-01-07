package com.anhprgm.deviceinfo.data.models

data class NetworkInfo(
    val connectionType: String,
    val networkName: String,
    val ipAddress: String,
    val signalStrength: String
)
