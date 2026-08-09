package com.anhprgm.deviceinfo.data.monitor

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.system.Os
import android.system.OsConstants
import com.anhprgm.deviceinfo.data.models.MonitoringInfo
import com.anhprgm.deviceinfo.data.source.BatteryDataSource
import com.anhprgm.deviceinfo.data.util.CpuStatParser
import com.anhprgm.deviceinfo.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val batteryDataSource: BatteryDataSource,
    @IoDispatcher private val io: CoroutineDispatcher
) {
    private val activityManager: ActivityManager
        get() = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    private val clockTicksPerSecond: Long by lazy {
        runCatching { Os.sysconf(OsConstants._SC_CLK_TCK) }.getOrDefault(100L)
    }

    /**
     * Emits a sample every [intervalMillis].
     *
     * The old implementation called Thread.sleep(200) on the main thread every
     * two seconds to measure CPU. Here the sampling gap is a suspending delay
     * on an IO dispatcher, so nothing blocks the UI.
     */
    fun sample(intervalMillis: Long = 2_000L): Flow<MonitoringInfo> = flow {
        while (true) {
            emit(snapshot())
            delay(intervalMillis)
        }
    }.flowOn(io)

    suspend fun snapshot(): MonitoringInfo = withContext(io) {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val statFs = StatFs(Environment.getDataDirectory().path)
        val battery = batteryDataSource.getBatteryInfo()

        MonitoringInfo(
            appCpuPercent = measureAppCpuPercent(),
            ramUsedBytes = memoryInfo.totalMem - memoryInfo.availMem,
            ramTotalBytes = memoryInfo.totalMem,
            ramAvailableBytes = memoryInfo.availMem,
            storageUsedBytes = statFs.totalBytes - statFs.availableBytes,
            storageTotalBytes = statFs.totalBytes,
            batteryLevel = battery.level,
            batteryTemperatureCelsius = battery.temperatureCelsius,
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * CPU consumed by this process over a short window, as a share of total
     * device capacity. Returns null if /proc/self/stat cannot be parsed —
     * callers must show N/A rather than 0.
     */
    private suspend fun measureAppCpuPercent(): Float? {
        val before = readSelfStat() ?: return null
        val startedAt = SystemClock.elapsedRealtime()
        delay(SAMPLE_WINDOW_MILLIS)
        val after = readSelfStat() ?: return null

        return CpuStatParser.cpuPercent(
            before = before,
            after = after,
            elapsedMillis = SystemClock.elapsedRealtime() - startedAt,
            clockTicksPerSecond = clockTicksPerSecond,
            coreCount = Runtime.getRuntime().availableProcessors()
        )
    }

    private fun readSelfStat(): CpuStatParser.ProcessCpuTime? = try {
        CpuStatParser.parseProcessCpuTime(File("/proc/self/stat").readText())
    } catch (_: Exception) {
        null
    }

    private companion object {
        const val SAMPLE_WINDOW_MILLIS = 250L
    }
}
