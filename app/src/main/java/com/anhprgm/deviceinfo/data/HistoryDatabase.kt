package com.anhprgm.deviceinfo.data

import android.content.Context
import com.anhprgm.deviceinfo.data.models.HistoryData
import com.anhprgm.deviceinfo.data.models.HistoryInfo
import java.io.File

class HistoryDatabase(private val context: Context) {
    private val historyFile = File(context.filesDir, "device_history.txt")
    private val maxEntries = 100 // Keep last 100 entries

    fun saveHistoryData(data: HistoryData) {
        try {
            val line = "${data.timestamp},${data.batteryLevel},${data.availableRam},${data.cpuUsage}\n"
            historyFile.appendText(line)
            
            // Keep only last maxEntries
            val lines = historyFile.readLines()
            if (lines.size > maxEntries) {
                val newLines = lines.takeLast(maxEntries)
                historyFile.writeText(newLines.joinToString("\n") + "\n")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getHistoryData(): HistoryInfo {
        return try {
            if (!historyFile.exists()) {
                return HistoryInfo(emptyList())
            }
            
            val history = historyFile.readLines()
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    try {
                        val parts = line.split(",")
                        if (parts.size == 4) {
                            HistoryData(
                                timestamp = parts[0].toLong(),
                                batteryLevel = parts[1].toInt(),
                                availableRam = parts[2].toLong(),
                                cpuUsage = parts[3].toFloat()
                            )
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }
            
            HistoryInfo(history)
        } catch (e: Exception) {
            HistoryInfo(emptyList())
        }
    }

    fun clearHistory() {
        try {
            if (historyFile.exists()) {
                historyFile.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
