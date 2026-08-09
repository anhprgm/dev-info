package com.anhprgm.deviceinfo.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.anhprgm.deviceinfo.data.db.entity.HistorySampleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    /** A Flow so the history screen updates live instead of showing a snapshot. */
    @Query("SELECT * FROM history_sample ORDER BY timestamp ASC")
    fun observeAll(): Flow<List<HistorySampleEntity>>

    @Query("SELECT * FROM history_sample WHERE timestamp >= :since ORDER BY timestamp ASC")
    fun observeSince(since: Long): Flow<List<HistorySampleEntity>>

    @Query("SELECT * FROM history_sample ORDER BY timestamp DESC LIMIT :limit")
    suspend fun latest(limit: Int): List<HistorySampleEntity>

    @Insert
    suspend fun insert(sample: HistorySampleEntity)

    @Insert
    suspend fun insertAll(samples: List<HistorySampleEntity>)

    @Query("DELETE FROM history_sample WHERE timestamp < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long): Int

    @Query("DELETE FROM history_sample")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM history_sample")
    suspend fun count(): Int
}
