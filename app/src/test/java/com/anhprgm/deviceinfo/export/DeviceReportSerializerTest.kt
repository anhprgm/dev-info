package com.anhprgm.deviceinfo.export

import com.anhprgm.deviceinfo.data.export.BatterySection
import com.anhprgm.deviceinfo.data.export.DeviceReport
import com.anhprgm.deviceinfo.data.export.DeviceSection
import com.anhprgm.deviceinfo.data.export.DisplaySection
import com.anhprgm.deviceinfo.data.export.HardwareSection
import com.anhprgm.deviceinfo.data.export.ReportSerializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

/**
 * The JSON report doubles as the import format for device comparison, so the
 * schema has to stay readable across versions and locales.
 */
class DeviceReportSerializerTest {

    private val serializer = ReportSerializer()

    private fun sampleReport() = DeviceReport(
        generatedAtMillis = 1_700_000_000_000L,
        appVersion = "1.0.1 (1)",
        device = DeviceSection(
            name = "Google Pixel 8",
            manufacturer = "Google",
            model = "Pixel 8",
            brand = "google",
            device = "shiba",
            board = "shiba",
            androidVersion = "15",
            apiLevel = 35,
            securityPatch = "2025-01-05",
            buildId = "AP4A.250105.002",
            fingerprint = "google/shiba/shiba:15/AP4A.250105.002/1:user/release-keys",
            supportedAbis = listOf("arm64-v8a", "armeabi-v7a")
        ),
        hardware = HardwareSection(
            cpuModel = "Tensor G3",
            cpuCores = 9,
            cpuMaxFrequencyKhz = 2_914_000,
            totalRamBytes = 8_589_934_592,
            availableRamBytes = 3_704_409_620,
            totalStorageBytes = 128_000_000_000,
            availableStorageBytes = 64_000_000_000
        ),
        display = DisplaySection(
            widthPixels = 1080,
            heightPixels = 2400,
            densityDpi = 420,
            diagonalInches = 6.2f,
            refreshRateHz = 120f,
            hdrSupported = true
        ),
        battery = BatterySection(
            level = 87,
            status = "DISCHARGING",
            health = "GOOD",
            temperatureCelsius = 31.5f,
            voltageVolts = 4.05f,
            technology = "Li-ion",
            estimatedCapacityMah = 4575
        )
    )

    @Test
    fun `json round trips`() {
        val original = sampleReport()
        val decoded = serializer.fromJson(serializer.toJson(original))

        assertNotNull(decoded)
        assertEquals(original, decoded)
    }

    @Test
    fun `schema version is written so old exports stay identifiable`() {
        val json = serializer.toJson(sampleReport())
        assertTrue(json.contains("\"schemaVersion\": 1"))
    }

    /**
     * A report written on a Vietnamese device must be readable on an English
     * one. Serialization has to use ROOT regardless of the default locale.
     */
    @Test
    fun `json numbers are locale independent`() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("vi-VN"))
            val json = serializer.toJson(sampleReport())

            // A comma decimal separator here would make the JSON invalid.
            assertTrue("Found a comma decimal in JSON", !json.contains("31,5"))
            assertTrue(json.contains("31.5"))
            assertNotNull(serializer.fromJson(json))
        } finally {
            Locale.setDefault(previous)
        }
    }

    @Test
    fun `plain text uses root locale for numbers`() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("vi-VN"))
            val text = serializer.toPlainText(sampleReport())
            assertTrue("Expected a dot decimal in the report", text.contains("31.5"))
        } finally {
            Locale.setDefault(previous)
        }
    }

    @Test
    fun `unknown fields from a newer version do not break decoding`() {
        val json = serializer.toJson(sampleReport())
            .replaceFirst("{", "{\n  \"somethingFromTheFuture\": 42,")

        assertNotNull("Decoder must tolerate unknown keys", serializer.fromJson(json))
    }

    @Test
    fun `malformed json yields null rather than throwing`() {
        assertNull(serializer.fromJson("not json at all"))
        assertNull(serializer.fromJson("{"))
    }

    @Test
    fun `plain text contains every major section`() {
        val text = serializer.toPlainText(sampleReport())
        listOf("DEVICE", "HARDWARE", "DISPLAY", "BATTERY").forEach { section ->
            assertTrue("Missing section $section", text.contains(section))
        }
        assertTrue(text.contains("Pixel 8"))
        assertTrue(text.contains("Tensor G3"))
    }

    @Test
    fun `share summary stays short enough for a share sheet`() {
        val summary = serializer.toShareSummary(sampleReport())
        assertTrue("Summary unexpectedly long", summary.length < 300)
        assertTrue(summary.contains("Google Pixel 8"))
    }
}
