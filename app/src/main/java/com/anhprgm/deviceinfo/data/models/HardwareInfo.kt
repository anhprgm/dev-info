package com.anhprgm.deviceinfo.data.models

data class HardwareInfo(
    val totalRamBytes: Long,
    val availableRamBytes: Long,
    val totalStorageBytes: Long,
    val availableStorageBytes: Long,
    val cpuModel: String,
    val cpuCores: Int,
    /** Peak clock in kHz, per `/sys/.../cpuinfo_max_freq`. Null when SELinux blocks the read. */
    val cpuMaxFrequencyKhz: Long?,
    val supportedAbis: List<String>
) {
    val usedRamBytes: Long get() = totalRamBytes - availableRamBytes
    val usedStorageBytes: Long get() = totalStorageBytes - availableStorageBytes

    val ramUsagePercent: Float
        get() = if (totalRamBytes > 0) usedRamBytes * 100f / totalRamBytes else 0f

    val storageUsagePercent: Float
        get() = if (totalStorageBytes > 0) usedStorageBytes * 100f / totalStorageBytes else 0f
}
