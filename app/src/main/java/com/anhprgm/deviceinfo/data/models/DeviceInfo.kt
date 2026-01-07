package com.anhprgm.deviceinfo.data.models

data class DeviceInfo(
    val deviceName: String,
    val manufacturer: String,
    val model: String,
    val brand: String,
    val androidVersion: String,
    val apiLevel: Int,
    val buildFingerprint: String,
    val securityPatch: String
)
