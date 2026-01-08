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
            val level = WifiManager.calculateSignalLevel(rssi, 4)
            "$level/3 (${rssi} dBm)"
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
                
                // Get sensor size and megapixels
                val sensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
                val megapixels = if (sensorSize != null) {
                    val mp = (sensorSize.width * sensorSize.height) / 1000000.0
                    String.format("%.1f MP", mp)
                } else {
                    "N/A"
                }
                
                val imageSize = if (sensorSize != null) {
                    "${sensorSize.width} x ${sensorSize.height}"
                } else {
                    "N/A"
                }
                
                // Get focal length
                val focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
                val focalLength = if (focalLengths != null && focalLengths.isNotEmpty()) {
                    "${focalLengths[0]} mm"
                } else {
                    "N/A"
                }
                
                // Get aperture
                val apertures = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)
                val aperture = if (apertures != null && apertures.isNotEmpty()) {
                    "f/${apertures[0]}"
                } else {
                    "N/A"
                }
                
                // Get optical stabilization
                val opticalStabilization = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION)
                    ?.contains(CameraCharacteristics.LENS_OPTICAL_STABILIZATION_MODE_ON) ?: false
                
                // Get auto-exposure and auto-white balance lock support
                val autoExposureLock = characteristics.get(CameraCharacteristics.CONTROL_AE_LOCK_AVAILABLE) ?: false
                val autoWhiteBalanceLock = characteristics.get(CameraCharacteristics.CONTROL_AWB_LOCK_AVAILABLE) ?: false
                
                // Get supported output formats
                val streamConfigMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                val outputFormats = if (streamConfigMap != null) {
                    val formats = streamConfigMap.outputFormats
                    formats.joinToString(", ") { format ->
                        when (format) {
                            android.graphics.ImageFormat.JPEG -> "JPEG"
                            android.graphics.ImageFormat.RAW_SENSOR -> "RAW"
                            android.graphics.ImageFormat.YUV_420_888 -> "YUV"
                            android.graphics.ImageFormat.PRIVATE -> "PRIVATE"
                            else -> "Format $format"
                        }
                    }.take(50) + if (formats.size > 3) "..." else ""
                } else {
                    "N/A"
                }

                CameraDetail(
                    cameraId = id,
                    facing = facingString,
                    flashAvailable = flashAvailable,
                    sensorOrientation = sensorOrientation,
                    megapixels = megapixels,
                    focalLength = focalLength,
                    aperture = aperture,
                    opticalStabilization = opticalStabilization,
                    autoExposureLock = autoExposureLock,
                    autoWhiteBalanceLock = autoWhiteBalanceLock,
                    outputFormats = outputFormats,
                    imageSize = imageSize
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
            var cpuModel = Build.HARDWARE
            var line: String?
            
            // Read through cpuinfo to find processor model
            while (reader.readLine().also { line = it } != null) {
                if (line?.startsWith("model name") == true || 
                    line?.startsWith("Processor") == true ||
                    line?.startsWith("Hardware") == true) {
                    cpuModel = line?.substringAfter(":")?.trim() ?: Build.HARDWARE
                    break
                }
            }
            reader.close()
            cpuModel
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

    fun getSensorInfo(): SensorInfo {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
        val sensorList = sensorManager.getSensorList(android.hardware.Sensor.TYPE_ALL)
        
        val sensors = sensorList.map { sensor ->
            SensorDetail(
                name = sensor.name,
                type = getSensorTypeName(sensor.type),
                vendor = sensor.vendor,
                power = String.format("%.2f mA", sensor.power),
                maxRange = String.format("%.2f", sensor.maximumRange),
                resolution = String.format("%.4f", sensor.resolution)
            )
        }
        
        return SensorInfo(
            sensorCount = sensors.size,
            sensors = sensors
        )
    }

    private fun getSensorTypeName(type: Int): String {
        return when (type) {
            android.hardware.Sensor.TYPE_ACCELEROMETER -> "Accelerometer"
            android.hardware.Sensor.TYPE_GYROSCOPE -> "Gyroscope"
            android.hardware.Sensor.TYPE_LIGHT -> "Light"
            android.hardware.Sensor.TYPE_PROXIMITY -> "Proximity"
            android.hardware.Sensor.TYPE_MAGNETIC_FIELD -> "Magnetic Field"
            android.hardware.Sensor.TYPE_PRESSURE -> "Pressure"
            android.hardware.Sensor.TYPE_TEMPERATURE -> "Temperature"
            android.hardware.Sensor.TYPE_GRAVITY -> "Gravity"
            android.hardware.Sensor.TYPE_LINEAR_ACCELERATION -> "Linear Acceleration"
            android.hardware.Sensor.TYPE_ROTATION_VECTOR -> "Rotation Vector"
            android.hardware.Sensor.TYPE_RELATIVE_HUMIDITY -> "Relative Humidity"
            android.hardware.Sensor.TYPE_AMBIENT_TEMPERATURE -> "Ambient Temperature"
            else -> "Other (Type: $type)"
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun getAppManagerInfo(): AppManagerInfo {
        val packageManager = context.packageManager
        val packages = packageManager.getInstalledPackages(android.content.pm.PackageManager.GET_PERMISSIONS)
        
        var systemAppsCount = 0
        var userAppsCount = 0
        
        // Load app info WITHOUT icons for instant display
        val apps = packages.mapNotNull { packageInfo ->
            try {
                val appInfo = packageManager.getApplicationInfo(packageInfo.packageName, 0)
                val isSystemApp = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                
                if (isSystemApp) systemAppsCount++ else userAppsCount++
                
                val permissions = packageInfo.requestedPermissions?.toList() ?: emptyList()
                
                AppInfo(
                    appName = packageManager.getApplicationLabel(appInfo).toString(),
                    packageName = packageInfo.packageName,
                    versionName = packageInfo.versionName ?: "Unknown",
                    size = "N/A", // Size calculation requires additional permissions
                    installTime = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        .format(java.util.Date(packageInfo.firstInstallTime)),
                    permissions = permissions,
                    icon = null // Don't load icons during initial fetch
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }.sortedBy { it.appName.lowercase() }
        
        return AppManagerInfo(
            totalApps = packages.size,
            systemApps = systemAppsCount,
            userApps = userAppsCount,
            apps = apps
        )
    }
    
    // New method to load icon for a specific package on demand
    fun getAppIcon(packageName: String): android.graphics.drawable.Drawable? {
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: Exception) {
            null
        }
    }

    fun getMonitoringInfo(): MonitoringInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val cpuUsage = getCpuUsage()
        val ramUsed = memoryInfo.totalMem - memoryInfo.availMem
        val ramUsage = (ramUsed.toFloat() / memoryInfo.totalMem.toFloat()) * 100f
        
        return MonitoringInfo(
            cpuUsage = cpuUsage,
            ramUsage = ramUsage,
            ramUsed = formatBytes(ramUsed),
            ramTotal = formatBytes(memoryInfo.totalMem),
            timestamp = System.currentTimeMillis()
        )
    }

    private fun getCpuUsage(): Float {
        return try {
            // Read initial CPU stats
            val reader1 = BufferedReader(FileReader("/proc/stat"))
            val line1 = reader1.readLine()
            reader1.close()
            
            // Wait 200ms for better measurement accuracy
            Thread.sleep(200)
            
            // Read second CPU stats
            val reader2 = BufferedReader(FileReader("/proc/stat"))
            val line2 = reader2.readLine()
            reader2.close()
            
            // Parse first measurement
            val tokens1 = line1.split("\\s+".toRegex())
            val user1 = tokens1.getOrNull(1)?.toLongOrNull() ?: 0L
            val nice1 = tokens1.getOrNull(2)?.toLongOrNull() ?: 0L
            val system1 = tokens1.getOrNull(3)?.toLongOrNull() ?: 0L
            val idle1 = tokens1.getOrNull(4)?.toLongOrNull() ?: 0L
            val iowait1 = tokens1.getOrNull(5)?.toLongOrNull() ?: 0L
            val irq1 = tokens1.getOrNull(6)?.toLongOrNull() ?: 0L
            val softirq1 = tokens1.getOrNull(7)?.toLongOrNull() ?: 0L
            
            // Parse second measurement
            val tokens2 = line2.split("\\s+".toRegex())
            val user2 = tokens2.getOrNull(1)?.toLongOrNull() ?: 0L
            val nice2 = tokens2.getOrNull(2)?.toLongOrNull() ?: 0L
            val system2 = tokens2.getOrNull(3)?.toLongOrNull() ?: 0L
            val idle2 = tokens2.getOrNull(4)?.toLongOrNull() ?: 0L
            val iowait2 = tokens2.getOrNull(5)?.toLongOrNull() ?: 0L
            val irq2 = tokens2.getOrNull(6)?.toLongOrNull() ?: 0L
            val softirq2 = tokens2.getOrNull(7)?.toLongOrNull() ?: 0L
            
            // Calculate deltas
            val userDelta = user2 - user1
            val niceDelta = nice2 - nice1
            val systemDelta = system2 - system1
            val idleDelta = idle2 - idle1
            val iowaitDelta = iowait2 - iowait1
            val irqDelta = irq2 - irq1
            val softirqDelta = softirq2 - softirq1
            
            // Total CPU time delta
            val totalDelta = userDelta + niceDelta + systemDelta + idleDelta + iowaitDelta + irqDelta + softirqDelta
            
            // Active time (everything except idle)
            val activeDelta = userDelta + niceDelta + systemDelta + iowaitDelta + irqDelta + softirqDelta
            
            // Calculate usage percentage
            if (totalDelta > 0) {
                val usage = (activeDelta.toFloat() / totalDelta.toFloat()) * 100f
                usage.coerceIn(0f, 100f)
            } else {
                0f
            }
        } catch (e: Exception) {
            e.printStackTrace()
            0f
        }
    }
            val iowait2 = tokens2.getOrNull(5)?.toLongOrNull() ?: 0L
            val irq2 = tokens2.getOrNull(6)?.toLongOrNull() ?: 0L
            val softirq2 = tokens2.getOrNull(7)?.toLongOrNull() ?: 0L
            
            // Calculate deltas
            val userDelta = user2 - user1
            val niceDelta = nice2 - nice1
            val systemDelta = system2 - system1
            val idleDelta = idle2 - idle1
            val iowaitDelta = iowait2 - iowait1
            val irqDelta = irq2 - irq1
            val softirqDelta = softirq2 - softirq1
            
            val activeTime = userDelta + niceDelta + systemDelta + irqDelta + softirqDelta
            val totalTime = activeTime + idleDelta + iowaitDelta
            
            if (totalTime > 0) {
                (activeTime.toFloat() / totalTime.toFloat() * 100f).coerceIn(0f, 100f)
            } else {
                0f
            }
        } catch (e: Exception) {
            e.printStackTrace()
            0f
        }
    }

    fun runBenchmark(): BenchmarkResult {
        val startTime = System.currentTimeMillis()
        
        // CPU single-core benchmark
        val singleCoreScore = benchmarkSingleCore()
        
        // CPU multi-core benchmark
        val multiCoreScore = benchmarkMultiCore()
        
        // Memory benchmark
        val memoryScore = benchmarkMemory()
        
        val cpuScore = (singleCoreScore + multiCoreScore) / 2
        val overallScore = (cpuScore + memoryScore) / 2
        
        val duration = System.currentTimeMillis() - startTime
        
        return BenchmarkResult(
            cpuScore = cpuScore,
            singleCoreScore = singleCoreScore,
            multiCoreScore = multiCoreScore,
            memoryScore = memoryScore,
            overallScore = overallScore,
            duration = duration
        )
    }

    private fun benchmarkSingleCore(): Int {
        val iterations = 2000000 // Increased from 1000000
        var result = 0.0
        val startTime = System.nanoTime()
        
        // Perform mathematical operations
        for (i in 0 until iterations) {
            result += sqrt(i.toDouble())
            result += i.toDouble().pow(0.5)
        }
        
        val endTime = System.nanoTime()
        val durationMs = (endTime - startTime) / 1000000.0 // Convert to milliseconds
        
        // Baseline: ~1000ms for a good device with 2M iterations, scale to 0-1000
        // Lower time = higher score
        val score = (1000.0 / durationMs * 1000.0).coerceIn(0.0, 1000.0)
        return score.toInt()
    }

    private fun benchmarkMultiCore(): Int {
        val cores = Runtime.getRuntime().availableProcessors()
        val iterations = 1000000 // Increased from 500000
        val startTime = System.nanoTime()
        
        val threads = (0 until cores).map {
            Thread {
                var result = 0.0
                for (i in 0 until iterations) {
                    result += sqrt(i.toDouble())
                    result += i.toDouble().pow(0.5)
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        val endTime = System.nanoTime()
        val durationMs = (endTime - startTime) / 1000000.0
        
        // Baseline: ~300ms for a good multi-core device, scale to 0-1000
        // Lower time = higher score
        val score = (300.0 / durationMs * 1000.0).coerceIn(0.0, 1000.0)
        return score.toInt()
    }

    private fun benchmarkMemory(): Int {
        val arraySize = 1000000
        val startTime = System.nanoTime()
        
        // Memory allocation and access test
        val array = IntArray(arraySize) { it }
        var sum = 0L
        
        // Multiple passes to test memory bandwidth - increased to 5 passes
        repeat(5) {
            for (i in 0 until arraySize) {
                sum += array[i]
            }
        }
        
        val endTime = System.nanoTime()
        val durationMs = (endTime - startTime) / 1000000.0
        
        // Baseline: ~150ms for good memory with 5 passes, scale to 0-1000
        // Lower time = higher score
        val score = (150.0 / durationMs * 1000.0).coerceIn(0.0, 1000.0)
        return score.toInt()
    }
}
