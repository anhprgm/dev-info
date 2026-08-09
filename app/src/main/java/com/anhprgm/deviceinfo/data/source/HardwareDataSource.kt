package com.anhprgm.deviceinfo.data.source

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import com.anhprgm.deviceinfo.data.models.HardwareInfo
import com.anhprgm.deviceinfo.data.util.CpuStatParser
import com.anhprgm.deviceinfo.data.util.SysFs
import com.anhprgm.deviceinfo.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HardwareDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val io: CoroutineDispatcher
) {
    private val activityManager: ActivityManager
        get() = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    /** Cached because /proc/cpuinfo parsing is not free and the answer never changes. */
    @Volatile
    private var cachedCpuModel: String? = null

    suspend fun getHardwareInfo(): HardwareInfo = withContext(io) {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val statFs = StatFs(Environment.getDataDirectory().path)

        HardwareInfo(
            totalRamBytes = memoryInfo.totalMem,
            availableRamBytes = memoryInfo.availMem,
            totalStorageBytes = statFs.totalBytes,
            availableStorageBytes = statFs.availableBytes,
            cpuModel = readCpuModel(),
            cpuCores = Runtime.getRuntime().availableProcessors(),
            cpuMaxFrequencyKhz = SysFs.maxCpuFrequencyKhz(),
            supportedAbis = Build.SUPPORTED_ABIS?.toList().orEmpty()
        )
    }

    private fun readCpuModel(): String {
        cachedCpuModel?.let { return it }
        val text = try {
            File("/proc/cpuinfo").takeIf { it.canRead() }?.readText()
        } catch (_: Exception) {
            null
        }
        val model = if (text != null) {
            CpuStatParser.parseCpuModel(text, Build.HARDWARE)
        } else {
            Build.HARDWARE
        }
        cachedCpuModel = model
        return model
    }
}
