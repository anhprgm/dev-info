package com.anhprgm.deviceinfo.ui.format

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.anhprgm.deviceinfo.R
import com.anhprgm.deviceinfo.data.models.BatteryHealth
import com.anhprgm.deviceinfo.data.models.BatteryStatus
import com.anhprgm.deviceinfo.data.models.ConnectionType
import com.anhprgm.deviceinfo.data.models.DensityBucket
import com.anhprgm.deviceinfo.data.models.LensFacing
import com.anhprgm.deviceinfo.data.models.SensorKind

/**
 * Maps model enums to string resources.
 *
 * Returning `@StringRes Int` rather than a resolved String keeps these usable
 * from outside composition (report export, widget) while still being
 * translatable — the enums themselves stay in `data/` with no wording in them.
 */
object Labels {

    @StringRes
    fun resOf(status: BatteryStatus): Int = when (status) {
        BatteryStatus.CHARGING -> R.string.battery_status_charging
        BatteryStatus.DISCHARGING -> R.string.battery_status_discharging
        BatteryStatus.FULL -> R.string.battery_status_full
        BatteryStatus.NOT_CHARGING -> R.string.battery_status_not_charging
        BatteryStatus.UNKNOWN -> R.string.common_unknown
    }

    @StringRes
    fun resOf(health: BatteryHealth): Int = when (health) {
        BatteryHealth.GOOD -> R.string.battery_health_good
        BatteryHealth.OVERHEAT -> R.string.battery_health_overheat
        BatteryHealth.DEAD -> R.string.battery_health_dead
        BatteryHealth.OVER_VOLTAGE -> R.string.battery_health_over_voltage
        BatteryHealth.COLD -> R.string.battery_health_cold
        BatteryHealth.UNSPECIFIED_FAILURE -> R.string.battery_health_failure
        BatteryHealth.UNKNOWN -> R.string.common_unknown
    }

    @StringRes
    fun resOf(type: ConnectionType): Int = when (type) {
        ConnectionType.WIFI -> R.string.connection_wifi
        ConnectionType.CELLULAR -> R.string.connection_cellular
        ConnectionType.ETHERNET -> R.string.connection_ethernet
        ConnectionType.VPN -> R.string.connection_vpn
        ConnectionType.BLUETOOTH -> R.string.connection_bluetooth
        ConnectionType.DISCONNECTED -> R.string.connection_disconnected
        ConnectionType.UNKNOWN -> R.string.common_unknown
    }

    @StringRes
    fun resOf(facing: LensFacing): Int = when (facing) {
        LensFacing.FRONT -> R.string.lens_front
        LensFacing.BACK -> R.string.lens_back
        LensFacing.EXTERNAL -> R.string.lens_external
        LensFacing.UNKNOWN -> R.string.common_unknown
    }

    @StringRes
    fun resOf(kind: SensorKind): Int = when (kind) {
        SensorKind.ACCELEROMETER -> R.string.sensor_accelerometer
        SensorKind.GYROSCOPE -> R.string.sensor_gyroscope
        SensorKind.LIGHT -> R.string.sensor_light
        SensorKind.PROXIMITY -> R.string.sensor_proximity
        SensorKind.MAGNETIC_FIELD -> R.string.sensor_magnetic_field
        SensorKind.PRESSURE -> R.string.sensor_pressure
        SensorKind.AMBIENT_TEMPERATURE -> R.string.sensor_ambient_temperature
        SensorKind.RELATIVE_HUMIDITY -> R.string.sensor_relative_humidity
        SensorKind.GRAVITY -> R.string.sensor_gravity
        SensorKind.LINEAR_ACCELERATION -> R.string.sensor_linear_acceleration
        SensorKind.ROTATION_VECTOR -> R.string.sensor_rotation_vector
        SensorKind.STEP_COUNTER -> R.string.sensor_step_counter
        SensorKind.STEP_DETECTOR -> R.string.sensor_step_detector
        SensorKind.HEART_RATE -> R.string.sensor_heart_rate
        SensorKind.OTHER -> R.string.sensor_other
    }

    /** Density buckets are platform identifiers, not prose — never translated. */
    fun of(bucket: DensityBucket): String = when (bucket) {
        DensityBucket.LDPI -> "ldpi"
        DensityBucket.MDPI -> "mdpi"
        DensityBucket.HDPI -> "hdpi"
        DensityBucket.XHDPI -> "xhdpi"
        DensityBucket.XXHDPI -> "xxhdpi"
        DensityBucket.XXXHDPI -> "xxxhdpi"
    }
}

// ---- Composable conveniences ----------------------------------------------

@Composable fun BatteryStatus.label(): String = stringResource(Labels.resOf(this))

@Composable fun BatteryHealth.label(): String = stringResource(Labels.resOf(this))

@Composable fun ConnectionType.label(): String = stringResource(Labels.resOf(this))

@Composable fun LensFacing.label(): String = stringResource(Labels.resOf(this))

/** [SensorKind.OTHER] carries the raw platform constant into its label. */
@Composable
fun SensorKind.label(rawType: Int): String =
    if (this == SensorKind.OTHER) stringResource(R.string.sensor_other, rawType)
    else stringResource(Labels.resOf(this))

@Composable fun Boolean.yesNo(): String =
    stringResource(if (this) R.string.common_yes else R.string.common_no)

@Composable fun Boolean.supported(): String =
    stringResource(if (this) R.string.common_supported else R.string.common_not_supported)
