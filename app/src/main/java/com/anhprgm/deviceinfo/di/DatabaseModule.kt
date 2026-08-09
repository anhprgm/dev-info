package com.anhprgm.deviceinfo.di

import android.content.Context
import androidx.room.Room
import com.anhprgm.deviceinfo.data.db.DevInfoDatabase
import com.anhprgm.deviceinfo.data.db.dao.BenchmarkDao
import com.anhprgm.deviceinfo.data.db.dao.HistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DevInfoDatabase =
        Room.databaseBuilder(context, DevInfoDatabase::class.java, DevInfoDatabase.NAME)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideHistoryDao(database: DevInfoDatabase): HistoryDao = database.historyDao()

    @Provides
    fun provideBenchmarkDao(database: DevInfoDatabase): BenchmarkDao = database.benchmarkDao()
}
