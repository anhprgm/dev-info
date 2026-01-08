package com.anhprgm.deviceinfo.data.models

data class AppInfo(
    val appName: String,
    val packageName: String,
    val versionName: String,
    val size: String,
    val installTime: String,
    val permissions: List<String>
)

data class AppManagerInfo(
    val totalApps: Int,
    val systemApps: Int,
    val userApps: Int,
    val apps: List<AppInfo>
)
