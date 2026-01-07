package com.anhprgm.deviceinfo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anhprgm.deviceinfo.ui.components.InfoCard
import com.anhprgm.deviceinfo.ui.components.InfoRow
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
    onNavigateToCamera: () -> Unit
) {
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val hardwareInfo by viewModel.hardwareInfo.collectAsState()
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val networkInfo by viewModel.networkInfo.collectAsState()
    val displayInfo by viewModel.displayInfo.collectAsState()
    val cameraInfo by viewModel.cameraInfo.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            "DevInfo",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Device Information",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
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
                // Quick Summary Cards
                deviceInfo?.let { device ->
                    QuickInfoCard(
                        icon = Icons.Default.Phone,
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
