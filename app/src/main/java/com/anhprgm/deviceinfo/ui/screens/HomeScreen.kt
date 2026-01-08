package com.anhprgm.deviceinfo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anhprgm.deviceinfo.ui.components.LoadingState
import com.anhprgm.deviceinfo.ui.viewmodel.DeviceInfoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: DeviceInfoViewModel,
    onNavigateToDevice: () -> Unit,
    onNavigateToHardware: () -> Unit,
    onNavigateToBattery: () -> Unit,
    onNavigateToNetwork: () -> Unit,
    onNavigateToDisplay: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToSensor: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAppManager: () -> Unit,
    onNavigateToMonitoring: () -> Unit,
    onNavigateToBenchmark: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val hardwareInfo by viewModel.hardwareInfo.collectAsState()
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val networkInfo by viewModel.networkInfo.collectAsState()
    val displayInfo by viewModel.displayInfo.collectAsState()
    val cameraInfo by viewModel.cameraInfo.collectAsState()
    val sensorInfo by viewModel.sensorInfo.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        if (deviceInfo == null) {
            LoadingState(modifier = Modifier.padding(paddingValues))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Welcome Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Welcome to DevInfo",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Your complete device information dashboard",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            QuickStatItem(
                                icon = Icons.Default.DeviceUnknown,
                                label = "Device",
                                value = deviceInfo?.manufacturer ?: "Unknown"
                            )
                            QuickStatItem(
                                icon = Icons.Default.BatteryChargingFull,
                                label = "Battery",
                                value = "${batteryInfo?.level ?: 0}%"
                            )
                            QuickStatItem(
                                icon = Icons.Default.Memory,
                                label = "RAM",
                                value = hardwareInfo?.totalRam ?: "N/A"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                // Basic Information Section
                SectionHeader(
                    icon = Icons.Default.Inventory,
                    title = "Device Information",
                    subtitle = "View detailed hardware and system specs"
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                deviceInfo?.let { device ->
                    QuickInfoCard(
                        icon = Icons.Default.DeviceUnknown,
                        title = "Device",
                        subtitle = "${device.manufacturer} ${device.model}",
                        onClick = onNavigateToDevice
                    )
                }

                hardwareInfo?.let { hardware ->
                    QuickInfoCard(
                        icon = Icons.Default.Memory,
                        title = "Hardware",
                        subtitle = "${hardware.totalRam} RAM • ${hardware.cpuCores} Cores",
                        onClick = onNavigateToHardware
                    )
                }

                batteryInfo?.let { battery ->
                    QuickInfoCard(
                        icon = Icons.Default.BatteryChargingFull,
                        title = "Battery",
                        subtitle = "${battery.level}% • ${battery.chargingStatus}",
                        onClick = onNavigateToBattery
                    )
                }

                networkInfo?.let { network ->
                    QuickInfoCard(
                        icon = Icons.Default.Wifi,
                        title = "Network",
                        subtitle = "${network.connectionType} • ${network.networkName}",
                        onClick = onNavigateToNetwork
                    )
                }

                displayInfo?.let { display ->
                    QuickInfoCard(
                        icon = Icons.Default.Screenshot,
                        title = "Display",
                        subtitle = "${display.resolution} • ${display.screenSize}",
                        onClick = onNavigateToDisplay
                    )
                }

                cameraInfo?.let { camera ->
                    QuickInfoCard(
                        icon = Icons.Default.CameraAlt,
                        title = "Camera",
                        subtitle = "${camera.cameraCount} Camera(s)",
                        onClick = onNavigateToCamera
                    )
                }

                sensorInfo?.let { sensor ->
                    QuickInfoCard(
                        icon = Icons.Default.Sensors,
                        title = "Sensors",
                        subtitle = "${sensor.sensorCount} Sensor(s)",
                        onClick = onNavigateToSensor
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // Advanced Features Section
                SectionHeader(
                    icon = Icons.Default.AutoAwesome,
                    title = "Advanced Features",
                    subtitle = "Monitor, analyze, and manage your device"
                )
                
                Spacer(modifier = Modifier.height(4.dp))

                QuickInfoCard(
                    icon = Icons.Default.History,
                    title = "History Tracking",
                    subtitle = "Battery & CPU usage over time",
                    onClick = onNavigateToHistory
                )

                QuickInfoCard(
                    icon = Icons.Default.Apps,
                    title = "App Manager",
                    subtitle = "View installed applications",
                    onClick = onNavigateToAppManager
                )

                QuickInfoCard(
                    icon = Icons.Default.Speed,
                    title = "Real-time Monitoring",
                    subtitle = "Live CPU & RAM monitoring",
                    onClick = onNavigateToMonitoring
                )

                QuickInfoCard(
                    icon = Icons.Default.Timer,
                    title = "Benchmark Tests",
                    subtitle = "Test device performance",
                    onClick = onNavigateToBenchmark
                )
            }
        }
    }
}

@Composable
fun QuickInfoCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SectionHeader(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun QuickStatItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
    }
}
