package com.anhprgm.deviceinfo.data.models

import android.graphics.drawable.Drawable

data class AppInfo(
    val appName: String,
    val packageName: String,
    val versionName: String?,
    val versionCode: Long,
    /** APK size from `ApplicationInfo.sourceDir` — needs no permission. */
    val apkSizeBytes: Long?,
    val firstInstallTimeMillis: Long,
    val lastUpdateTimeMillis: Long,
    /** Derived from FLAG_SYSTEM, not from a package-name prefix guess. */
    val isSystemApp: Boolean,
    val minSdk: Int?,
    val targetSdk: Int,
    val permissions: List<String>,
    val icon: Drawable? = null
)

data class AppManagerInfo(
    val systemApps: Int,
    val userApps: Int,
    val apps: List<AppInfo>
) {
    /** Derived so it can never disagree with [apps], as the old field did. */
    val totalApps: Int get() = apps.size
}
