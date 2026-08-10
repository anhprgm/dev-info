package com.anhprgm.deviceinfo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anhprgm.deviceinfo.data.source.AudioChannel
import com.anhprgm.deviceinfo.data.source.HardwareTestDataSource
import com.anhprgm.deviceinfo.data.source.MicrophoneDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HardwareTestViewModel @Inject constructor(
    private val hardwareTest: HardwareTestDataSource,
    private val microphone: MicrophoneDataSource
) : ViewModel() {

    private val _playingChannel = MutableStateFlow<AudioChannel?>(null)
    val playingChannel: StateFlow<AudioChannel?> = _playingChannel.asStateFlow()

    private val _micLevel = MutableStateFlow(0f)
    val micLevel: StateFlow<Float> = _micLevel.asStateFlow()

    private val _micRunning = MutableStateFlow(false)
    val micRunning: StateFlow<Boolean> = _micRunning.asStateFlow()

    private var toneJob: Job? = null
    private var micJob: Job? = null

    val hasVibrator: Boolean get() = hardwareTest.hasVibrator()
    val hasAmplitudeControl: Boolean get() = hardwareTest.hasAmplitudeControl()
    val hasMicrophone: Boolean get() = microphone.hasMicrophone()

    fun hasMicPermission(): Boolean = microphone.hasPermission()

    fun isMuted(): Boolean = hardwareTest.isMuted()

    fun playTone(channel: AudioChannel) {
        // Cancelling the previous job stops the old track before starting a new
        // one, so tapping L then R does not overlap two tones.
        toneJob?.cancel()
        toneJob = viewModelScope.launch {
            _playingChannel.value = channel
            try {
                hardwareTest.playTone(channel)
            } finally {
                _playingChannel.value = null
            }
        }
    }

    fun stopTone() {
        toneJob?.cancel()
        toneJob = null
        _playingChannel.value = null
    }

    fun vibrate(durationMillis: Long) = hardwareTest.vibrate(durationMillis)

    fun vibratePattern() = hardwareTest.vibratePattern(
        timings = longArrayOf(0, 200, 150, 200, 150, 400),
        amplitudes = intArrayOf(0, 80, 0, 160, 0, 255)
    )

    fun cancelVibration() = hardwareTest.cancelVibration()

    fun startMicMeter() {
        if (micJob != null) return
        _micRunning.value = true
        micJob = viewModelScope.launch {
            microphone.levels().collect { _micLevel.value = it }
        }
    }

    fun stopMicMeter() {
        micJob?.cancel()
        micJob = null
        _micRunning.value = false
        _micLevel.value = 0f
    }

    override fun onCleared() {
        super.onCleared()
        // Leaving the screen must release the mic and silence the speaker.
        stopTone()
        stopMicMeter()
        hardwareTest.cancelVibration()
    }
}
