package com.anhprgm.deviceinfo.data.export

import com.anhprgm.deviceinfo.BuildConfig
import com.anhprgm.deviceinfo.data.repository.HistoryRepository
import com.anhprgm.deviceinfo.data.source.BatteryDataSource
import com.anhprgm.deviceinfo.data.source.CameraDataSource
import com.anhprgm.deviceinfo.data.source.DeviceDataSource
import com.anhprgm.deviceinfo.data.source.DisplayDataSource
import com.anhprgm.deviceinfo.data.source.GpuDataSource
import com.anhprgm.deviceinfo.data.source.HardwareDataSource
import com.anhprgm.deviceinfo.data.source.SensorDataSource
import com.anhprgm.deviceinfo.data.source.StorageDataSource
import com.anhprgm.deviceinfo.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Assembles the full report by fanning out across every data source.
 *
 * Sources are queried concurrently: GPU needs an EGL context and storage walks
 * the package list, so running them in sequence would make export feel slow.
 */
@Singleton
class ReportBuilder @Inject constructor(
    private val deviceDataSource: DeviceDataSource,
    private val hardwareDataSource: HardwareDataSource,
    private val displayDataSource: DisplayDataSource,
    private val batteryDataSource: BatteryDataSource,
    private val gpuDataSource: GpuDataSource,
    private val storageDataSource: StorageDataSource,
    private val cameraDataSource: CameraDataSource,
    private val sensorDataSource: SensorDataSource,
    private val historyRepository: HistoryRepository,
    @IoDispatcher private val io: CoroutineDispatcher
) {
    suspend fun build(): DeviceReport = withContext(io) {
        coroutineScope {
            val device = async { deviceDataSource.getDeviceInfo() }
            val hardware = async { hardwareDataSource.getHardwareInfo() }
            val display = async { displayDataSource.getDisplayInfo() }
            val battery = async { batteryDataSource.getBatteryInfo() }
            val gpu = async { runCatching { gpuDataSource.getGpuInfo() }.getOrNull() }
            val storage = async { runCatching { storageDataSource.getStorageInfo() }.getOrNull() }
            val cameras = async { runCatching { cameraDataSource.getCameraInfo() }.getOrNull() }
            val sensors = async { runCatching { sensorDataSource.getSensorInfo() }.getOrNull() }
            val benchmark = async { historyRepository.latestBenchmark() }

            val d = device.await()
            val h = hardware.await()
            val disp = display.await()
            val b = battery.await()

            DeviceReport(
                generatedAtMillis = System.currentTimeMillis(),
                appVersion = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                device = DeviceSection(
                    name = d.deviceName,
                    manufacturer = d.manufacturer,
                    model = d.model,
                    brand = d.brand,
                    device = d.device,
                    board = d.board,
                    androidVersion = d.androidVersion,
                    apiLevel = d.apiLevel,
                    securityPatch = d.securityPatch,
                    buildId = d.buildId,
                    fingerprint = d.buildFingerprint,
                    supportedAbis = d.supportedAbis
                ),
                hardware = HardwareSection(
                    cpuModel = h.cpuModel,
                    cpuCores = h.cpuCores,
                    cpuMaxFrequencyKhz = h.cpuMaxFrequencyKhz,
                    totalRamBytes = h.totalRamBytes,
                    availableRamBytes = h.availableRamBytes,
                    totalStorageBytes = h.totalStorageBytes,
                    availableStorageBytes = h.availableStorageBytes
                ),
                display = DisplaySection(
                    widthPixels = disp.widthPixels,
                    heightPixels = disp.heightPixels,
                    densityDpi = disp.densityDpi,
                    diagonalInches = disp.diagonalInches,
                    refreshRateHz = disp.refreshRateHz,
                    hdrSupported = disp.hdrSupported
                ),
                battery = BatterySection(
                    level = b.level,
                    status = b.status.name,
                    health = b.health.name,
                    temperatureCelsius = b.temperatureCelsius,
                    voltageVolts = b.voltageVolts,
                    technology = b.technology,
                    estimatedCapacityMah = b.estimatedCapacityMah
                ),
                gpu = gpu.await()?.let {
                    GpuSection(
                        renderer = it.renderer,
                        vendor = it.vendor,
                        glVersion = it.glVersion,
                        glesVersion = it.glesVersion,
                        vulkanSupported = it.vulkan.supported,
                        vulkanApiVersion = it.vulkan.apiVersion
                    )
                },
                storage = storage.await()?.let {
                    StorageSection(
                        totalBytes = it.totalBytes,
                        availableBytes = it.availableBytes,
                        appsBytes = it.appsBytes,
                        installedAppCount = it.installedAppCount
                    )
                },
                cameras = cameras.await()?.cameras?.map {
                    CameraSection(
                        id = it.cameraId,
                        facing = it.facing.name,
                        megapixels = it.megapixels,
                        pixelWidth = it.pixelWidth,
                        pixelHeight = it.pixelHeight,
                        flashAvailable = it.flashAvailable
                    )
                }.orEmpty(),
                sensors = sensors.await()?.sensors?.map {
                    SensorSection(
                        name = it.name,
                        type = it.kind.name,
                        vendor = it.vendor,
                        powerMilliAmps = it.powerMilliAmps
                    )
                }.orEmpty(),
                benchmark = benchmark.await()?.let {
                    BenchmarkSection(
                        singleCoreMillis = it.singleCoreMillis,
                        multiCoreMillis = it.multiCoreMillis,
                        memoryMillis = it.memoryMillis,
                        coresUsed = it.coresUsed,
                        overallScore = it.overallScore,
                        timestamp = it.timestamp
                    )
                }
            )
        }
    }
}
