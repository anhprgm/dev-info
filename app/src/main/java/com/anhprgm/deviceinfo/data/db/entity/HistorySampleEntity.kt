package com.anhprgm.deviceinfo.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.anhprgm.deviceinfo.data.models.HistorySample

@Entity(
    tableName = "history_sample",
    indices = [Index(value = ["timestamp"])]
)
data class HistorySampleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val batteryLevel: Int,
    val availableRamBytes: Long,
    val totalRamBytes: Long,
    /** Nullable: "could not measure" is not the same as 0%. */
    val cpuPercent: Float?,
    val batteryTemperatureCelsius: Float?
) {
    fun toDomain() = HistorySample(
        timestamp = timestamp,
        batteryLevel = batteryLevel,
        availableRamBytes = availableRamBytes,
        totalRamBytes = totalRamBytes,
        cpuPercent = cpuPercent,
        batteryTemperatureCelsius = batteryTemperatureCelsius
    )

    companion object {
        fun fromDomain(sample: HistorySample) = HistorySampleEntity(
            timestamp = sample.timestamp,
            batteryLevel = sample.batteryLevel,
            availableRamBytes = sample.availableRamBytes,
            totalRamBytes = sample.totalRamBytes,
            cpuPercent = sample.cpuPercent,
            batteryTemperatureCelsius = sample.batteryTemperatureCelsius
        )
    }
}
