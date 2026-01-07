package com.anhprgm.deviceinfo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anhprgm.deviceinfo.data.DeviceInfoRepository
import com.anhprgm.deviceinfo.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DeviceInfoViewModel(private val repository: DeviceInfoRepository) : ViewModel() {

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
}
