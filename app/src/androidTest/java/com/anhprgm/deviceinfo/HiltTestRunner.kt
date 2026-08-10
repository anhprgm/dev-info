package com.anhprgm.deviceinfo

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Swaps in HiltTestApplication so instrumented tests get a Hilt component
 * without DevInfoApplication's WorkManager scheduling running first.
 */
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        classLoader: ClassLoader?,
        className: String?,
        context: Context?
    ): Application = super.newApplication(classLoader, HiltTestApplication::class.java.name, context)
}
