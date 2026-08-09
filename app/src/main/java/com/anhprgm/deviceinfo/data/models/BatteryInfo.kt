package com.anhprgm.deviceinfo.data.models

data class BatteryInfo(
    /** 0..100, or null when the platform reports an invalid level/scale. */
    val level: Int?,
    val status: BatteryStatus,
    val health: BatteryHealth,
    val temperatureCelsius: Float?,
    val voltageVolts: Float?,
    val technology: String?,
    /** Estimated from the charge counter — not the design capacity. */
    val estimatedCapacityMah: Int?,
    /** Read from sysfs; unavailable on most devices. */
    val chargeCycles: Int?,
    val isCharging: Boolean
)
