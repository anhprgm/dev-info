package com.anhprgm.deviceinfo.ui.viewmodel

import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anhprgm.deviceinfo.data.benchmark.BenchmarkRunner
import com.anhprgm.deviceinfo.data.models.*
import com.anhprgm.deviceinfo.data.monitor.SystemMonitor
import com.anhprgm.deviceinfo.data.repository.HistoryRepository
import com.anhprgm.deviceinfo.data.source.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Single shared ViewModel, kept for Phase 1 so the screens change as little as
 * possible while the data layer is rebuilt. Phase 2 splits it into per-screen
 * ViewModels once the navigation graph is restructured.
 *
 * Every read now runs on an injected IO dispatcher inside the data sources, so
 * nothing here touches disk on the main thread.
 */
@HiltViewModel
class DeviceInfoViewModel @Inject constructor(
    private val deviceDataSource: DeviceDataSource,
    private val hardwareDataSource: HardwareDataSource,
    private val batteryDataSource: BatteryDataSource,
    private val networkDataSource: NetworkDataSource,
    private val displayDataSource: DisplayDataSource,
    private val cameraDataSource: CameraDataSource,
    private val sensorDataSource: SensorDataSource,
    private val packageDataSource: PackageDataSource,
    private val systemMonitor: SystemMonitor,
    private val benchmarkRunner: BenchmarkRunner,
    private val historyRepository: HistoryRepository
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

    private val _appManagerInfo = MutableStateFlow<AppManagerInfo?>(null)
    val appManagerInfo: StateFlow<AppManagerInfo?> = _appManagerInfo.asStateFlow()

    private val _monitoringInfo = MutableStateFlow<MonitoringInfo?>(null)
    val monitoringInfo: StateFlow<MonitoringInfo?> = _monitoringInfo.asStateFlow()

    private val _benchmarkResult = MutableStateFlow<BenchmarkResult?>(null)
    val benchmarkResult: StateFlow<BenchmarkResult?> = _benchmarkResult.asStateFlow()

    private val _isRunningBenchmark = MutableStateFlow(false)
    val isRunningBenchmark: StateFlow<Boolean> = _isRunningBenchmark.asStateFlow()

    private val _selectedApp = MutableStateFlow<AppInfo?>(null)
    val selectedApp: StateFlow<AppInfo?> = _selectedApp.asStateFlow()

    /** Backed by Room, so the history screen updates as samples land. */
    val historyInfo: StateFlow<HistoryInfo> = historyRepository.observeHistory()
        .map { HistoryInfo(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryInfo(emptyList())
        )

    init {
        loadAllInfo()
        viewModelScope.launch {
            historyRepository.migrateLegacyDataIfNeeded()
            _benchmarkResult.value = historyRepository.latestBenchmark()
        }
    }

    /**
     * Each read is launched independently so one slow source (camera
     * enumeration, package listing) does not gate the rest, unlike the previous
     * sequential block.
     */
    fun loadAllInfo() {
        viewModelScope.launch { _deviceInfo.value = deviceDataSource.getDeviceInfo() }
        viewModelScope.launch { _hardwareInfo.value = hardwareDataSource.getHardwareInfo() }
        viewModelScope.launch { _batteryInfo.value = batteryDataSource.getBatteryInfo() }
        viewModelScope.launch { _networkInfo.value = networkDataSource.getNetworkInfo() }
        viewModelScope.launch { _displayInfo.value = displayDataSource.getDisplayInfo() }
        viewModelScope.launch { _cameraInfo.value = cameraDataSource.getCameraInfo() }
        viewModelScope.launch { _sensorInfo.value = sensorDataSource.getSensorInfo() }
    }

    fun refreshBatteryInfo() = viewModelScope.launch {
        _batteryInfo.value = batteryDataSource.getBatteryInfo()
    }

    fun refreshNetworkInfo() = viewModelScope.launch {
        _networkInfo.value = networkDataSource.getNetworkInfo()
    }

    fun refreshSensorInfo() = viewModelScope.launch {
        _sensorInfo.value = sensorDataSource.getSensorInfo()
    }

    fun refreshHardwareInfo() = viewModelScope.launch {
        _hardwareInfo.value = hardwareDataSource.getHardwareInfo()
    }

    fun loadAppManagerInfo() = viewModelScope.launch {
        _appManagerInfo.value = packageDataSource.getAppManagerInfo()
    }

    /** Samples until the monitoring screen stops collecting, then records one point. */
    fun refreshMonitoringInfo() = viewModelScope.launch {
        val snapshot = systemMonitor.snapshot()
        _monitoringInfo.value = snapshot
        historyRepository.record(snapshot)
    }

    fun runBenchmark() = viewModelScope.launch {
        _isRunningBenchmark.value = true
        try {
            val result = benchmarkRunner.run()
            _benchmarkResult.value = result
            historyRepository.recordBenchmark(result)
        } finally {
            _isRunningBenchmark.value = false
        }
    }

    fun clearHistory() = viewModelScope.launch {
        historyRepository.clearHistory()
    }

    fun selectApp(appInfo: AppInfo) {
        _selectedApp.value = appInfo
    }

    suspend fun getAppIcon(packageName: String): Drawable? =
        packageDataSource.getAppIcon(packageName)
}
