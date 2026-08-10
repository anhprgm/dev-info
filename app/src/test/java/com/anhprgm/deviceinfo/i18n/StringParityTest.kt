package com.anhprgm.deviceinfo.i18n

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Guards the #1 localization regression: adding an English string and
 * forgetting the translation, or changing a format specifier in one file only.
 *
 * Runs in milliseconds and needs no device.
 */
class StringParityTest {

    private val resDir = File("src/main/res")

    private fun parseStrings(path: String): Map<String, String> {
        val file = File(resDir, path)
        assertTrue("Missing $path", file.exists())

        val doc = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(file)

        val nodes = doc.getElementsByTagName("string")
        return buildMap {
            for (i in 0 until nodes.length) {
                val element = nodes.item(i)
                val name = element.attributes.getNamedItem("name")?.nodeValue ?: continue
                put(name, element.textContent ?: "")
            }
        }
    }

    /** Positional specifiers like %1$s / %2$d, which must match across locales. */
    private fun specifiersOf(value: String): List<String> =
        Regex("%\\d+\\$[sdf]").findAll(value).map { it.value }.sorted().toList()

    @Test
    fun `vietnamese has every english key`() {
        val en = parseStrings("values/strings.xml")
        val vi = parseStrings("values-vi/strings.xml")

        val missing = en.keys - vi.keys
        assertTrue("Missing Vietnamese translations for: ${missing.sorted()}", missing.isEmpty())
    }

    @Test
    fun `vietnamese has no orphan keys`() {
        val en = parseStrings("values/strings.xml")
        val vi = parseStrings("values-vi/strings.xml")

        val orphans = vi.keys - en.keys
        assertTrue("Vietnamese keys with no English original: ${orphans.sorted()}", orphans.isEmpty())
    }

    @Test
    fun `format specifiers match between locales`() {
        val en = parseStrings("values/strings.xml")
        val vi = parseStrings("values-vi/strings.xml")

        // A mismatch here throws IllegalFormatException at runtime, not compile time.
        en.forEach { (key, englishValue) ->
            val vietnameseValue = vi[key] ?: return@forEach
            assertEquals(
                "Format specifiers differ for '$key'",
                specifiersOf(englishValue),
                specifiersOf(vietnameseValue)
            )
        }
    }

    @Test
    fun `no translation is left as the english original`() {
        val en = parseStrings("values/strings.xml")
        val vi = parseStrings("values-vi/strings.xml")

        // Proper nouns, platform identifiers and pure format strings legitimately
        // stay identical across locales.
        val allowedIdentical = setOf(
            "connection_wifi", "connection_ethernet", "connection_vpn", "connection_bluetooth",
            "device_model", "device_bootloader", "device_fingerprint", "display_hdr",
            "display_density_format", "network_signal_format", "network_link_speed_format",
            "screen_camera", "app_target_sdk", "dashboard_android", "dashboard_ram",
            "camera_number", "hardware_summary", "history_point_count",
            "history_sample_summary", "sensor_other",
            // Technology names — translating these would make them wrong.
            "gpu_gles_version", "vulkan_section", "security_bootloader", "security_selinux",
            // Axis labels are universal notation.
            "test_sensor_x", "test_sensor_y", "test_sensor_z",
            // File-format names.
            "export_format_txt", "export_format_json", "export_format_pdf"
        )

        val untranslated = en.filter { (key, value) ->
            key !in allowedIdentical && vi[key] == value && value.isNotBlank()
        }.keys

        assertTrue("Still in English in values-vi: ${untranslated.sorted()}", untranslated.isEmpty())
    }
}
