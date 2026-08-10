package com.anhprgm.deviceinfo.data.export

import kotlinx.serialization.Serializable

/**
 * The serialized device report.
 *
 * This doubles as the import format for comparison, so [schemaVersion] must be
 * bumped on any breaking change and older files kept readable. Values are raw
 * (bytes, celsius, hertz) rather than formatted strings — a report written on
 * a Vietnamese device must be readable on an English one.
 */
@Serializable
data class DeviceReport(
    val schemaVersion: Int = SCHEMA_VERSION,
    val generatedAtMillis: Long,
    val appVersion: String,
    val device: DeviceSection,
    val hardware: HardwareSection,
    val display: DisplaySection,
    val battery: BatterySection,
    val gpu: GpuSection? = null,
    val storage: StorageSection? = null,
    val cameras: List<CameraSection> = emptyList(),
    val sensors: List<SensorSection> = emptyList(),
    val benchmark: BenchmarkSection? = null
) {
    companion object {
        const val SCHEMA_VERSION = 1
    }
}

@Serializable
data class DeviceSection(
    val name: String,
    val manufacturer: String,
    val model: String,
    val brand: String,
    val device: String,
    val board: String,
    val androidVersion: String,
    val apiLevel: Int,
    val securityPatch: String?,
    val buildId: String,
    val fingerprint: String,
    val supportedAbis: List<String>
)

@Serializable
data class HardwareSection(
    val cpuModel: String,
    val cpuCores: Int,
    val cpuMaxFrequencyKhz: Long?,
    val totalRamBytes: Long,
    val availableRamBytes: Long,
    val totalStorageBytes: Long,
    val availableStorageBytes: Long
)

@Serializable
data class DisplaySection(
    val widthPixels: Int,
    val heightPixels: Int,
    val densityDpi: Int,
    val diagonalInches: Float?,
    val refreshRateHz: Float?,
    val hdrSupported: Boolean
)

@Serializable
data class BatterySection(
    val level: Int?,
    val status: String,
    val health: String,
    val temperatureCelsius: Float?,
    val voltageVolts: Float?,
    val technology: String?,
    val estimatedCapacityMah: Int?
)

@Serializable
data class GpuSection(
    val renderer: String?,
    val vendor: String?,
    val glVersion: String?,
    val glesVersion: String,
    val vulkanSupported: Boolean,
    val vulkanApiVersion: String?
)

@Serializable
data class StorageSection(
    val totalBytes: Long,
    val availableBytes: Long,
    val appsBytes: Long?,
    val installedAppCount: Int
)

@Serializable
data class CameraSection(
    val id: String,
    val facing: String,
    val megapixels: Double?,
    val pixelWidth: Int?,
    val pixelHeight: Int?,
    val flashAvailable: Boolean
)

@Serializable
data class SensorSection(
    val name: String,
    val type: String,
    val vendor: String,
    val powerMilliAmps: Float
)

@Serializable
data class BenchmarkSection(
    val singleCoreMillis: Long,
    val multiCoreMillis: Long,
    val memoryMillis: Long,
    val coresUsed: Int,
    val overallScore: Int,
    val timestamp: Long
)
