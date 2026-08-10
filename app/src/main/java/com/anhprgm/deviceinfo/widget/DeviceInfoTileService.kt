package com.anhprgm.deviceinfo.widget

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.anhprgm.deviceinfo.R
import com.anhprgm.deviceinfo.data.monitor.SystemMonitor
import com.anhprgm.deviceinfo.ui.format.Formatters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Quick Settings tile showing battery and RAM at a glance.
 *
 * Like the widget, this is framework-instantiated with no Activity, so the data
 * layer is reached through a Hilt entry point.
 */
class DeviceInfoTileService : TileService() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface TileEntryPoint {
        fun systemMonitor(): SystemMonitor
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onStartListening() {
        super.onStartListening()
        refresh()
    }

    override fun onClick() {
        super.onClick()
        refresh()
    }

    override fun onStopListening() {
        super.onStopListening()
        // Nothing should keep sampling once the shade closes.
        scope.coroutineContext.cancelChildren()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun refresh() {
        val monitor = EntryPointAccessors
            .fromApplication(applicationContext, TileEntryPoint::class.java)
            .systemMonitor()

        scope.launch {
            val snapshot = runCatching { monitor.snapshot() }.getOrNull()
            val label = getString(R.string.app_name)
            val subtitle = snapshot?.let {
                "${Formatters.percentInt(it.batteryLevel)} · " +
                    "${Formatters.percent(it.ramUsagePercent, 0)} RAM"
            } ?: getString(R.string.common_na)

            withContext(Dispatchers.Main) {
                qsTile?.apply {
                    state = Tile.STATE_ACTIVE
                    this.label = label
                    // subtitle is API 29+; below that the label carries it all.
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        this.subtitle = subtitle
                    }
                    contentDescription = "$label $subtitle"
                    updateTile()
                }
            }
        }
    }
}

private fun kotlin.coroutines.CoroutineContext.cancelChildren() {
    this[kotlinx.coroutines.Job]?.children?.forEach { it.cancel() }
}
