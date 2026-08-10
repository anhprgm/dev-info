package com.anhprgm.deviceinfo.data.source

import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.anhprgm.deviceinfo.data.models.StorageInfo
import com.anhprgm.deviceinfo.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Storage breakdown that needs no permission at all.
 *
 * The per-app breakdown deliberately uses APK file sizes rather than
 * StorageStatsManager.queryStatsForPackage, which requires the
 * PACKAGE_USAGE_STATS special access. Media categories (photos/video/audio)
 * would need READ_MEDIA_* on API 33+, so they are left out of the default
 * view rather than prompting for a sensitive permission on first open.
 */
@Singleton
class StorageDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val packageDataSource: PackageDataSource,
    @IoDispatcher private val io: CoroutineDispatcher
) {
    suspend fun getStorageInfo(): StorageInfo = withContext(io) {
        val internal = StatFs(Environment.getDataDirectory().path)

        val external = runCatching {
            context.getExternalFilesDir(null)?.let { StatFs(it.path) }
        }.getOrNull()

        val apps = runCatching {
            packageDataSource.getAppManagerInfo().apps
        }.getOrNull()

        StorageInfo(
            totalBytes = internal.totalBytes,
            availableBytes = internal.availableBytes,
            appsBytes = apps?.mapNotNull { it.apkSizeBytes }?.sum(),
            installedAppCount = apps?.size ?: 0,
            externalTotalBytes = external?.totalBytes,
            externalAvailableBytes = external?.availableBytes,
            isEmulatedExternal = Environment.isExternalStorageEmulated()
        )
    }
}
