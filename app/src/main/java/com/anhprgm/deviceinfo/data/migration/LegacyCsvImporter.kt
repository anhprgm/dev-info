package com.anhprgm.deviceinfo.data.migration

import com.anhprgm.deviceinfo.data.db.entity.HistorySampleEntity
import java.io.File

/**
 * One-shot import of the pre-Room `filesDir/device_history.txt` CSV store.
 *
 * The parse is a pure function so it can be unit-tested: this migration only
 * ever runs once per install and cannot be retried if it drops data.
 */
object LegacyCsvImporter {

    const val LEGACY_FILE_NAME = "device_history.txt"
    private const val IMPORTED_SUFFIX = ".imported"

    /**
     * Legacy row format: `timestamp,batteryLevel,availableRam,cpuUsage`.
     *
     * The old writer stored 0f whenever CPU could not be read, and 0 for RAM
     * whenever the locale-dependent re-parse of "3,45 GB" failed. Both are
     * mapped back to null here so the charts do not plot fabricated zeros.
     */
    fun parseCsvLines(lines: List<String>): List<HistorySampleEntity> =
        lines.mapNotNull { line ->
            val parts = line.trim().takeIf { it.isNotEmpty() }?.split(',') ?: return@mapNotNull null
            if (parts.size != 4) return@mapNotNull null

            val timestamp = parts[0].trim().toLongOrNull() ?: return@mapNotNull null
            val batteryLevel = parts[1].trim().toIntOrNull() ?: return@mapNotNull null
            val availableRam = parts[2].trim().toLongOrNull() ?: return@mapNotNull null
            val cpu = parts[3].trim().toFloatOrNull()

            if (timestamp <= 0L || batteryLevel !in 0..100) return@mapNotNull null

            HistorySampleEntity(
                timestamp = timestamp,
                batteryLevel = batteryLevel,
                availableRamBytes = availableRam,
                // The legacy schema had no total-RAM column.
                totalRamBytes = 0L,
                cpuPercent = cpu?.takeIf { it > 0f && it <= 100f },
                batteryTemperatureCelsius = null
            )
        }

    fun readLegacyFile(filesDir: File): List<HistorySampleEntity> {
        val file = File(filesDir, LEGACY_FILE_NAME)
        if (!file.exists() || !file.canRead()) return emptyList()
        return try {
            parseCsvLines(file.readLines())
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Renamed rather than deleted, so a bad import can still be recovered from
     * the user's device for one release.
     */
    fun markImported(filesDir: File): Boolean {
        val file = File(filesDir, LEGACY_FILE_NAME)
        if (!file.exists()) return false
        return try {
            file.renameTo(File(filesDir, LEGACY_FILE_NAME + IMPORTED_SUFFIX))
        } catch (_: Exception) {
            false
        }
    }
}
