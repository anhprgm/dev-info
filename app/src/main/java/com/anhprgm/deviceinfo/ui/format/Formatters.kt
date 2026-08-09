package com.anhprgm.deviceinfo.ui.format

import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * All presentation of raw values lives here.
 *
 * Models hold typed values (Long bytes, Float celsius); nothing in `data/`
 * formats for display. That split exists because the previous design stored
 * pre-formatted strings and then parsed them back — under a comma-decimal
 * locale like Vietnamese, `String.format("%.2f GB")` produces "3,45 GB" and
 * `"3,45".toFloatOrNull()` returns null.
 *
 * Rule: [Locale.getDefault] for anything a user reads, [Locale.ROOT] for
 * anything that gets serialized (JSON, CSV, database, share text).
 */
object Formatters {

    const val NOT_AVAILABLE = "N/A"

    private const val KB = 1024.0
    private const val MB = KB * 1024
    private const val GB = MB * 1024
    private const val TB = GB * 1024

    /** Human-readable byte size, e.g. "3.45 GB". Null renders as "N/A". */
    fun bytes(value: Long?, locale: Locale = Locale.getDefault()): String {
        if (value == null || value < 0) return NOT_AVAILABLE
        return when {
            value >= TB -> String.format(locale, "%.2f TB", value / TB)
            value >= GB -> String.format(locale, "%.2f GB", value / GB)
            value >= MB -> String.format(locale, "%.2f MB", value / MB)
            value >= KB -> String.format(locale, "%.2f KB", value / KB)
            else -> String.format(locale, "%d B", value)
        }
    }

    /** Byte size for serialization — always ROOT so it round-trips. */
    fun bytesRoot(value: Long?): String = bytes(value, Locale.ROOT)

    fun percent(value: Float?, decimals: Int = 1, locale: Locale = Locale.getDefault()): String {
        if (value == null || value.isNaN()) return NOT_AVAILABLE
        return String.format(locale, "%.${decimals}f%%", value)
    }

    fun percentInt(value: Int?, locale: Locale = Locale.getDefault()): String {
        if (value == null || value < 0) return NOT_AVAILABLE
        return String.format(locale, "%d%%", value)
    }

    fun celsius(value: Float?, locale: Locale = Locale.getDefault()): String {
        if (value == null || value.isNaN()) return NOT_AVAILABLE
        return String.format(locale, "%.1f°C", value)
    }

    fun volts(value: Float?, locale: Locale = Locale.getDefault()): String {
        if (value == null || value.isNaN()) return NOT_AVAILABLE
        return String.format(locale, "%.2f V", value)
    }

    fun milliAmpHours(value: Int?, locale: Locale = Locale.getDefault()): String {
        if (value == null || value <= 0) return NOT_AVAILABLE
        return String.format(locale, "%d mAh", value)
    }

    fun milliAmps(value: Float?, locale: Locale = Locale.getDefault()): String {
        if (value == null || value.isNaN()) return NOT_AVAILABLE
        return String.format(locale, "%.2f mA", value)
    }

    fun megapixels(value: Double?, locale: Locale = Locale.getDefault()): String {
        if (value == null || value.isNaN()) return NOT_AVAILABLE
        return String.format(locale, "%.1f MP", value)
    }

    fun millimetres(value: Float?, locale: Locale = Locale.getDefault()): String {
        if (value == null || value.isNaN()) return NOT_AVAILABLE
        return String.format(locale, "%.2f mm", value)
    }

    fun aperture(value: Float?, locale: Locale = Locale.getDefault()): String {
        if (value == null || value.isNaN()) return NOT_AVAILABLE
        return String.format(locale, "f/%.1f", value)
    }

    fun inches(value: Float?, locale: Locale = Locale.getDefault()): String {
        if (value == null || value.isNaN()) return NOT_AVAILABLE
        return String.format(locale, "%.2f\"", value)
    }

    fun hertz(value: Float?, locale: Locale = Locale.getDefault()): String {
        if (value == null || value.isNaN()) return NOT_AVAILABLE
        return String.format(locale, "%.0f Hz", value)
    }

    /** CPU clock speed given in kHz (the unit `/sys/.../cpuinfo_max_freq` uses). */
    fun megahertzFromKhz(khz: Long?, locale: Locale = Locale.getDefault()): String {
        if (khz == null || khz <= 0) return NOT_AVAILABLE
        val ghz = khz / 1_000_000.0
        return if (ghz >= 1.0) String.format(locale, "%.2f GHz", ghz)
        else String.format(locale, "%d MHz", khz / 1000)
    }

    fun decimal(value: Float?, decimals: Int = 2, locale: Locale = Locale.getDefault()): String {
        if (value == null || value.isNaN()) return NOT_AVAILABLE
        return String.format(locale, "%.${decimals}f", value)
    }

    fun count(value: Int?, unit: String, locale: Locale = Locale.getDefault()): String {
        if (value == null || value < 0) return NOT_AVAILABLE
        return String.format(locale, "%d %s", value, unit)
    }

    /** Uptime as "2d 4h 13m" / "4h 13m" / "13m". */
    fun duration(millis: Long?, locale: Locale = Locale.getDefault()): String {
        if (millis == null || millis < 0) return NOT_AVAILABLE
        val days = TimeUnit.MILLISECONDS.toDays(millis)
        val hours = TimeUnit.MILLISECONDS.toHours(millis) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        return when {
            days > 0 -> String.format(locale, "%dd %dh %dm", days, hours, minutes)
            hours > 0 -> String.format(locale, "%dh %dm", hours, minutes)
            else -> String.format(locale, "%dm", minutes)
        }
    }

    /** Benchmark wall time — sub-second results read better in ms. */
    fun elapsed(millis: Long?, locale: Locale = Locale.getDefault()): String {
        if (millis == null || millis < 0) return NOT_AVAILABLE
        return if (millis >= 1000) String.format(locale, "%.2f s", millis / 1000.0)
        else String.format(locale, "%d ms", millis)
    }

    fun dbm(value: Int?, locale: Locale = Locale.getDefault()): String {
        if (value == null) return NOT_AVAILABLE
        return String.format(locale, "%d dBm", value)
    }

    fun resolution(width: Int?, height: Int?, locale: Locale = Locale.getDefault()): String {
        if (width == null || height == null || width <= 0 || height <= 0) return NOT_AVAILABLE
        return String.format(locale, "%d × %d", width, height)
    }

    /** Falls back to "N/A" for null/blank so screens never print "null". */
    fun text(value: String?): String = value?.takeIf { it.isNotBlank() } ?: NOT_AVAILABLE
}
