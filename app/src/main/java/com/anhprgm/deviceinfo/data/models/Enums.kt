package com.anhprgm.deviceinfo.data.models

/**
 * Categorical values are modelled as enums rather than display strings so the
 * UI layer owns the wording. Phase 2 maps each constant to a string resource;
 * keeping them as English literals in `data/` would make that untranslatable.
 */

enum class BatteryStatus { CHARGING, DISCHARGING, FULL, NOT_CHARGING, UNKNOWN }

enum class BatteryHealth { GOOD, OVERHEAT, DEAD, OVER_VOLTAGE, COLD, UNSPECIFIED_FAILURE, UNKNOWN }

enum class ConnectionType { WIFI, CELLULAR, ETHERNET, VPN, BLUETOOTH, DISCONNECTED, UNKNOWN }

enum class LensFacing { FRONT, BACK, EXTERNAL, UNKNOWN }

enum class DensityBucket { LDPI, MDPI, HDPI, XHDPI, XXHDPI, XXXHDPI }

/** Known sensor types; [OTHER] carries the raw platform constant. */
enum class SensorKind {
    ACCELEROMETER, GYROSCOPE, LIGHT, PROXIMITY, MAGNETIC_FIELD, PRESSURE,
    AMBIENT_TEMPERATURE, RELATIVE_HUMIDITY, GRAVITY, LINEAR_ACCELERATION,
    ROTATION_VECTOR, STEP_COUNTER, STEP_DETECTOR, HEART_RATE, OTHER
}

/**
 * Why a value is missing. The previous code collapsed "unsupported", "no
 * permission" and "genuinely zero" all into `0` or the string "N/A", which is
 * what makes the app look broken rather than honest.
 */
sealed interface Availability<out T> {
    data class Available<T>(val value: T) : Availability<T>

    /** Needs a newer platform than this device runs. */
    data class RequiresApi(val apiLevel: Int) : Availability<Nothing>

    /** Needs a runtime permission the user has not granted. */
    data class RequiresPermission(val permission: String) : Availability<Nothing>

    /** Hardware or OS simply does not expose it (e.g. SELinux-blocked). */
    data object Unsupported : Availability<Nothing>

    companion object {
        fun <T : Any> ofNullable(value: T?): Availability<T> =
            if (value == null) Unsupported else Available(value)
    }
}

fun <T> Availability<T>.valueOrNull(): T? =
    (this as? Availability.Available)?.value
