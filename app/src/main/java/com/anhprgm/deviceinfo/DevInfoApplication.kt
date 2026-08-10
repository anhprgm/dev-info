package com.anhprgm.deviceinfo

import android.app.Application
import android.os.Build
import android.os.StrictMode
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.anhprgm.deviceinfo.background.DeviceSamplingWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class DevInfoApplication : Application(), Configuration.Provider {

    /**
     * Lets Hilt construct @HiltWorker classes, which is what makes the sampling
     * worker able to reach the data layer without an Activity.
     */
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            enableStrictMode()
        }
        // Finally gives the History screen something to read: saveHistoryData()
        // previously had no call sites anywhere in the app.
        DeviceSamplingWorker.enqueue(this)
    }

    /**
     * Phase 1's acceptance gate: no disk or network work on the main thread.
     * This app reads /proc, sysfs, StatFs and the full package list, all of
     * which used to run on the UI thread.
     */
    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedClosableObjects()
                .detectLeakedSqlLiteObjects()
                .apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        detectUnsafeIntentLaunch()
                    }
                }
                .penaltyLog()
                .build()
        )
    }
}
