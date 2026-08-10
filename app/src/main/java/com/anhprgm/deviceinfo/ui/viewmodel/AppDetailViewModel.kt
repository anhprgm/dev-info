package com.anhprgm.deviceinfo.ui.viewmodel

import android.graphics.drawable.Drawable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anhprgm.deviceinfo.data.models.AppInfo
import com.anhprgm.deviceinfo.data.source.PackageDataSource
import com.anhprgm.deviceinfo.navigation.AppDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Resolves its own app from the package name carried in the typed route.
 *
 * This is what fixes the old process-death bug: the previous screen read the
 * selected app out of the shared ViewModel, so restoring the process (or
 * arriving via a deep link) left it with nothing to render.
 */
@HiltViewModel
class AppDetailViewModel @Inject constructor(
    private val packageDataSource: PackageDataSource,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val route: AppDetail = savedStateHandle.toRoute()

    private val _app = MutableStateFlow<AppInfo?>(null)
    val app: StateFlow<AppInfo?> = _app.asStateFlow()

    private val _icon = MutableStateFlow<Drawable?>(null)
    val icon: StateFlow<Drawable?> = _icon.asStateFlow()

    private val _notFound = MutableStateFlow(false)
    val notFound: StateFlow<Boolean> = _notFound.asStateFlow()

    init {
        viewModelScope.launch {
            val resolved = packageDataSource.findByPackageName(route.packageName)
            _app.value = resolved
            _notFound.value = resolved == null
        }
        viewModelScope.launch {
            _icon.value = packageDataSource.getAppIcon(route.packageName)
        }
    }
}
