package com.anhprgm.deviceinfo.data.source

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import com.anhprgm.deviceinfo.data.models.AppInfo
import com.anhprgm.deviceinfo.data.models.AppManagerInfo
import com.anhprgm.deviceinfo.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lists installed applications.
 *
 * Visibility comes from the `<queries>` element in the manifest, which exposes
 * every app with a LAUNCHER activity and needs no permission. We deliberately
 * do **not** request QUERY_ALL_PACKAGES: Google Play treats the full installed
 * inventory as sensitive data, gates it behind the Permissions Declaration
 * Form, and only approves it where broad visibility is the app's core purpose
 * (file managers, browsers, antivirus, banking). A device-info utility is not
 * in that set, so requesting it risks removal.
 *
 * Consequence to surface in the UI: system components without a launcher entry
 * are not listed.
 */
@Singleton
class PackageDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val io: CoroutineDispatcher
) {
    private val packageManager: PackageManager get() = context.packageManager

    suspend fun getAppManagerInfo(): AppManagerInfo = withContext(io) {
        val apps = queryLaunchablePackages()
            .mapNotNull { toAppInfo(it) }
            .sortedBy { it.appName.lowercase() }

        AppManagerInfo(
            systemApps = apps.count { it.isSystemApp },
            userApps = apps.count { !it.isSystemApp },
            apps = apps
        )
    }

    suspend fun getAppIcon(packageName: String): Drawable? = withContext(io) {
        try {
            packageManager.getApplicationIcon(packageName)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun findByPackageName(packageName: String): AppInfo? = withContext(io) {
        try {
            toAppInfo(getPackageInfoCompat(packageName))
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Resolving LAUNCHER intents rather than calling getInstalledPackages()
     * keeps the result set identical to what package visibility would allow
     * anyway, and makes the filtering explicit instead of silent.
     */
    private fun queryLaunchablePackages(): List<PackageInfo> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(0L)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, 0)
        }

        return resolved
            .map { it.activityInfo.packageName }
            .distinct()
            .mapNotNull { name ->
                try {
                    getPackageInfoCompat(name)
                } catch (_: Exception) {
                    null
                }
            }
    }

    private fun getPackageInfoCompat(packageName: String): PackageInfo =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
        }

    private fun toAppInfo(packageInfo: PackageInfo): AppInfo? {
        val applicationInfo = packageInfo.applicationInfo ?: return null
        return AppInfo(
            appName = packageManager.getApplicationLabel(applicationInfo).toString(),
            packageName = packageInfo.packageName,
            versionName = packageInfo.versionName,
            versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            },
            // sourceDir is the APK path; its length needs no permission at all,
            // unlike StorageStatsManager which requires PACKAGE_USAGE_STATS.
            apkSizeBytes = applicationInfo.sourceDir
                ?.let { path -> runCatching { File(path).length() }.getOrNull() }
                ?.takeIf { it > 0 },
            firstInstallTimeMillis = packageInfo.firstInstallTime,
            lastUpdateTimeMillis = packageInfo.lastUpdateTime,
            // FLAG_SYSTEM is the real answer; classifying by "com.android" prefix
            // misfiles updatable apps like com.android.chrome as system apps.
            isSystemApp = (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
            minSdk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                applicationInfo.minSdkVersion
            } else {
                null
            },
            targetSdk = applicationInfo.targetSdkVersion,
            permissions = packageInfo.requestedPermissions?.toList().orEmpty(),
            icon = null // Loaded lazily per row; eager loading stalls the list.
        )
    }
}
