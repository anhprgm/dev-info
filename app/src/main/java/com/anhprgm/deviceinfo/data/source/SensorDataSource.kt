package com.anhprgm.deviceinfo.data.source

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import com.anhprgm.deviceinfo.data.models.SensorDetail
import com.anhprgm.deviceinfo.data.models.SensorInfo
import com.anhprgm.deviceinfo.data.models.SensorKind
import com.anhprgm.deviceinfo.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val io: CoroutineDispatcher
) {
    private val sensorManager: SensorManager
        get() = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    suspend fun getSensorInfo(): SensorInfo = withContext(io) {
        val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL).map { sensor ->
            SensorDetail(
                name = sensor.name,
                kind = kindOf(sensor.type),
                rawType = sensor.type,
                vendor = sensor.vendor,
                version = sensor.version,
                powerMilliAmps = sensor.power,
                maximumRange = sensor.maximumRange,
                resolution = sensor.resolution,
                isWakeUpSensor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                    sensor.isWakeUpSensor
            )
        }
        SensorInfo(sensorCount = sensors.size, sensors = sensors)
    }

    private fun kindOf(type: Int): SensorKind = when (type) {
        Sensor.TYPE_ACCELEROMETER -> SensorKind.ACCELEROMETER
        Sensor.TYPE_GYROSCOPE -> SensorKind.GYROSCOPE
        Sensor.TYPE_LIGHT -> SensorKind.LIGHT
        Sensor.TYPE_PROXIMITY -> SensorKind.PROXIMITY
        Sensor.TYPE_MAGNETIC_FIELD -> SensorKind.MAGNETIC_FIELD
        Sensor.TYPE_PRESSURE -> SensorKind.PRESSURE
        // TYPE_TEMPERATURE is deprecated; TYPE_AMBIENT_TEMPERATURE replaced it.
        Sensor.TYPE_AMBIENT_TEMPERATURE -> SensorKind.AMBIENT_TEMPERATURE
        Sensor.TYPE_RELATIVE_HUMIDITY -> SensorKind.RELATIVE_HUMIDITY
        Sensor.TYPE_GRAVITY -> SensorKind.GRAVITY
        Sensor.TYPE_LINEAR_ACCELERATION -> SensorKind.LINEAR_ACCELERATION
        Sensor.TYPE_ROTATION_VECTOR -> SensorKind.ROTATION_VECTOR
        Sensor.TYPE_STEP_COUNTER -> SensorKind.STEP_COUNTER
        Sensor.TYPE_STEP_DETECTOR -> SensorKind.STEP_DETECTOR
        Sensor.TYPE_HEART_RATE -> SensorKind.HEART_RATE
        else -> SensorKind.OTHER
    }
}
