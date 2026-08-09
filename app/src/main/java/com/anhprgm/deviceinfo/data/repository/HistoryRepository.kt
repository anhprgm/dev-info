package com.anhprgm.deviceinfo.data.repository

import android.content.Context
import com.anhprgm.deviceinfo.data.db.dao.BenchmarkDao
import com.anhprgm.deviceinfo.data.db.dao.HistoryDao
import com.anhprgm.deviceinfo.data.db.entity.BenchmarkResultEntity
import com.anhprgm.deviceinfo.data.db.entity.HistorySampleEntity
import com.anhprgm.deviceinfo.data.migration.LegacyCsvImporter
import com.anhprgm.deviceinfo.data.models.BenchmarkResult
import com.anhprgm.deviceinfo.data.models.HistorySample
import com.anhprgm.deviceinfo.data.models.MonitoringInfo
import com.anhprgm.deviceinfo.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val historyDao: HistoryDao,
    private val benchmarkDao: BenchmarkDao,
    @IoDispatcher private val io: CoroutineDispatcher
) {
    private val migrationLock = Mutex()
    private var migrationChecked = false

    fun observeHistory(): Flow<List<HistorySample>> =
        historyDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    fun observeHistorySince(since: Long): Flow<List<HistorySample>> =
        historyDao.observeSince(since).map { rows -> rows.map { it.toDomain() } }

    fun observeBenchmarks(): Flow<List<BenchmarkResult>> =
        benchmarkDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    suspend fun latestBenchmark(): BenchmarkResult? = withContext(io) {
        benchmarkDao.latest()?.toDomain()
    }

    suspend fun recordBenchmark(result: BenchmarkResult) = withContext(io) {
        benchmarkDao.insert(BenchmarkResultEntity.fromDomain(result))
        Unit
    }

    /** Persists one monitoring snapshot as a history point. */
    suspend fun record(monitoring: MonitoringInfo) = withContext(io) {
        val level = monitoring.batteryLevel ?: return@withContext
        historyDao.insert(
            HistorySampleEntity(
                timestamp = monitoring.timestamp,
                batteryLevel = level,
                availableRamBytes = monitoring.ramAvailableBytes,
                totalRamBytes = monitoring.ramTotalBytes,
                cpuPercent = monitoring.appCpuPercent,
                batteryTemperatureCelsius = monitoring.batteryTemperatureCelsius
            )
        )
        historyDao.deleteOlderThan(System.currentTimeMillis() - RETENTION_MILLIS)
        Unit
    }

    suspend fun clearHistory() = withContext(io) {
        historyDao.clear()
    }

    /**
     * Imports the legacy CSV once. Guarded by a mutex because the worker and
     * the UI can both reach this on first launch — the file-based store it
     * replaces had no such protection and would interleave writes.
     */
    suspend fun migrateLegacyDataIfNeeded() = withContext(io) {
        migrationLock.withLock {
            if (migrationChecked) return@withContext
            migrationChecked = true

            val legacy = LegacyCsvImporter.readLegacyFile(context.filesDir)
            if (legacy.isNotEmpty()) {
                historyDao.insertAll(legacy)
            }
            LegacyCsvImporter.markImported(context.filesDir)
        }
    }

    private companion object {
        /** Keep a week of samples; at a 15-minute cadence that is ~672 rows. */
        const val RETENTION_MILLIS = 7L * 24 * 60 * 60 * 1000
    }
}
