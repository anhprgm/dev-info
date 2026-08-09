package com.anhprgm.deviceinfo.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.anhprgm.deviceinfo.data.db.dao.BenchmarkDao
import com.anhprgm.deviceinfo.data.db.dao.HistoryDao
import com.anhprgm.deviceinfo.data.db.entity.BenchmarkResultEntity
import com.anhprgm.deviceinfo.data.db.entity.HistorySampleEntity

@Database(
    entities = [HistorySampleEntity::class, BenchmarkResultEntity::class],
    version = 1,
    exportSchema = true
)
abstract class DevInfoDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun benchmarkDao(): BenchmarkDao

    companion object {
        const val NAME = "devinfo.db"
    }
}
