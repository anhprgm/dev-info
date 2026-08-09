package com.anhprgm.deviceinfo.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.anhprgm.deviceinfo.data.db.entity.BenchmarkResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BenchmarkDao {

    @Query("SELECT * FROM benchmark_result ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<BenchmarkResultEntity>>

    @Query("SELECT * FROM benchmark_result ORDER BY timestamp DESC LIMIT 1")
    suspend fun latest(): BenchmarkResultEntity?

    @Insert
    suspend fun insert(result: BenchmarkResultEntity): Long

    @Query("DELETE FROM benchmark_result")
    suspend fun clear()
}
