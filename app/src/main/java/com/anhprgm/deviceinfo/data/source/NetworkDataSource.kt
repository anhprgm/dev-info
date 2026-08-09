package com.anhprgm.deviceinfo.data.source

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import com.anhprgm.deviceinfo.data.models.ConnectionType
import com.anhprgm.deviceinfo.data.models.NetworkInfo
import com.anhprgm.deviceinfo.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.NetworkInterface
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val io: CoroutineDispatcher
) {
    /** SSID the platform returns when it is withholding the real one. */
    private val unknownSsid = "<unknown ssid>"

    suspend fun getNetworkInfo(): NetworkInfo = withContext(io) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)

        val connectionType = when {
            capabilities == null -> ConnectionType.DISCONNECTED
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> ConnectionType.VPN
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> ConnectionType.BLUETOOTH
            else -> ConnectionType.UNKNOWN
        }

        val wifiInfo = if (connectionType == ConnectionType.WIFI) currentWifiInfo(capabilities) else null
        val addresses = localAddresses()
        val maxLevel = maxSignalLevel()

        NetworkInfo(
            connectionType = connectionType,
            ssid = wifiInfo?.ssid
                ?.removeSurrounding("\"")
                // On API 29+ this is withheld unless the app holds ACCESS_FINE_LOCATION
                // *and* location services are on. Show nothing rather than the placeholder.
                ?.takeIf { it.isNotBlank() && !it.equals(unknownSsid, ignoreCase = true) },
            ipv4Address = addresses.first,
            ipv6Address = addresses.second,
            signalLevel = wifiInfo?.rssi?.let { signalLevel(it, maxLevel) },
            maxSignalLevel = maxLevel,
            rssiDbm = wifiInfo?.rssi?.takeIf { it != WIFI_INVALID_RSSI },
            linkSpeedMbps = wifiInfo?.linkSpeed?.takeIf { it > 0 },
            isMetered = capabilities?.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_NOT_METERED
            )?.not() ?: false
        )
    }

    /**
     * `WifiManager.getConnectionInfo()` is deprecated at API 31 in favour of
     * reading the WifiInfo straight off the network capabilities.
     */
    @Suppress("DEPRECATION")
    private fun currentWifiInfo(capabilities: NetworkCapabilities?): WifiInfo? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            capabilities?.transportInfo as? WifiInfo
        } else {
            val wifiManager = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiManager.connectionInfo
        }
    } catch (_: Exception) {
        null
    }

    /**
     * The two-argument `calculateSignalLevel` is deprecated at API 30; the
     * replacement pairs a one-argument call with the system's own bucket count.
     */
    @Suppress("DEPRECATION")
    private fun maxSignalLevel(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val wifiManager = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiManager.maxSignalLevel
        } else {
            LEGACY_SIGNAL_BUCKETS - 1
        }

    @Suppress("DEPRECATION")
    private fun signalLevel(rssi: Int, maxLevel: Int): Int? {
        if (rssi == WIFI_INVALID_RSSI) return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val wifiManager = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiManager.calculateSignalLevel(rssi)
        } else {
            WifiManager.calculateSignalLevel(rssi, LEGACY_SIGNAL_BUCKETS)
        }.coerceIn(0, maxLevel)
    }

    /** First non-loopback IPv4 and IPv6 address. */
    private fun localAddresses(): Pair<String?, String?> = try {
        var v4: String? = null
        var v6: String? = null
        NetworkInterface.getNetworkInterfaces()?.asSequence().orEmpty()
            .filter { it.isUp && !it.isLoopback }
            .flatMap { it.inetAddresses.asSequence() }
            .forEach { address ->
                when {
                    address.isLoopbackAddress -> Unit
                    v4 == null && address is Inet4Address -> v4 = address.hostAddress
                    v6 == null && address is Inet6Address && !address.isLinkLocalAddress ->
                        // Strip the zone index (e.g. "%wlan0") for readability.
                        v6 = address.hostAddress?.substringBefore('%')
                }
            }
        v4 to v6
    } catch (_: Exception) {
        null to null
    }

    private companion object {
        const val WIFI_INVALID_RSSI = -127
        const val LEGACY_SIGNAL_BUCKETS = 5
    }
}
