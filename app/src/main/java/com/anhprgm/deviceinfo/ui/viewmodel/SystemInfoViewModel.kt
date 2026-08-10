package com.anhprgm.deviceinfo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anhprgm.deviceinfo.data.models.CodecInfo
import com.anhprgm.deviceinfo.data.models.GpuInfo
import com.anhprgm.deviceinfo.data.models.SecurityInfo
import com.anhprgm.deviceinfo.data.models.StorageInfo
import com.anhprgm.deviceinfo.data.models.TelephonyInfo
import com.anhprgm.deviceinfo.data.models.ThermalInfo
import com.anhprgm.deviceinfo.data.source.CodecDataSource
import com.anhprgm.deviceinfo.data.source.GpuDataSource
import com.anhprgm.deviceinfo.data.source.SecurityDataSource
import com.anhprgm.deviceinfo.data.source.StorageDataSource
import com.anhprgm.deviceinfo.data.source.TelephonyDataSource
import com.anhprgm.deviceinfo.data.source.ThermalDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The Phase 3 sources. Each loads independently so one slow query (GPU needs an
 * EGL context, codecs enumerate the whole media stack) does not gate the rest.
 */
@HiltViewModel
class SystemInfoViewModel @Inject constructor(
    private val gpuDataSource: GpuDataSource,
    private val storageDataSource: StorageDataSource,
    private val thermalDataSource: ThermalDataSource,
    private val codecDataSource: CodecDataSource,
    private val securityDataSource: SecurityDataSource,
    private val telephonyDataSource: TelephonyDataSource
) : ViewModel() {

    private val _gpu = MutableStateFlow<GpuInfo?>(null)
    val gpu: StateFlow<GpuInfo?> = _gpu.asStateFlow()

    private val _storage = MutableStateFlow<StorageInfo?>(null)
    val storage: StateFlow<StorageInfo?> = _storage.asStateFlow()

    private val _thermal = MutableStateFlow<ThermalInfo?>(null)
    val thermal: StateFlow<ThermalInfo?> = _thermal.asStateFlow()

    private val _codecs = MutableStateFlow<CodecInfo?>(null)
    val codecs: StateFlow<CodecInfo?> = _codecs.asStateFlow()

    private val _security = MutableStateFlow<SecurityInfo?>(null)
    val security: StateFlow<SecurityInfo?> = _security.asStateFlow()

    private val _telephony = MutableStateFlow<TelephonyInfo?>(null)
    val telephony: StateFlow<TelephonyInfo?> = _telephony.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch { _gpu.value = gpuDataSource.getGpuInfo() }
        viewModelScope.launch { _storage.value = storageDataSource.getStorageInfo() }
        viewModelScope.launch { _thermal.value = thermalDataSource.getThermalInfo() }
        viewModelScope.launch { _codecs.value = codecDataSource.getCodecInfo() }
        viewModelScope.launch { _security.value = securityDataSource.getSecurityInfo() }
        viewModelScope.launch { _telephony.value = telephonyDataSource.getTelephonyInfo() }
    }

    fun refreshThermal() = viewModelScope.launch {
        _thermal.value = thermalDataSource.getThermalInfo()
    }

    fun refreshStorage() = viewModelScope.launch {
        _storage.value = storageDataSource.getStorageInfo()
    }

    /** Called after the permission dialog resolves, to pick up the new grant. */
    fun refreshTelephony() = viewModelScope.launch {
        _telephony.value = telephonyDataSource.getTelephonyInfo()
    }
}
