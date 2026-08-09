package com.anhprgm.deviceinfo.ui.format

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

/**
 * The reason typed models exist.
 *
 * The old code formatted bytes into a display string with an implicit locale
 * and later parsed that string back with `toFloatOrNull()`. Under a
 * comma-decimal locale the round trip returns null, and every history row
 * silently recorded 0 bytes of RAM.
 */
class FormattersTest {

    private val vietnamese = Locale.forLanguageTag("vi-VN")

    @Test
    fun `bytes formats gigabytes in US locale`() {
        assertEquals("3.45 GB", Formatters.bytes(3_704_409_620L, Locale.US))
    }

    @Test
    fun `bytes uses a comma decimal separator in Vietnamese`() {
        // This is the exact output that broke the old round trip.
        assertEquals("3,45 GB", Formatters.bytes(3_704_409_620L, vietnamese))
    }

    @Test
    fun `bytesRoot is locale independent so serialized values round trip`() {
        val fromUs = Formatters.bytesRoot(3_704_409_620L)
        val fromVietnamese = Formatters.bytesRoot(3_704_409_620L)
        assertEquals(fromUs, fromVietnamese)
        assertEquals("3.45 GB", fromUs)
    }

    @Test
    fun `bytes picks the right unit at each boundary`() {
        assertEquals("512 B", Formatters.bytes(512L, Locale.US))
        assertEquals("1.00 KB", Formatters.bytes(1024L, Locale.US))
        assertEquals("1.00 MB", Formatters.bytes(1024L * 1024, Locale.US))
        assertEquals("1.00 GB", Formatters.bytes(1024L * 1024 * 1024, Locale.US))
    }

    @Test
    fun `null and negative sizes render as N slash A rather than zero`() {
        assertEquals("N/A", Formatters.bytes(null, Locale.US))
        assertEquals("N/A", Formatters.bytes(-1L, Locale.US))
    }

    @Test
    fun `unavailable cpu reading is N slash A not zero percent`() {
        assertEquals("N/A", Formatters.percent(null, locale = Locale.US))
        assertEquals("0.0%", Formatters.percent(0f, locale = Locale.US))
    }

    @Test
    fun `percentInt rejects null and negative levels`() {
        assertEquals("N/A", Formatters.percentInt(null, Locale.US))
        assertEquals("N/A", Formatters.percentInt(-1, Locale.US))
        assertEquals("87%", Formatters.percentInt(87, Locale.US))
    }

    @Test
    fun `celsius and volts respect the locale separator`() {
        assertEquals("31.5°C", Formatters.celsius(31.5f, Locale.US))
        assertEquals("31,5°C", Formatters.celsius(31.5f, vietnamese))
        assertEquals("4.05 V", Formatters.volts(4.05f, Locale.US))
    }

    @Test
    fun `NaN is treated as unavailable`() {
        assertEquals("N/A", Formatters.celsius(Float.NaN, Locale.US))
        assertEquals("N/A", Formatters.percent(Float.NaN, locale = Locale.US))
    }

    @Test
    fun `duration collapses to the largest meaningful unit`() {
        assertEquals("45m", Formatters.duration(45 * 60_000L, Locale.US))
        assertEquals("3h 5m", Formatters.duration((3 * 60 + 5) * 60_000L, Locale.US))
        assertEquals("2d 4h 13m", Formatters.duration(
            (2 * 24 * 60 + 4 * 60 + 13) * 60_000L, Locale.US
        ))
    }

    @Test
    fun `elapsed switches to seconds past a second`() {
        assertEquals("850 ms", Formatters.elapsed(850L, Locale.US))
        assertEquals("1.50 s", Formatters.elapsed(1500L, Locale.US))
    }

    @Test
    fun `megahertz converts kHz and drops to MHz below a gigahertz`() {
        assertEquals("2.40 GHz", Formatters.megahertzFromKhz(2_400_000L, Locale.US))
        assertEquals("800 MHz", Formatters.megahertzFromKhz(800_000L, Locale.US))
        assertEquals("N/A", Formatters.megahertzFromKhz(null, Locale.US))
    }

    @Test
    fun `text falls back for null and blank`() {
        assertEquals("N/A", Formatters.text(null))
        assertEquals("N/A", Formatters.text("   "))
        assertEquals("Li-ion", Formatters.text("Li-ion"))
    }

    @Test
    fun `resolution rejects non positive dimensions`() {
        assertEquals("1080 × 2400", Formatters.resolution(1080, 2400, Locale.US))
        assertEquals("N/A", Formatters.resolution(0, 2400, Locale.US))
        assertEquals("N/A", Formatters.resolution(null, null, Locale.US))
    }
}
