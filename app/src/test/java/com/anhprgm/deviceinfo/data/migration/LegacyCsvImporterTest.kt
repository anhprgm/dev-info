package com.anhprgm.deviceinfo.data.migration

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * This migration runs once per install and cannot be retried, so the parse is
 * covered directly rather than through the file wrapper.
 */
class LegacyCsvImporterTest {

    @Test
    fun `parses well formed rows`() {
        val rows = LegacyCsvImporter.parseCsvLines(
            listOf(
                "1700000000000,85,2147483648,12.5",
                "1700000060000,84,2000000000,7.25"
            )
        )

        assertEquals(2, rows.size)
        assertEquals(1_700_000_000_000L, rows[0].timestamp)
        assertEquals(85, rows[0].batteryLevel)
        assertEquals(2_147_483_648L, rows[0].availableRamBytes)
        assertEquals(12.5f, rows[0].cpuPercent!!, 0.001f)
    }

    /**
     * The legacy writer stored 0f whenever /proc/stat could not be read. Those
     * are absent measurements, not idle CPU, so they must not become data
     * points on the chart.
     */
    @Test
    fun `zero cpu from the old writer becomes null not a real reading`() {
        val rows = LegacyCsvImporter.parseCsvLines(listOf("1700000000000,85,2147483648,0.0"))
        assertEquals(1, rows.size)
        assertNull(rows[0].cpuPercent)
    }

    @Test
    fun `out of range cpu is discarded`() {
        val rows = LegacyCsvImporter.parseCsvLines(listOf("1700000000000,85,123,150.0"))
        assertNull(rows[0].cpuPercent)
    }

    @Test
    fun `malformed rows are skipped without failing the import`() {
        val rows = LegacyCsvImporter.parseCsvLines(
            listOf(
                "",
                "   ",
                "not,enough",
                "too,many,columns,here,extra",
                "notanumber,85,123,1.0",
                "1700000000000,notanumber,123,1.0",
                "1700000000000,85,notanumber,1.0",
                "1700000000000,85,2147483648,1.0" // the only good row
            )
        )
        assertEquals(1, rows.size)
        assertEquals(1_700_000_000_000L, rows[0].timestamp)
    }

    @Test
    fun `implausible battery levels and timestamps are rejected`() {
        val rows = LegacyCsvImporter.parseCsvLines(
            listOf(
                "0,85,123,1.0",       // zero timestamp
                "-5,85,123,1.0",      // negative timestamp
                "1700000000000,101,123,1.0",  // above 100%
                "1700000000000,-1,123,1.0"    // below 0%
            )
        )
        assertTrue(rows.isEmpty())
    }

    @Test
    fun `unparseable cpu column still yields a usable row`() {
        val rows = LegacyCsvImporter.parseCsvLines(listOf("1700000000000,85,2147483648,NaNish"))
        assertEquals(1, rows.size)
        assertNull(rows[0].cpuPercent)
        assertEquals(85, rows[0].batteryLevel)
    }

    @Test
    fun `whitespace around values is tolerated`() {
        val rows = LegacyCsvImporter.parseCsvLines(listOf(" 1700000000000 , 85 , 2147483648 , 5.0 "))
        assertEquals(1, rows.size)
        assertEquals(85, rows[0].batteryLevel)
        assertEquals(5.0f, rows[0].cpuPercent!!, 0.001f)
    }

    @Test
    fun `legacy schema had no total ram column`() {
        val rows = LegacyCsvImporter.parseCsvLines(listOf("1700000000000,85,2147483648,5.0"))
        assertEquals(0L, rows[0].totalRamBytes)
    }
}
