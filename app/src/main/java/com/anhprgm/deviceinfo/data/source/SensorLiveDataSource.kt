package com.anhprgm.deviceinfo.data.source

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

data class SensorReading(
    val values: FloatArray,
    val accuracy: Int,
    val timestampNanos: Long
) {
    // FloatArray needs structural equality written out; the generated one is
    // identity-based and would make every emission look distinct.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SensorReading) return false
        return values.contentEquals(other.values) &&
            accuracy == other.accuracy &&
            timestampNanos == other.timestampNanos
    }

    override fun hashCode(): Int =
        values.contentHashCode() * 31 + accuracy * 31 + timestampNanos.hashCode()
}

@Singleton
class SensorLiveDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sensorManager: SensorManager
        get() = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    fun sensorOfType(type: Int): Sensor? = sensorManager.getDefaultSensor(type)

    /**
     * Live readings for one sensor. Unregisters automatically when collection
     * stops, so leaving the screen stops the sensor.
     *
     * SENSOR_DELAY_UI is deliberate: sampling above 200 Hz on API 31+ requires
     * the HIGH_SAMPLING_RATE_SENSORS permission, and nothing here needs it.
     */
    fun readings(
        type: Int,
        samplingPeriodUs: Int = SensorManager.SENSOR_DELAY_UI
    ): Flow<SensorReading> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(type)
        if (sensor == null) {
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                trySend(
                    SensorReading(
                        values = event.values.copyOf(),
                        accuracy = event.accuracy,
                        timestampNanos = event.timestamp
                    )
                )
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        sensorManager.registerListener(listener, sensor, samplingPeriodUs)
        awaitClose { sensorManager.unregisterListener(listener) }
    }

    /**
     * Compass heading in degrees, derived from the rotation vector rather than
     * fusing raw accelerometer and magnetometer — the platform's fused sensor
     * is both more accurate and less code.
     */
    fun heading(): Flow<Float> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (sensor == null) {
            close()
            return@callbackFlow
        }

        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientation)
                val degrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
                trySend((degrees + 360f) % 360f)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        awaitClose { sensorManager.unregisterListener(listener) }
    }
}
