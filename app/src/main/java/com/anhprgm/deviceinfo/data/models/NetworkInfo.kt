package com.anhprgm.deviceinfo.data.models

data class NetworkInfo(
    val connectionType: ConnectionType,
    /**
     * Wi-Fi SSID. Null on API 29+ unless the app holds ACCESS_FINE_LOCATION and
     * location services are on — the platform returns "<unknown ssid>" there,
     * which we normalise to null rather than showing the placeholder.
     */
    val ssid: String?,
    val ipv4Address: String?,
    val ipv6Address: String?,
    /** 0..[maxSignalLevel]; null when not on Wi-Fi. */
    val signalLevel: Int?,
    val maxSignalLevel: Int,
    val rssiDbm: Int?,
    val linkSpeedMbps: Int?,
    val isMetered: Boolean
)
