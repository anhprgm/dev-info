package com.anhprgm.deviceinfo.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.anhprgm.deviceinfo.MainActivity
import com.anhprgm.deviceinfo.R
import com.anhprgm.deviceinfo.data.monitor.SystemMonitor
import com.anhprgm.deviceinfo.ui.format.Formatters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Home-screen widget.
 *
 * Shows battery, RAM and storage — deliberately NOT CPU. System-wide CPU is
 * unobtainable on modern Android (SELinux blocks /proc/stat for
 * untrusted_app), so a "CPU" widget would sit on the home screen reading 0%
 * forever.
 */
class DeviceInfoWidget : GlanceAppWidget() {

    /**
     * The receiver is instantiated by the framework with no Activity in scope,
     * so the data layer is reached through a Hilt entry point rather than
     * injection. This is the concrete reason Hilt earns its place here.
     */
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun systemMonitor(): SystemMonitor
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val monitor = EntryPointAccessors
            .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
            .systemMonitor()

        val snapshot = runCatching { monitor.snapshot() }.getOrNull()

        provideContent {
            GlanceTheme {
                WidgetContent(
                    battery = snapshot?.batteryLevel,
                    ramPercent = snapshot?.ramUsagePercent,
                    ramFree = snapshot?.ramAvailableBytes,
                    storageFree = snapshot?.let { it.storageTotalBytes - it.storageUsedBytes }
                )
            }
        }
    }

    @Composable
    private fun WidgetContent(
        battery: Int?,
        ramPercent: Float?,
        ramFree: Long?,
        storageFree: Long?
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surfaceVariant)
                .padding(12.dp)
                // Glance 1.1's actionStartActivity takes an Intent, not a
                // reified activity type.
                .clickable(
                    actionStartActivity(
                        Intent(LocalContext.current, MainActivity::class.java)
                    )
                ),
            verticalAlignment = Alignment.Top
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = GlanceModifier.size(18.dp)
                )
                Spacer(modifier = GlanceModifier.size(6.dp))
                Text(
                    text = LocalWidgetStrings.title,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            WidgetRow(LocalWidgetStrings.battery, Formatters.percentInt(battery))
            WidgetRow(
                LocalWidgetStrings.ram,
                ramPercent?.let { Formatters.percent(it, 0) } ?: Formatters.NOT_AVAILABLE
            )
            WidgetRow(LocalWidgetStrings.ramFree, Formatters.bytes(ramFree))
            WidgetRow(LocalWidgetStrings.storageFree, Formatters.bytes(storageFree))
        }
    }

    @Composable
    private fun WidgetRow(label: String, value: String) {
        Row(modifier = GlanceModifier.fillMaxWidth().padding(vertical = 2.dp)) {
            Text(
                text = label,
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant)
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = value,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

/**
 * Glance composables cannot call stringResource, so the labels are resolved
 * once by the receiver and held here.
 */
object LocalWidgetStrings {
    var title: String = "DevInfo"
    var battery: String = "Battery"
    var ram: String = "RAM"
    var ramFree: String = "Free RAM"
    var storageFree: String = "Free storage"
}

class DeviceInfoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = DeviceInfoWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        loadStrings(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: android.appwidget.AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        loadStrings(context)
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    private fun loadStrings(context: Context) {
        LocalWidgetStrings.title = context.getString(R.string.app_name)
        LocalWidgetStrings.battery = context.getString(R.string.dashboard_battery)
        LocalWidgetStrings.ram = context.getString(R.string.dashboard_ram)
        LocalWidgetStrings.ramFree = context.getString(R.string.common_available)
        LocalWidgetStrings.storageFree = context.getString(R.string.dashboard_storage_free)
    }
}
