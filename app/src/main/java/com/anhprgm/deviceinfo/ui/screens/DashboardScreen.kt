package com.anhprgm.deviceinfo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anhprgm.deviceinfo.R
import com.anhprgm.deviceinfo.navigation.Battery
import com.anhprgm.deviceinfo.navigation.Camera
import com.anhprgm.deviceinfo.navigation.DeviceDetail
import com.anhprgm.deviceinfo.navigation.Display
import com.anhprgm.deviceinfo.navigation.Hardware
import com.anhprgm.deviceinfo.navigation.Network
import com.anhprgm.deviceinfo.navigation.Sensors
import com.anhprgm.deviceinfo.navigation.Settings as SettingsRoute
import com.anhprgm.deviceinfo.ui.components.CategoryCard
import com.anhprgm.deviceinfo.ui.components.CircularGauge
import com.anhprgm.deviceinfo.ui.components.HeroCard
import com.anhprgm.deviceinfo.ui.components.LoadingState
import com.anhprgm.deviceinfo.ui.components.SectionHeader
import com.anhprgm.deviceinfo.ui.components.Sparkline
import com.anhprgm.deviceinfo.ui.components.StatTile
import com.anhprgm.deviceinfo.ui.format.Formatters
import com.anhprgm.deviceinfo.ui.theme.Dimens
import com.anhprgm.deviceinfo.ui.theme.LocalStatusPalette
import com.anhprgm.deviceinfo.ui.viewmodel.DeviceInfoViewModel

private data class Category(
    val icon: ImageVector,
    val titleRes: Int,
    val descRes: Int,
    val route: Any
)

