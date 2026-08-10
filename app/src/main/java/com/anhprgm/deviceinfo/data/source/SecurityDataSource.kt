package com.anhprgm.deviceinfo.data.source

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Build
import com.anhprgm.deviceinfo.data.models.EncryptionStatus
import com.anhprgm.deviceinfo.data.models.SecurityInfo
import com.anhprgm.deviceinfo.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Locally observable integrity signals.
 *
 * Every root check here is a heuristic and is labelled as such in the UI.
 * There is no reliable way for an app to prove a device is unmodified; Play
 * Integrity would need a backend to decode its token, so it is out of scope.
 *
 * System properties are read via `getprop` because android.os.SystemProperties
 * is a hidden API and blocked by the non-SDK interface restrictions.
 */
@Singleton
class SecurityDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val io: CoroutineDispatcher
) {
    suspend fun getSecurityInfo(): SecurityInfo = withContext(io) {
        val verifiedBootState = getProp("ro.boot.verifiedbootstate")
        val flashLocked = getProp("ro.boot.flash.locked")

        SecurityInfo(
            encryptionStatus = encryptionStatus(),
            bootloader = Build.BOOTLOADER,
            verifiedBootState = verifiedBootState,
            bootloaderLocked = when {
                flashLocked == "1" -> true
                flashLocked == "0" -> false
                verifiedBootState.equals("green", ignoreCase = true) -> true
                verifiedBootState.equals("orange", ignoreCase = true) -> false
                else -> null
            },
            selinuxMode = getProp("ro.boot.selinux")
                ?: getProp("ro.build.selinux")?.let { if (it == "1") "enforcing" else it },
            rootIndicators = detectRootIndicators(),
            hasTestKeys = Build.TAGS?.contains("test-keys") == true,
            isDebuggableBuild = getProp("ro.debuggable") == "1",
            securityPatch = Build.VERSION.SECURITY_PATCH?.takeIf { it.isNotBlank() }
        )
    }

    private fun encryptionStatus(): EncryptionStatus {
        val dpm = runCatching {
            context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        }.getOrNull() ?: return EncryptionStatus.UNKNOWN

        return when (runCatching { dpm.storageEncryptionStatus }.getOrNull()) {
            DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED -> EncryptionStatus.UNSUPPORTED
            DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE -> EncryptionStatus.INACTIVE
            DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE -> EncryptionStatus.ACTIVE
            DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_DEFAULT_KEY ->
                EncryptionStatus.ACTIVE_DEFAULT_KEY
            DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER ->
                EncryptionStatus.ACTIVE_PER_USER
            else -> EncryptionStatus.UNKNOWN
        }
    }

    /**
     * Returns the specific signals observed, not a boolean verdict, so the UI
     * can show *why* it thinks something and the user can judge for themselves.
     */
    private fun detectRootIndicators(): List<String> = buildList {
        SU_PATHS.filter { path ->
            runCatching { File(path).exists() }.getOrDefault(false)
        }.forEach { add(it) }

        if (Build.TAGS?.contains("test-keys") == true) add("Build.TAGS: test-keys")
        if (getProp("ro.debuggable") == "1") add("ro.debuggable=1")
        if (getProp("ro.secure") == "0") add("ro.secure=0")
    }

    private fun getProp(key: String): String? = runCatching {
        val process = ProcessBuilder("/system/bin/getprop", key)
            .redirectErrorStream(true)
            .start()
        val value = process.inputStream.bufferedReader().use { it.readLine() }?.trim()
        process.waitFor()
        value?.takeIf { it.isNotEmpty() }
    }.getOrNull()

    private companion object {
        val SU_PATHS = listOf(
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
    }
}
