package com.anhprgm.deviceinfo.data.source

import android.os.Build
import android.os.SystemClock
import com.anhprgm.deviceinfo.data.models.DeviceInfo
import com.anhprgm.deviceinfo.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceDataSource @Inject constructor(
    @IoDispatcher private val io: CoroutineDispatcher
) {
    suspend fun getDeviceInfo(): DeviceInfo = withContext(io) {
        DeviceInfo(
            deviceName = "${Build.MANUFACTURER} ${Build.MODEL}",
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            brand = Build.BRAND,
            device = Build.DEVICE,
            product = Build.PRODUCT,
            board = Build.BOARD,
            hardware = Build.HARDWARE,
            androidVersion = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT,
            buildFingerprint = Build.FINGERPRINT,
            buildId = Build.ID,
            bootloader = Build.BOOTLOADER,
            // SECURITY_PATCH exists from API 23; minSdk is 24, so it is always present.
            securityPatch = Build.VERSION.SECURITY_PATCH?.takeIf { it.isNotBlank() },
            supportedAbis = Build.SUPPORTED_ABIS?.toList().orEmpty(),
            uptimeMillis = SystemClock.elapsedRealtime()
        )
    }
}