private val categories = listOf(
    Category(Icons.Default.PhoneAndroid, R.string.screen_device, R.string.screen_device_desc, DeviceDetail),
    Category(Icons.Default.DeveloperBoard, R.string.screen_hardware, R.string.screen_hardware_desc, Hardware),
    Category(Icons.Default.BatteryFull, R.string.screen_battery, R.string.screen_battery_desc, Battery),
    Category(Icons.Default.Tv, R.string.screen_display, R.string.screen_display_desc, Display),
    Category(Icons.Default.Wifi, R.string.screen_network, R.string.screen_network_desc, Network),
    Category(Icons.Default.CameraAlt, R.string.screen_camera, R.string.screen_camera_desc, Camera),
    Category(Icons.Default.Sensors, R.string.screen_sensors, R.string.screen_sensors_desc, Sensors)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DeviceInfoViewModel,
    contentPadding: PaddingValues,
    onNavigate: (Any) -> Unit
) {
    val device by viewModel.deviceInfo.collectAsState()
    val hardware by viewModel.hardwareInfo.collectAsState()
    val battery by viewModel.batteryInfo.collectAsState()
    val history by viewModel.historyInfo.collectAsState()
    val status = LocalStatusPalette.current

    androidx.compose.material3.Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard_title)) },
                actions = {
                    // Settings is an action here rather than a fourth tab.
                    IconButton(onClick = { onNavigate(SettingsRoute) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.screen_settings)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { innerPadding ->
        if (device == null) {
            LoadingState(modifier = Modifier.padding(innerPadding))
            return@Scaffold
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Dimens.screenPadding,
                end = Dimens.screenPadding,
                top = innerPadding.calculateTopPadding() + Dimens.spaceSm,
                bottom = contentPadding.calculateBottomPadding() + Dimens.spaceXl
            ),
            horizontalArrangement = Arrangement.spacedBy(Dimens.cardSpacing),
            verticalArrangement = Arrangement.spacedBy(Dimens.cardSpacing)
        ) {
            device?.let { d ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    HeroCard(
                        deviceName = d.deviceName,
                        // deviceName is already "manufacturer model", so showing
                        // model underneath just repeated it. Codename/board is
                        // the genuinely different identifier.
                        model = listOf(d.device, d.board)
                            .filter { it.isNotBlank() }
                            .distinct()
                            .joinToString(" • "),
                        androidVersion = d.androidVersion,
                        apiLevel = d.apiLevel,
                        uptime = Formatters.duration(d.uptimeMillis),
                        androidLabel = stringResource(R.string.dashboard_android),
                        uptimeLabel = stringResource(R.string.dashboard_uptime)
                    )
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.cardPadding),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val level = battery?.level
                        CircularGauge(
                            progress = level?.let { it / 100f },
                            label = stringResource(R.string.dashboard_battery),
                            valueText = Formatters.percentInt(level),
                            color = when {
                                level == null -> MaterialTheme.colorScheme.outline
                                level > 40 -> status.good
                                level > 15 -> status.warning
                                else -> status.critical
                            }
                        )
                        val ramPercent = hardware?.ramUsagePercent
                        CircularGauge(
                            progress = ramPercent?.let { it / 100f },
                            label = stringResource(R.string.dashboard_ram),
                            valueText = Formatters.percent(ramPercent, decimals = 0),
                            color = when {
                                ramPercent == null -> MaterialTheme.colorScheme.outline
                                ramPercent > 85 -> status.critical
                                ramPercent > 65 -> status.warning
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                }
            }

            item {
                // The headline number is free space, so the label has to say so
                // — "Storage: 8.02 GB" reads as capacity.
                StatTile(
                    icon = Icons.Default.Storage,
                    label = stringResource(R.string.dashboard_storage_free),
                    value = Formatters.bytes(hardware?.availableStorageBytes),
                    supporting = hardware?.let {
                        stringResource(
                            R.string.dashboard_storage_used_of,
                            Formatters.bytes(it.usedStorageBytes),
                            Formatters.bytes(it.totalStorageBytes)
                        )
                    },
                    onClick = { onNavigate(Hardware) }
                )
            }
            item {
                StatTile(
                    icon = Icons.Default.Thermostat,
                    label = stringResource(R.string.dashboard_temperature),
                    value = Formatters.celsius(battery?.temperatureCelsius),
                    supporting = stringResource(R.string.screen_battery),
                    onClick = { onNavigate(Battery) }
                )
            }
            item {
                StatTile(
                    icon = Icons.Default.DeveloperBoard,
                    label = stringResource(R.string.dashboard_cpu_cores),
                    value = hardware?.cpuCores?.toString() ?: Formatters.NOT_AVAILABLE,
                    supporting = Formatters.megahertzFromKhz(hardware?.cpuMaxFrequencyKhz),
                    onClick = { onNavigate(Hardware) }
                )
            }
            item {
                StatTile(
                    icon = Icons.Default.Memory,
                    label = stringResource(R.string.hardware_total_ram),
                    value = Formatters.bytes(hardware?.totalRamBytes),
                    // A bare second byte figure was ambiguous against the total.
                    supporting = hardware?.let {
                        stringResource(
                            R.string.dashboard_ram_free,
                            Formatters.bytes(it.availableRamBytes)
                        )
                    },
                    onClick = { onNavigate(Hardware) }
                )
            }

            // Only shown once there is something to plot; an empty chart frame
            // reads as a rendering bug.
            if (history.samples.size >= 2) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        androidx.compose.foundation.layout.Column(
                            modifier = Modifier.padding(Dimens.cardPadding)
                        ) {
                            Text(
                                text = stringResource(R.string.dashboard_battery_trend),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Sparkline(
                                data = history.samples.map { it.batteryLevel.toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(Dimens.sparklineHeight)
                                    .padding(top = Dimens.spaceSm)
                            )
                        }
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(title = stringResource(R.string.dashboard_categories))
            }

            items(categories, span = { GridItemSpan(maxLineSpan) }) { category ->
                CategoryCard(
                    icon = category.icon,
                    title = stringResource(category.titleRes),
                    subtitle = stringResource(category.descRes),
                    onClick = { onNavigate(category.route) }
                )
            }
        }
    }
}
