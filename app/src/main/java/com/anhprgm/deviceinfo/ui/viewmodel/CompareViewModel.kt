package com.anhprgm.deviceinfo.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anhprgm.deviceinfo.data.export.DeviceReport
import com.anhprgm.deviceinfo.data.export.ReportBuilder
import com.anhprgm.deviceinfo.data.export.ReportSerializer
import com.anhprgm.deviceinfo.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Compares this device against a report exported from another one.
 *
 * Scoped deliberately: a true cross-device comparison would need a server-side
 * database of device specs, which this app does not have. Comparing against a
 * JSON file someone else exported is the useful half that works offline, and
 * the export format is already the import format.
 */
@HiltViewModel
class CompareViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reportBuilder: ReportBuilder,
    private val serializer: ReportSerializer,
    @IoDispatcher private val io: CoroutineDispatcher
) : ViewModel() {

    private val _current = MutableStateFlow<DeviceReport?>(null)
    val current: StateFlow<DeviceReport?> = _current.asStateFlow()

    private val _other = MutableStateFlow<DeviceReport?>(null)
    val other: StateFlow<DeviceReport?> = _other.asStateFlow()

    private val _loadError = MutableStateFlow(false)
    val loadError: StateFlow<Boolean> = _loadError.asStateFlow()

    init {
        viewModelScope.launch {
            _current.value = runCatching { reportBuilder.build() }.getOrNull()
        }
    }

    fun load(uri: Uri) {
        viewModelScope.launch {
            _loadError.value = false
            val report = withContext(io) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        serializer.fromJson(stream.readBytes().decodeToString())
                    }
                }.getOrNull()
            }
            if (report == null) {
                // A wrong file or an incompatible schema — say so rather than
                // silently showing an empty comparison.
                _loadError.value = true
            } else {
                _other.value = report
            }
        }
    }

    fun clear() {
        _other.value = null
        _loadError.value = false
    }
}
