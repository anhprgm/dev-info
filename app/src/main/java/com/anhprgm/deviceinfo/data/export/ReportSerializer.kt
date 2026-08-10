package com.anhprgm.deviceinfo.data.export

import com.anhprgm.deviceinfo.ui.format.Formatters
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Renders a [DeviceReport] to text formats.
 *
 * Everything here formats with [Locale.ROOT]: a report is a file that gets
 * shared and re-read, possibly on a device with a different locale, so a
 * comma decimal separator would make it unparseable.
 */
@Singleton
class ReportSerializer @Inject constructor() {

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        // Tolerate fields added by newer versions when reading back for Compare.
        ignoreUnknownKeys = true
    }

    fun toJson(report: DeviceReport): String = json.encodeToString(report)

    fun fromJson(text: String): DeviceReport? =
        runCatching { json.decodeFromString<DeviceReport>(text) }.getOrNull()

    fun toPlainText(report: DeviceReport): String = buildString {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
            .format(Date(report.generatedAtMillis))

        appendLine("DEVICE INFORMATION REPORT")
        appendLine("Generated: $timestamp")
        appendLine("App: DevInfo ${report.appVersion}")
        appendLine()

        section("DEVICE")
        field("Name", report.device.name)
        field("Manufacturer", report.device.manufacturer)
        field("Model", report.device.model)
        field("Brand", report.device.brand)
        field("Device", report.device.device)
        field("Board", report.device.board)
        field("Android", "${report.device.androidVersion} (API ${report.device.apiLevel})")
        field("Security patch", report.device.securityPatch)
        field("Build ID", report.device.buildId)
        field("ABIs", report.device.supportedAbis.joinToString(", "))
        field("Fingerprint", report.device.fingerprint)
        appendLine()

        section("HARDWARE")
        field("CPU", report.hardware.cpuModel)
        field("Cores", report.hardware.cpuCores.toString())
        field("Max frequency", Formatters.megahertzFromKhz(
            report.hardware.cpuMaxFrequencyKhz, Locale.ROOT
        ))
        field("Total RAM", Formatters.bytesRoot(report.hardware.totalRamBytes))
        field("Available RAM", Formatters.bytesRoot(report.hardware.availableRamBytes))
        field("Total storage", Formatters.bytesRoot(report.hardware.totalStorageBytes))
        field("Available storage", Formatters.bytesRoot(report.hardware.availableStorageBytes))
        appendLine()

        report.gpu?.let { gpu ->
            section("GRAPHICS")
            field("Renderer", gpu.renderer)
            field("Vendor", gpu.vendor)
            field("OpenGL", gpu.glVersion)
            field("OpenGL ES", gpu.glesVersion)
            field("Vulkan", if (gpu.vulkanSupported) gpu.vulkanApiVersion ?: "yes" else "no")
            appendLine()
        }

        section("DISPLAY")
        field("Resolution", "${report.display.widthPixels} x ${report.display.heightPixels}")
        field("Density", "${report.display.densityDpi} dpi")
        field("Size", Formatters.inches(report.display.diagonalInches, Locale.ROOT))
        field("Refresh rate", Formatters.hertz(report.display.refreshRateHz, Locale.ROOT))
        field("HDR", if (report.display.hdrSupported) "yes" else "no")
        appendLine()

        section("BATTERY")
        field("Level", Formatters.percentInt(report.battery.level, Locale.ROOT))
        field("Status", report.battery.status)
        field("Health", report.battery.health)
        field("Temperature", Formatters.celsius(report.battery.temperatureCelsius, Locale.ROOT))
        field("Voltage", Formatters.volts(report.battery.voltageVolts, Locale.ROOT))
        field("Technology", report.battery.technology)
        field(
            "Estimated capacity",
            Formatters.milliAmpHours(report.battery.estimatedCapacityMah, Locale.ROOT)
        )
        appendLine()

        report.storage?.let { storage ->
            section("STORAGE")
            field("Total", Formatters.bytesRoot(storage.totalBytes))
            field("Available", Formatters.bytesRoot(storage.availableBytes))
            field("Apps (APK)", Formatters.bytesRoot(storage.appsBytes))
            field("Installed apps", storage.installedAppCount.toString())
            appendLine()
        }

        if (report.cameras.isNotEmpty()) {
            section("CAMERAS (${report.cameras.size})")
            report.cameras.forEach { camera ->
                field(
                    "Camera ${camera.id}",
                    "${camera.facing}, " +
                        Formatters.megapixels(camera.megapixels, Locale.ROOT) +
                        ", flash=${if (camera.flashAvailable) "yes" else "no"}"
                )
            }
            appendLine()
        }

        if (report.sensors.isNotEmpty()) {
            section("SENSORS (${report.sensors.size})")
            report.sensors.forEach { sensor ->
                field(sensor.name, "${sensor.type}, ${sensor.vendor}")
            }
            appendLine()
        }

        report.benchmark?.let { benchmark ->
            section("BENCHMARK")
            field("Single-core", "${benchmark.singleCoreMillis} ms")
            field("Multi-core", "${benchmark.multiCoreMillis} ms")
            field("Memory", "${benchmark.memoryMillis} ms")
            field("Cores used", benchmark.coresUsed.toString())
            field("Overall score", benchmark.overallScore.toString())
            appendLine()
        }
    }

    /** Short form for a share sheet, where a full report would be unusable. */
    fun toShareSummary(report: DeviceReport): String = buildString {
        appendLine(report.device.name)
        appendLine("Android ${report.device.androidVersion} (API ${report.device.apiLevel})")
        appendLine(
            "${report.hardware.cpuModel} · ${report.hardware.cpuCores} cores · " +
                Formatters.bytesRoot(report.hardware.totalRamBytes) + " RAM"
        )
        appendLine(
            "${report.display.widthPixels}x${report.display.heightPixels} · " +
                "${report.display.densityDpi} dpi"
        )
        report.gpu?.renderer?.let { appendLine(it) }
    }

    private fun StringBuilder.section(title: String) {
        appendLine(title)
        appendLine("-".repeat(title.length))
    }

    private fun StringBuilder.field(label: String, value: String?) {
        appendLine("${label.padEnd(FIELD_WIDTH)}: ${value ?: Formatters.NOT_AVAILABLE}")
    }

    private companion object {
        const val FIELD_WIDTH = 20
    }
}
