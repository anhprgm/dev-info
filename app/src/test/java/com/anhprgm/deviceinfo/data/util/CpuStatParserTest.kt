package com.anhprgm.deviceinfo.data.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CpuStatParserTest {

    /** A realistic `/proc/self/stat` line: utime=1234 (field 14), stime=567 (field 15). */
    private val statLine =
        "4242 (com.anhprgm.devinfo) S 1000 4242 0 0 -1 4194624 9876 0 12 0 1234 567 0 0 20 0 18 0 " +
            "9876543 5432109876 12345 18446744073709551615 1 1 0 0 0 0 4612 1 1073775864 0 0 0 17 3 0 0"

    @Test
    fun `parses utime and stime`() {
        val parsed = CpuStatParser.parseProcessCpuTime(statLine)
        assertNotNull(parsed)
        assertEquals(1234L, parsed!!.utimeTicks)
        assertEquals(567L, parsed.stimeTicks)
        assertEquals(1801L, parsed.totalTicks)
    }

    /**
     * Field 2 is the process name in parentheses and may itself contain spaces
     * or parentheses, so fields must be located from the last ')'.
     */
    @Test
    fun `handles a process name containing spaces and parentheses`() {
        val awkward = "77 (weird (name) here) S 1 77 0 0 -1 0 0 0 0 0 900 100 0 0 20 0 1 0 5 6 7"
        val parsed = CpuStatParser.parseProcessCpuTime(awkward)
        assertNotNull(parsed)
        assertEquals(900L, parsed!!.utimeTicks)
        assertEquals(100L, parsed.stimeTicks)
    }

    @Test
    fun `returns null for malformed input rather than throwing`() {
        assertNull(CpuStatParser.parseProcessCpuTime(""))
        assertNull(CpuStatParser.parseProcessCpuTime("no parenthesis here"))
        assertNull(CpuStatParser.parseProcessCpuTime("1 (x) S 1 2 3"))
    }

    @Test
    fun `computes cpu percent against wall time and core count`() {
        val before = CpuStatParser.ProcessCpuTime(1000, 0)
        // 100 extra ticks at 100 Hz = 1.0 s of CPU.
        val after = CpuStatParser.ProcessCpuTime(1100, 0)

        // 1.0s CPU over 1.0s wall on 1 core = 100%.
        assertEquals(
            100f,
            CpuStatParser.cpuPercent(before, after, 1000L, 100L, 1)!!,
            0.01f
        )
        // Same work spread over 4 cores of capacity = 25%.
        assertEquals(
            25f,
            CpuStatParser.cpuPercent(before, after, 1000L, 100L, 4)!!,
            0.01f
        )
    }

    @Test
    fun `cpu percent is null when it cannot be computed`() {
        val a = CpuStatParser.ProcessCpuTime(100, 0)
        val b = CpuStatParser.ProcessCpuTime(200, 0)
        assertNull(CpuStatParser.cpuPercent(a, b, 0L, 100L, 4))
        assertNull(CpuStatParser.cpuPercent(a, b, 1000L, 0L, 4))
        assertNull(CpuStatParser.cpuPercent(a, b, 1000L, 100L, 0))
        // Counters going backwards means the samples are not comparable.
        assertNull(CpuStatParser.cpuPercent(b, a, 1000L, 100L, 4))
    }

    @Test
    fun `cpu percent is clamped to 100`() {
        val before = CpuStatParser.ProcessCpuTime(0, 0)
        val after = CpuStatParser.ProcessCpuTime(100_000, 0)
        assertEquals(100f, CpuStatParser.cpuPercent(before, after, 10L, 100L, 1)!!, 0.01f)
    }

    @Test
    fun `parses a cpu model name and falls back when absent`() {
        val cpuInfo = """
            processor	: 0
            model name	: ARMv8 Processor rev 1 (v8l)
            BogoMIPS	: 38.40
        """.trimIndent()
        assertEquals(
            "ARMv8 Processor rev 1 (v8l)",
            CpuStatParser.parseCpuModel(cpuInfo, "fallback")
        )
        assertEquals("fallback", CpuStatParser.parseCpuModel("processor\t: 0", "fallback"))
        assertEquals("fallback", CpuStatParser.parseCpuModel("model name\t:   ", "fallback"))
    }
}
