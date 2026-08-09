package com.anhprgm.deviceinfo.data.models

data class MonitoringInfo(
    /**
     * CPU used by *this app's process*, not the whole system.
     *
     * System-wide CPU is not obtainable: Android 8.0 tightened SELinux so
     * `untrusted_app` cannot read /proc/stat. The old code swallowed that
     * permission error and returned 0f, so the monitor showed 0.0% on every
     * modern device. Null here means "could not measure" and must render as
     * N/A — never as zero.
     */
    val appCpuPercent: Float?,
    val ramUsedBytes: Long,
    val ramTotalBytes: Long,
    val ramAvailableBytes: Long,
    val storageUsedBytes: Long,
    val storageTotalBytes: Long,
    val batteryLevel: Int?,
    val batteryTemperatureCelsius: Float?,
    val timestamp: Long
) {
    val ramUsagePercent: Float
        get() = if (ramTotalBytes > 0) ramUsedBytes * 100f / ramTotalBytes else 0f

    val storageUsagePercent: Float
        get() = if (storageTotalBytes > 0) storageUsedBytes * 100f / storageTotalBytes else 0f
}
