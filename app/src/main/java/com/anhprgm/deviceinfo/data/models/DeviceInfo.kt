package com.anhprgm.deviceinfo.data.models

data class DeviceInfo(
    val deviceName: String,
    val manufacturer: String,
    val model: String,
    val brand: String,
    val device: String,
    val product: String,
    val board: String,
    val hardware: String,
    val androidVersion: String,
    val apiLevel: Int,
    val buildFingerprint: String,
    val buildId: String,
    val bootloader: String,
    /** Null below API 23, where [android.os.Build.VERSION.SECURITY_PATCH] does not exist. */
    val securityPatch: String?,
    val supportedAbis: List<String>,
    /** Milliseconds since boot, including deep sleep. */
    val uptimeMillis: Long
)
