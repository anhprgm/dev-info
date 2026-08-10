package com.anhprgm.deviceinfo.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anhprgm.deviceinfo.data.prefs.AppSettings
import com.anhprgm.deviceinfo.data.prefs.SettingsRepository
import com.anhprgm.deviceinfo.data.prefs.ThemeMode
import com.anhprgm.deviceinfo.background.DeviceSamplingWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch {
        settingsRepository.setThemeMode(mode)
    }

    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setDynamicColor(enabled)
    }

    /** The preference and the scheduled work have to move together. */
    fun setBackgroundSampling(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setBackgroundSampling(enabled)
        if (enabled) {
            DeviceSamplingWorker.enqueue(context)
        } else {
            DeviceSamplingWorker.cancel(context)
        }
    }
}
