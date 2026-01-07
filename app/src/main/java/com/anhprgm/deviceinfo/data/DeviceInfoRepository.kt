package com.anhprgm.deviceinfo.data

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.DisplayMetrics
import android.view.WindowManager
import com.anhprgm.deviceinfo.data.models.*
import java.io.BufferedReader
import java.io.FileReader
import java.net.Inet4Address
import java.net.NetworkInterface
import kotlin.math.pow
import kotlin.math.sqrt

class DeviceInfoRepository(private val context: Context) {

    fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            deviceName = "${Build.MANUFACTURER} ${Build.MODEL}",
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            brand = Build.BRAND,
            androidVersion = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT,
            buildFingerprint = Build.FINGERPRINT,
            securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Build.VERSION.SECURITY_PATCH
            } else {
                "N/A"
            }
        )
    }

    fun getHardwareInfo(): HardwareInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalRam = formatBytes(memoryInfo.totalMem)
        val availableRam = formatBytes(memoryInfo.availMem)

        val statFs = StatFs(Environment.getDataDirectory().path)
        val totalStorage = formatBytes(statFs.totalBytes)
        val availableStorage = formatBytes(statFs.availableBytes)

        val cpuInfo = getCpuInfo()
        val cpuCores = Runtime.getRuntime().availableProcessors()

        return HardwareInfo(
            totalRam = totalRam,
            availableRam = availableRam,
            totalStorage = totalStorage,
            availableStorage = availableStorage,
            cpuInfo = cpuInfo,
            cpuCores = cpuCores
        )
    }

    fun getBatteryInfo(): BatteryInfo {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }

        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level != -1 && scale != -1) {
            (level * 100 / scale.toFloat()).toInt()
        } else {
            0
        }

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val chargingStatus = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "Unknown"
        }

        val temperature = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        val tempCelsius = temperature / 10.0

        val voltage = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
        val voltageVolts = voltage / 1000.0

        val health = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        val healthStatus = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }

        val technology = batteryStatus?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"

        return BatteryInfo(
            level = batteryPct,
            chargingStatus = chargingStatus,
            temperature = String.format("%.1f°C", tempCelsius),
            voltage = String.format("%.2fV", voltageVolts),
            health = healthStatus,
            technology = technology
        )
    }

    @SuppressLint("MissingPermission")
    fun getNetworkInfo(): NetworkInfo {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        val connectionType = when {
            capabilities == null -> "Disconnected"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile Data"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Unknown"
        }

        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val networkName = if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
            wifiInfo.ssid.replace("\"", "")
        } else if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true) {
            "Mobile Network"
        } else {
            "N/A"
        }

        val ipAddress = getIpAddress()
        
        val signalStrength = if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
            val rssi = wifiInfo.rssi
            val level = WifiManager.calculateSignalLevel(rssi, 5)
            "$level/4 (${rssi} dBm)"
        } else {
            "N/A"
        }

        return NetworkInfo(
            connectionType = connectionType,
            networkName = networkName,
            ipAddress = ipAddress,
            signalStrength = signalStrength
        )
    }

    fun getDisplayInfo(): DisplayInfo {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val display = context.display
            display?.getRealMetrics(displayMetrics)
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        }

        val widthPixels = displayMetrics.widthPixels
        val heightPixels = displayMetrics.heightPixels
        val resolution = "${widthPixels} x ${heightPixels}"

        val xdpi = displayMetrics.xdpi
        val ydpi = displayMetrics.ydpi
        val widthInches = widthPixels / xdpi
        val heightInches = heightPixels / ydpi
        val diagonalInches = sqrt(widthInches.pow(2) + heightInches.pow(2))
        val screenSize = String.format("%.2f inches", diagonalInches)

        val density = displayMetrics.density
        val densityDpi = displayMetrics.densityDpi
        val pixelDensity = "$densityDpi dpi (${getDensityString(density)})"

        val refreshRate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val display = context.display
            "${display?.refreshRate?.toInt() ?: 60} Hz"
        } else {
            @Suppress("DEPRECATION")
            "${windowManager.defaultDisplay.refreshRate.toInt()} Hz"
        }

        return DisplayInfo(
            resolution = resolution,
            screenSize = screenSize,
            pixelDensity = pixelDensity,
            refreshRate = refreshRate
        )
    }

    @SuppressLint("MissingPermission")
    fun getCameraInfo(): CameraInfo {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraIds = try {
            cameraManager.cameraIdList
        } catch (e: Exception) {
            arrayOf()
        }

        val cameras = cameraIds.mapNotNull { id ->
            try {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                val facingString = when (facing) {
                    CameraCharacteristics.LENS_FACING_FRONT -> "Front"
                    CameraCharacteristics.LENS_FACING_BACK -> "Back"
                    CameraCharacteristics.LENS_FACING_EXTERNAL -> "External"
                    else -> "Unknown"
                }
                
                val flashAvailable = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                val sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0

                CameraDetail(
                    cameraId = id,
                    facing = facingString,
                    flashAvailable = flashAvailable,
                    sensorOrientation = sensorOrientation
                )
            } catch (e: Exception) {
                null
            }
        }

        return CameraInfo(
            cameraCount = cameras.size,
            cameras = cameras
        )
    }

    private fun formatBytes(bytes: Long): String {
        val gb = bytes / (1024.0 * 1024.0 * 1024.0)
        return if (gb >= 1.0) {
            String.format("%.2f GB", gb)
        } else {
            val mb = bytes / (1024.0 * 1024.0)
            String.format("%.2f MB", mb)
        }
    }

    private fun getCpuInfo(): String {
        return try {
            val reader = BufferedReader(FileReader("/proc/cpuinfo"))
            val line = reader.readLine()
            reader.close()
            line?.substringAfter(":")?.trim() ?: Build.HARDWARE
        } catch (e: Exception) {
            Build.HARDWARE
        }
    }

    private fun getIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress ?: "N/A"
                    }
                }
            }
        } catch (e: Exception) {
            return "N/A"
        }
        return "N/A"
    }

    private fun getDensityString(density: Float): String {
        return when {
            density >= 4.0 -> "xxxhdpi"
            density >= 3.0 -> "xxhdpi"
            density >= 2.0 -> "xhdpi"
            density >= 1.5 -> "hdpi"
            density >= 1.0 -> "mdpi"
            else -> "ldpi"
        }
    }
}
