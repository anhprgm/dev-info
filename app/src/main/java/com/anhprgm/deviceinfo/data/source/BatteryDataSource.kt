package com.anhprgm.deviceinfo.data.source

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.anhprgm.deviceinfo.data.models.BatteryHealth
import com.anhprgm.deviceinfo.data.models.BatteryInfo
import com.anhprgm.deviceinfo.data.models.BatteryStatus
import com.anhprgm.deviceinfo.data.util.SysFs
import com.anhprgm.deviceinfo.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatteryDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val io: CoroutineDispatcher
) {
    private val batteryManager: BatteryManager
        get() = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    suspend fun getBatteryInfo(): BatteryInfo = withContext(io) {
        // ACTION_BATTERY_CHANGED is sticky, so a null receiver returns the last
        // broadcast immediately. It cannot be received by a manifest-declared
        // receiver since API 26 — runtime registration is the only option.
        val intent: Intent? = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        val rawLevel = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val level = if (rawLevel >= 0 && scale > 0) {
            (rawLevel * 100f / scale).toInt().coerceIn(0, 100)
        } else {
            null
        }

        val statusValue = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val status = when (statusValue) {
            BatteryManager.BATTERY_STATUS_CHARGING -> BatteryStatus.CHARGING
            BatteryManager.BATTERY_STATUS_DISCHARGING -> BatteryStatus.DISCHARGING
            BatteryManager.BATTERY_STATUS_FULL -> BatteryStatus.FULL
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> BatteryStatus.NOT_CHARGING
            else -> BatteryStatus.UNKNOWN
        }

        val healthValue = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        val health = when (healthValue) {
            BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealth.GOOD
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> BatteryHealth.OVERHEAT
            BatteryManager.BATTERY_HEALTH_DEAD -> BatteryHealth.DEAD
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealth.OVER_VOLTAGE
            BatteryManager.BATTERY_HEALTH_COLD -> BatteryHealth.COLD
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> BatteryHealth.UNSPECIFIED_FAILURE
            else -> BatteryHealth.UNKNOWN
        }

        // EXTRA_TEMPERATURE is tenths of a degree Celsius; EXTRA_VOLTAGE is millivolts.
        val temperature = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
            ?.takeIf { it != Int.MIN_VALUE && it > -500 }
            ?.let { it / 10f }

        val voltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
            ?.takeIf { it > 0 }
            ?.let { it / 1000f }

        BatteryInfo(
            level = level,
            status = status,
            health = health,
            temperatureCelsius = temperature,
            voltageVolts = voltage,
            technology = intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY),
            estimatedCapacityMah = estimateCapacityMah(level),
            chargeCycles = SysFs.batteryChargeCycles(),
            isCharging = status == BatteryStatus.CHARGING || status == BatteryStatus.FULL
        )
    }

    /**
     * Derived from the remaining charge counter and the current level, so it
     * approximates *full* capacity — not the manufacturer's design capacity,
     * which no public API exposes.
     */
    private fun estimateCapacityMah(level: Int?): Int? {
        if (level == null || level <= 0) return null
        val chargeCounterMicroAh = try {
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        } catch (_: Exception) {
            return null
        }
        if (chargeCounterMicroAh <= 0) return null
        return (chargeCounterMicroAh / 1000.0 / level * 100).toInt().takeIf { it > 0 }
    }
}
