package com.anhprgm.deviceinfo.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.anhprgm.deviceinfo.data.models.BenchmarkResult

@Entity(tableName = "benchmark_result")
data class BenchmarkResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val singleCoreMillis: Long,
    val multiCoreMillis: Long,
    val memoryMillis: Long,
    val coresUsed: Int,
    val totalDurationMillis: Long
) {
    fun toDomain() = BenchmarkResult(
        singleCoreMillis = singleCoreMillis,
        multiCoreMillis = multiCoreMillis,
        memoryMillis = memoryMillis,
        coresUsed = coresUsed,
        totalDurationMillis = totalDurationMillis,
        timestamp = timestamp
    )

    companion object {
        fun fromDomain(result: BenchmarkResult) = BenchmarkResultEntity(
            timestamp = result.timestamp,
            singleCoreMillis = result.singleCoreMillis,
            multiCoreMillis = result.multiCoreMillis,
            memoryMillis = result.memoryMillis,
            coresUsed = result.coresUsed,
            totalDurationMillis = result.totalDurationMillis
        )
    }
}
