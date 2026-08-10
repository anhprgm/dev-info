package com.anhprgm.deviceinfo.data.source

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.anhprgm.deviceinfo.data.models.PhoneType
import com.anhprgm.deviceinfo.data.models.SimState
import com.anhprgm.deviceinfo.data.models.TelephonyInfo
import com.anhprgm.deviceinfo.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelephonyDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val io: CoroutineDispatcher
) {
    fun hasPhoneStatePermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) ==
            PackageManager.PERMISSION_GRANTED

    suspend fun getTelephonyInfo(): TelephonyInfo = withContext(io) {
        val hasTelephony = context.packageManager
            .hasSystemFeature(PackageManager.FEATURE_TELEPHONY)

        if (!hasTelephony) {
            return@withContext TelephonyInfo(
                hasTelephony = false,
                simState = SimState.ABSENT,
                networkOperatorName = null,
                simOperatorName = null,
                simOperator = null,
                simCountryIso = null,
                phoneType = PhoneType.NONE,
                activeModemCount = null,
                dataNetworkType = null,
                isRoaming = null,
                hasPhoneStatePermission = false
            )
        }

        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val granted = hasPhoneStatePermission()

        TelephonyInfo(
            hasTelephony = true,
            simState = simStateOf(runCatching { tm.simState }.getOrNull()),
            networkOperatorName = tm.networkOperatorName?.takeIf { it.isNotBlank() },
            simOperatorName = tm.simOperatorName?.takeIf { it.isNotBlank() },
            simOperator = tm.simOperator?.takeIf { it.isNotBlank() },
            simCountryIso = tm.simCountryIso?.takeIf { it.isNotBlank() }?.uppercase(),
            phoneType = phoneTypeOf(runCatching { tm.phoneType }.getOrNull()),
            activeModemCount = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                runCatching { tm.activeModemCount }.getOrNull()
            } else {
                null
            },
            // The only way to report "5G / LTE / HSPA+", and it is gated.
            // The permission check is inlined here rather than hoisted so lint
            // can see it; the runCatching still guards against an OEM throwing
            // SecurityException anyway.
            dataNetworkType = if (
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                runCatching { networkTypeName(tm.dataNetworkType) }.getOrNull()
            } else {
                null
            },
            isRoaming = runCatching { tm.isNetworkRoaming }.getOrNull(),
            hasPhoneStatePermission = granted
        )
    }

    private fun simStateOf(value: Int?): SimState = when (value) {
        TelephonyManager.SIM_STATE_ABSENT -> SimState.ABSENT
        TelephonyManager.SIM_STATE_NETWORK_LOCKED -> SimState.NETWORK_LOCKED
        TelephonyManager.SIM_STATE_PIN_REQUIRED -> SimState.PIN_REQUIRED
        TelephonyManager.SIM_STATE_PUK_REQUIRED -> SimState.PUK_REQUIRED
        TelephonyManager.SIM_STATE_READY -> SimState.READY
        TelephonyManager.SIM_STATE_NOT_READY -> SimState.NOT_READY
        TelephonyManager.SIM_STATE_PERM_DISABLED -> SimState.PERM_DISABLED
        TelephonyManager.SIM_STATE_CARD_IO_ERROR -> SimState.CARD_IO_ERROR
        TelephonyManager.SIM_STATE_CARD_RESTRICTED -> SimState.CARD_RESTRICTED
        else -> SimState.UNKNOWN
    }

    private fun phoneTypeOf(value: Int?): PhoneType = when (value) {
        TelephonyManager.PHONE_TYPE_NONE -> PhoneType.NONE
        TelephonyManager.PHONE_TYPE_GSM -> PhoneType.GSM
        TelephonyManager.PHONE_TYPE_CDMA -> PhoneType.CDMA
        TelephonyManager.PHONE_TYPE_SIP -> PhoneType.SIP
        else -> PhoneType.UNKNOWN
    }

    /** Radio technology names are industry identifiers, so they are not translated. */
    private fun networkTypeName(type: Int): String = when (type) {
        TelephonyManager.NETWORK_TYPE_NR -> "5G NR"
        TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
        TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPA+"
        TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
        TelephonyManager.NETWORK_TYPE_HSDPA -> "HSDPA"
        TelephonyManager.NETWORK_TYPE_HSUPA -> "HSUPA"
        TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
        TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
        TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
        TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
        TelephonyManager.NETWORK_TYPE_EVDO_0 -> "EVDO rev.0"
        TelephonyManager.NETWORK_TYPE_EVDO_A -> "EVDO rev.A"
        TelephonyManager.NETWORK_TYPE_EVDO_B -> "EVDO rev.B"
        TelephonyManager.NETWORK_TYPE_1xRTT -> "1xRTT"
        TelephonyManager.NETWORK_TYPE_IDEN -> "iDEN"
        TelephonyManager.NETWORK_TYPE_EHRPD -> "eHRPD"
        TelephonyManager.NETWORK_TYPE_IWLAN -> "IWLAN"
        TelephonyManager.NETWORK_TYPE_GSM -> "GSM"
        TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "TD-SCDMA"
        else -> "Unknown"
    }
}
