package com.anhprgm.deviceinfo.ui.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anhprgm.deviceinfo.data.export.DeviceReport
import com.anhprgm.deviceinfo.data.export.ExportFormat
import com.anhprgm.deviceinfo.data.export.ReportBuilder
import com.anhprgm.deviceinfo.data.export.ReportExporter
import com.anhprgm.deviceinfo.data.export.ReportSerializer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val reportBuilder: ReportBuilder,
    private val exporter: ReportExporter,
    private val serializer: ReportSerializer
) : ViewModel() {

    private val _report = MutableStateFlow<DeviceReport?>(null)
    val report: StateFlow<DeviceReport?> = _report.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _shareIntent = MutableStateFlow<Intent?>(null)
    val shareIntent: StateFlow<Intent?> = _shareIntent.asStateFlow()

    private val _error = MutableStateFlow(false)
    val error: StateFlow<Boolean> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            exporter.pruneOldReports()
            _report.value = runCatching { reportBuilder.build() }.getOrNull()
        }
    }

    fun preview(): String = _report.value?.let(serializer::toPlainText).orEmpty()

    fun export(format: ExportFormat) {
        val current = _report.value ?: return
        if (_busy.value) return

        viewModelScope.launch {
            _busy.value = true
            _error.value = false
            try {
                val file = exporter.export(current, format)
                _shareIntent.value = exporter.shareIntent(file, format)
            } catch (_: Exception) {
                // Writing or rendering can fail on a full cache; surface it
                // rather than leaving the button looking inert.
                _error.value = true
            } finally {
                _busy.value = false
            }
        }
    }

    fun shareDeviceCard(png: ByteArray) {
        viewModelScope.launch {
            _busy.value = true
            try {
                val file = exporter.writeImage(png, "devinfo-card-${System.currentTimeMillis()}.png")
                _shareIntent.value = exporter.shareImageIntent(file)
            } catch (_: Exception) {
                _error.value = true
            } finally {
                _busy.value = false
            }
        }
    }

    /** Cleared once the chooser has been launched so it fires only once. */
    fun consumeShareIntent() {
        _shareIntent.value = null
    }
}
