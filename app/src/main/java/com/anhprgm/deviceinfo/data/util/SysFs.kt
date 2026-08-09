package com.anhprgm.deviceinfo.data.util

import java.io.File

/**
 * Best-effort reads of kernel pseudo-files.
 *
 * Every one of these can be denied by SELinux depending on device and OS
 * version, so each returns null rather than throwing or substituting a
 * placeholder value.
 */
object SysFs {

    fun readText(path: String): String? = try {
        val file = File(path)
        if (file.canRead()) file.readText().trim().takeIf { it.isNotEmpty() } else null
    } catch (_: Exception) {
        null
    }

    fun readLong(path: String): Long? = readText(path)?.toLongOrNull()

    fun readInt(path: String): Int? = readText(path)?.toIntOrNull()

    /**
     * Highest `cpuinfo_max_freq` across all cores, in kHz.
     * Big.LITTLE devices report different values per core; the peak is the
     * number users expect to see quoted.
     */
    fun maxCpuFrequencyKhz(): Long? {
        val values = (0 until MAX_CORES_PROBED).mapNotNull { core ->
            readLong("/sys/devices/system/cpu/cpu$core/cpufreq/cpuinfo_max_freq")
        }
        return values.maxOrNull()
    }

    /** Battery charge cycles. Present on a minority of devices. */
    fun batteryChargeCycles(): Int? = CYCLE_COUNT_PATHS.firstNotNullOfOrNull { readInt(it) }
        ?.takeIf { it > 0 }

    private const val MAX_CORES_PROBED = 32

    private val CYCLE_COUNT_PATHS = listOf(
        "/sys/class/power_supply/battery/cycle_count",
        "/sys/class/power_supply/bms/cycle_count"
    )
}
