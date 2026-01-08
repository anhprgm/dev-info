package com.anhprgm.deviceinfo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anhprgm.deviceinfo.data.DeviceInfoRepository
import com.anhprgm.deviceinfo.data.HistoryDatabase
import com.anhprgm.deviceinfo.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DeviceInfoViewModel(
    private val repository: DeviceInfoRepository,
    private val historyDatabase: HistoryDatabase
) : ViewModel() {

    private val _deviceInfo = MutableStateFlow<DeviceInfo?>(null)
    val deviceInfo: StateFlow<DeviceInfo?> = _deviceInfo.asStateFlow()

    private val _hardwareInfo = MutableStateFlow<HardwareInfo?>(null)
    val hardwareInfo: StateFlow<HardwareInfo?> = _hardwareInfo.asStateFlow()

    private val _batteryInfo = MutableStateFlow<BatteryInfo?>(null)
    val batteryInfo: StateFlow<BatteryInfo?> = _batteryInfo.asStateFlow()

    private val _networkInfo = MutableStateFlow<NetworkInfo?>(null)
    val networkInfo: StateFlow<NetworkInfo?> = _networkInfo.asStateFlow()

    private val _displayInfo = MutableStateFlow<DisplayInfo?>(null)
    val displayInfo: StateFlow<DisplayInfo?> = _displayInfo.asStateFlow()

    private val _cameraInfo = MutableStateFlow<CameraInfo?>(null)
    val cameraInfo: StateFlow<CameraInfo?> = _cameraInfo.asStateFlow()

    private val _sensorInfo = MutableStateFlow<SensorInfo?>(null)
    val sensorInfo: StateFlow<SensorInfo?> = _sensorInfo.asStateFlow()

    private val _historyInfo = MutableStateFlow<HistoryInfo?>(null)
    val historyInfo: StateFlow<HistoryInfo?> = _historyInfo.asStateFlow()

    private val _appManagerInfo = MutableStateFlow<AppManagerInfo?>(null)
    val appManagerInfo: StateFlow<AppManagerInfo?> = _appManagerInfo.asStateFlow()

    private val _monitoringInfo = MutableStateFlow<MonitoringInfo?>(null)
    val monitoringInfo: StateFlow<MonitoringInfo?> = _monitoringInfo.asStateFlow()

    private val _benchmarkResult = MutableStateFlow<BenchmarkResult?>(null)
    val benchmarkResult: StateFlow<BenchmarkResult?> = _benchmarkResult.asStateFlow()

    private val _isRunningBenchmark = MutableStateFlow(false)
    val isRunningBenchmark: StateFlow<Boolean> = _isRunningBenchmark.asStateFlow()

    init {
        loadAllInfo()
    }

    fun loadAllInfo() {
        viewModelScope.launch {
            _deviceInfo.value = repository.getDeviceInfo()
            _hardwareInfo.value = repository.getHardwareInfo()
            _batteryInfo.value = repository.getBatteryInfo()
            _networkInfo.value = repository.getNetworkInfo()
            _displayInfo.value = repository.getDisplayInfo()
            _cameraInfo.value = repository.getCameraInfo()
            _sensorInfo.value = repository.getSensorInfo()
            _historyInfo.value = historyDatabase.getHistoryData()
        }
    }

    fun refreshBatteryInfo() {
        viewModelScope.launch {
            _batteryInfo.value = repository.getBatteryInfo()
        }
    }

    fun refreshNetworkInfo() {
        viewModelScope.launch {
            _networkInfo.value = repository.getNetworkInfo()
        }
    }

    fun refreshSensorInfo() {
        viewModelScope.launch {
            _sensorInfo.value = repository.getSensorInfo()
        }
    }

    fun loadAppManagerInfo() {
        viewModelScope.launch {
            _appManagerInfo.value = repository.getAppManagerInfo()
        }
    }

    fun refreshMonitoringInfo() {
        viewModelScope.launch {
            _monitoringInfo.value = repository.getMonitoringInfo()
        }
    }

    fun runBenchmark() {
        viewModelScope.launch {
            _isRunningBenchmark.value = true
            _benchmarkResult.value = repository.runBenchmark()
            _isRunningBenchmark.value = false
        }
    }

    fun saveHistoryData() {
        viewModelScope.launch {
            val battery = _batteryInfo.value
            val monitoring = repository.getMonitoringInfo()
            
            if (battery != null) {
                val hardware = _hardwareInfo.value
                val availableRamBytes = if (hardware != null) {
                    // Parse available RAM from string like "3.45 GB"
                    val ramString = hardware.availableRam
                    val value = ramString.substringBefore(" ").toFloatOrNull() ?: 0f
                    if (ramString.contains("GB")) {
                        (value * 1024 * 1024 * 1024).toLong()
                    } else {
                        (value * 1024 * 1024).toLong()
                    }
                } else {
                    0L
                }
                
                val historyData = HistoryData(
                    timestamp = System.currentTimeMillis(),
                    batteryLevel = battery.level,
                    availableRam = availableRamBytes,
                    cpuUsage = monitoring.cpuUsage
                )
                historyDatabase.saveHistoryData(historyData)
                _historyInfo.value = historyDatabase.getHistoryData()
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyDatabase.clearHistory()
            _historyInfo.value = historyDatabase.getHistoryData()
        }
    }
}
