package com.anhprgm.deviceinfo.ui.viewmodel

import android.hardware.Sensor
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anhprgm.deviceinfo.data.source.SensorLiveDataSource
import com.anhprgm.deviceinfo.data.source.SensorReading
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SensorLiveViewModel @Inject constructor(
    private val sensorLive: SensorLiveDataSource
) : ViewModel() {

    private val _reading = MutableStateFlow<SensorReading?>(null)
    val reading: StateFlow<SensorReading?> = _reading.asStateFlow()

    private val _heading = MutableStateFlow<Float?>(null)
    val heading: StateFlow<Float?> = _heading.asStateFlow()

    private var readingJob: Job? = null
    private var headingJob: Job? = null

    fun isAvailable(type: Int): Boolean = sensorLive.sensorOfType(type) != null

    /** Switching sensors cancels the previous subscription, which unregisters it. */
    fun observe(type: Int) {
        readingJob?.cancel()
        _reading.value = null
        readingJob = viewModelScope.launch {
            sensorLive.readings(type).collect { _reading.value = it }
        }

        // The compass overlay only makes sense for the magnetometer.
        headingJob?.cancel()
        _heading.value = null
        if (type == Sensor.TYPE_MAGNETIC_FIELD) {
            headingJob = viewModelScope.launch {
                sensorLive.heading().collect { _heading.value = it }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        readingJob?.cancel()
        headingJob?.cancel()
    }
}
