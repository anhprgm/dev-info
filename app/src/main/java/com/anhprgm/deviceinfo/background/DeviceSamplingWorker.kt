package com.anhprgm.deviceinfo.background

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.anhprgm.deviceinfo.data.monitor.SystemMonitor
import com.anhprgm.deviceinfo.data.repository.HistoryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Periodic history sampling.
 *
 * This is what finally makes the History screen work: `saveHistoryData()` used
 * to have no call sites at all, so the feature's read path, chart and clear
 * button were all built while nothing ever wrote a row.
 *
 * Deliberately a worker rather than a foreground service. A long-lived
 * notification with live data means an FGS, and on API 34+ every FGS must
 * declare a type — none of which fit "device monitoring".
 * FOREGROUND_SERVICE_TYPE_SPECIAL_USE needs a written justification reviewed by
 * Google Play and is routinely rejected for convenience monitoring. The cost of
 * this design is 15-minute granularity, which for battery and RAM is fine.
 */
@HiltWorker
class DeviceSamplingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val systemMonitor: SystemMonitor,
    private val historyRepository: HistoryRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        historyRepository.record(systemMonitor.snapshot())
        Result.success()
    } catch (_: Exception) {
        // Retrying a missed sample is pointless — the next period covers it.
        Result.success()
    }

    companion object {
        private const val WORK_NAME = "device-sampling"

        /** WorkManager's periodic floor is 15 minutes; asking for less is ignored. */
        private const val INTERVAL_MINUTES = 15L

        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<DeviceSamplingWorker>(
                INTERVAL_MINUTES, TimeUnit.MINUTES
            ).setConstraints(
                // Sampling is not worth waking a nearly dead device for.
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                // KEEP, so re-launching the app does not reset the schedule.
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
