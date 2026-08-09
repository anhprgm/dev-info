package com.anhprgm.deviceinfo.ui.format

import com.anhprgm.deviceinfo.data.models.BatteryHealth
import com.anhprgm.deviceinfo.data.models.BatteryStatus
import com.anhprgm.deviceinfo.data.models.ConnectionType
import com.anhprgm.deviceinfo.data.models.DensityBucket
import com.anhprgm.deviceinfo.data.models.LensFacing
import com.anhprgm.deviceinfo.data.models.SensorKind

/**
 * Display wording for the categorical model enums.
 *
 * These are plain literals for now; Phase 2 replaces each body with a
 * `stringResource` lookup so the strings become translatable. Keeping the
 * mapping in one file means that swap touches one place rather than 14 screens.
 */
object Labels {

    fun of(status: BatteryStatus): String = when (status) {
        BatteryStatus.CHARGING -> "Charging"
        BatteryStatus.DISCHARGING -> "Discharging"
        BatteryStatus.FULL -> "Full"
        BatteryStatus.NOT_CHARGING -> "Not charging"
        BatteryStatus.UNKNOWN -> "Unknown"
    }

    fun of(health: BatteryHealth): String = when (health) {
        BatteryHealth.GOOD -> "Good"
        BatteryHealth.OVERHEAT -> "Overheating"
        BatteryHealth.DEAD -> "Dead"
        BatteryHealth.OVER_VOLTAGE -> "Over voltage"
        BatteryHealth.COLD -> "Cold"
        BatteryHealth.UNSPECIFIED_FAILURE -> "Failure"
        BatteryHealth.UNKNOWN -> "Unknown"
    }

    fun of(type: ConnectionType): String = when (type) {
        ConnectionType.WIFI -> "Wi-Fi"
        ConnectionType.CELLULAR -> "Mobile data"
        ConnectionType.ETHERNET -> "Ethernet"
        ConnectionType.VPN -> "VPN"
        ConnectionType.BLUETOOTH -> "Bluetooth"
        ConnectionType.DISCONNECTED -> "Disconnected"
        ConnectionType.UNKNOWN -> "Unknown"
    }

    fun of(facing: LensFacing): String = when (facing) {
        LensFacing.FRONT -> "Front"
        LensFacing.BACK -> "Back"
        LensFacing.EXTERNAL -> "External"
        LensFacing.UNKNOWN -> "Unknown"
    }

    fun of(bucket: DensityBucket): String = when (bucket) {
        DensityBucket.LDPI -> "ldpi"
        DensityBucket.MDPI -> "mdpi"
        DensityBucket.HDPI -> "hdpi"
        DensityBucket.XHDPI -> "xhdpi"
        DensityBucket.XXHDPI -> "xxhdpi"
        DensityBucket.XXXHDPI -> "xxxhdpi"
    }

    fun of(kind: SensorKind, rawType: Int): String = when (kind) {
        SensorKind.ACCELEROMETER -> "Accelerometer"
        SensorKind.GYROSCOPE -> "Gyroscope"
        SensorKind.LIGHT -> "Light"
        SensorKind.PROXIMITY -> "Proximity"
        SensorKind.MAGNETIC_FIELD -> "Magnetic field"
        SensorKind.PRESSURE -> "Pressure"
        SensorKind.AMBIENT_TEMPERATURE -> "Ambient temperature"
        SensorKind.RELATIVE_HUMIDITY -> "Relative humidity"
        SensorKind.GRAVITY -> "Gravity"
        SensorKind.LINEAR_ACCELERATION -> "Linear acceleration"
        SensorKind.ROTATION_VECTOR -> "Rotation vector"
        SensorKind.STEP_COUNTER -> "Step counter"
        SensorKind.STEP_DETECTOR -> "Step detector"
        SensorKind.HEART_RATE -> "Heart rate"
        SensorKind.OTHER -> "Other (type $rawType)"
    }

    fun yesNo(value: Boolean): String = if (value) "Yes" else "No"

    fun supported(value: Boolean): String = if (value) "Supported" else "Not supported"
}
