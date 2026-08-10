package com.anhprgm.deviceinfo.data.source

import android.content.Context
import android.os.Build
import android.os.PowerManager
import com.anhprgm.deviceinfo.data.models.ThermalInfo
import com.anhprgm.deviceinfo.data.models.ThermalStatus
import com.anhprgm.deviceinfo.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thermal readings, with honest availability.
 *
 * Per-component CPU/GPU temperatures are NOT obtainable:
 * HardwarePropertiesManager needs the signature-level DEVICE_POWER permission,
 * and /sys/class/thermal is blocked by SELinux on most devices. Battery
 * temperature is the only universally reliable reading.
 */
@Singleton
class ThermalDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val batteryDataSource: BatteryDataSource,
    @IoDispatcher private val io: CoroutineDispatcher
) {
    suspend fun getThermalInfo(): ThermalInfo = withContext(io) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val battery = batteryDataSource.getBatteryInfo()

        val status = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runCatching { statusOf(powerManager.currentThermalStatus) }.getOrNull()
        } else {
            null
        }

        val headroom = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            runCatching { powerManager.getThermalHeadroom(HEADROOM_FORECAST_SECONDS) }
                .getOrNull()
                // Devices without a thermal HAL return NaN.
                ?.takeIf { !it.isNaN() && it.isFinite() }
        } else {
            null
        }

        ThermalInfo(
            batteryTemperatureCelsius = battery.temperatureCelsius,
            thermalStatus = status,
            thermalHeadroom = headroom,
            requiresApiForStatus = Build.VERSION_CODES.Q
                .takeIf { Build.VERSION.SDK_INT < it },
            requiresApiForHeadroom = Build.VERSION_CODES.R
                .takeIf { Build.VERSION.SDK_INT < it }
        )
    }

    private fun statusOf(value: Int): ThermalStatus = when (value) {
        PowerManager.THERMAL_STATUS_NONE -> ThermalStatus.NONE
        PowerManager.THERMAL_STATUS_LIGHT -> ThermalStatus.LIGHT
        PowerManager.THERMAL_STATUS_MODERATE -> ThermalStatus.MODERATE
        PowerManager.THERMAL_STATUS_SEVERE -> ThermalStatus.SEVERE
        PowerManager.THERMAL_STATUS_CRITICAL -> ThermalStatus.CRITICAL
        PowerManager.THERMAL_STATUS_EMERGENCY -> ThermalStatus.EMERGENCY
        PowerManager.THERMAL_STATUS_SHUTDOWN -> ThermalStatus.SHUTDOWN
        else -> ThermalStatus.UNKNOWN
    }

    private companion object {
        const val HEADROOM_FORECAST_SECONDS = 60
    }
}
