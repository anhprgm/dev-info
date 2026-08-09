package com.anhprgm.deviceinfo.data.util

/**
 * Parsing of `/proc/self/stat`, kept as pure functions so it is unit-testable
 * without a device.
 *
 * We read *self*, not `/proc/stat`: since Android 8.0 SELinux denies
 * `untrusted_app` access to the system-wide file, so any system CPU figure an
 * app prints is either fabricated or zero. `/proc/self/stat` is always
 * readable and describes real work this process did.
 */
object CpuStatParser {

    data class ProcessCpuTime(val utimeTicks: Long, val stimeTicks: Long) {
        val totalTicks: Long get() = utimeTicks + stimeTicks
    }

    /**
     * Field 14 (utime) and 15 (stime) of `/proc/[pid]/stat`.
     *
     * Field 2 (`comm`) is the process name wrapped in parentheses and may itself
     * contain spaces or parentheses, so the fields are located relative to the
     * *last* ')' rather than by splitting the whole line.
     */
    fun parseProcessCpuTime(statLine: String): ProcessCpuTime? {
        val closingParen = statLine.lastIndexOf(')')
        if (closingParen < 0 || closingParen + 1 >= statLine.length) return null

        // After ')' the next token is field 3 (state), so field N sits at index N-3.
        val fields = statLine.substring(closingParen + 1).trim().split(WHITESPACE)
        val utime = fields.getOrNull(FIELD_UTIME - 3)?.toLongOrNull() ?: return null
        val stime = fields.getOrNull(FIELD_STIME - 3)?.toLongOrNull() ?: return null
        if (utime < 0 || stime < 0) return null
        return ProcessCpuTime(utime, stime)
    }

    /**
     * CPU used between two samples, as a percentage of the whole device's
     * capacity (all cores). Returns null when the inputs cannot yield a
     * meaningful figure — callers must render that as "N/A", not 0.
     */
    fun cpuPercent(
        before: ProcessCpuTime,
        after: ProcessCpuTime,
        elapsedMillis: Long,
        clockTicksPerSecond: Long,
        coreCount: Int
    ): Float? {
        if (elapsedMillis <= 0 || clockTicksPerSecond <= 0 || coreCount <= 0) return null
        val deltaTicks = after.totalTicks - before.totalTicks
        // A negative delta means the counters were not from the same process.
        if (deltaTicks < 0) return null

        val cpuSeconds = deltaTicks.toDouble() / clockTicksPerSecond
        val wallSeconds = elapsedMillis / 1000.0
        val percent = (cpuSeconds / (wallSeconds * coreCount)) * 100.0
        return percent.coerceIn(0.0, 100.0).toFloat()
    }

    /** Parses one `/proc/cpuinfo` block for a human-readable CPU name. */
    fun parseCpuModel(cpuInfoText: String, fallback: String): String {
        val keys = listOf("model name", "Processor", "Hardware", "CPU implementer")
        for (key in keys) {
            val line = cpuInfoText.lineSequence().firstOrNull { it.startsWith(key) }
            val value = line?.substringAfter(':', "")?.trim()
            if (!value.isNullOrBlank()) return value
        }
        return fallback
    }

    private const val FIELD_UTIME = 14
    private const val FIELD_STIME = 15
    private val WHITESPACE = Regex("\\s+")
}
