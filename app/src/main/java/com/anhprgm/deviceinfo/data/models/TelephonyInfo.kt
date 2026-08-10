package com.anhprgm.deviceinfo.data.models

/**
 * SIM and carrier information.
 *
 * Split by what the platform actually allows:
 *  - the fields above [dataNetworkType] need no permission;
 *  - [dataNetworkType] and signal strength require READ_PHONE_STATE at runtime;
 *  - IMEI/IMSI/serial are impossible — getImei() has needed the signature-level
 *    READ_PRIVILEGED_PHONE_STATE since API 29 and Build.getSerial() returns
 *    "UNKNOWN". There is no workaround, so the app does not pretend otherwise.
 */
data class TelephonyInfo(
    val hasTelephony: Boolean,
    val simState: SimState,
    val networkOperatorName: String?,
    val simOperatorName: String?,
    /** MCC+MNC, e.g. "45201". */
    val simOperator: String?,
    val simCountryIso: String?,
    val phoneType: PhoneType,
    val activeModemCount: Int?,
    /** Null unless READ_PHONE_STATE has been granted. */
    val dataNetworkType: String?,
    val isRoaming: Boolean?,
    val hasPhoneStatePermission: Boolean
)

enum class SimState { ABSENT, NETWORK_LOCKED, PIN_REQUIRED, PUK_REQUIRED, READY, NOT_READY, PERM_DISABLED, CARD_IO_ERROR, CARD_RESTRICTED, UNKNOWN }

enum class PhoneType { NONE, GSM, CDMA, SIP, UNKNOWN }
