package com.anhprgm.deviceinfo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anhprgm.deviceinfo.ui.components.DetailRow
import com.anhprgm.deviceinfo.ui.components.InfoCard
import com.anhprgm.deviceinfo.ui.components.LoadingState
import com.anhprgm.deviceinfo.ui.format.Formatters
import com.anhprgm.deviceinfo.ui.format.Labels
import com.anhprgm.deviceinfo.ui.viewmodel.DeviceInfoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HardwareScreen(
    viewModel: DeviceInfoViewModel,
    onNavigateBack: () -> Unit
) {
    val hardwareInfo by viewModel.hardwareInfo.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Hardware Information",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        hardwareInfo?.let { hardware ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoCard(title = "Memory (RAM)") {
                    DetailRow(label = "Total RAM", value = Formatters.bytes(hardware.totalRamBytes))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    DetailRow(
                        label = "Available RAM",
                        value = Formatters.bytes(hardware.availableRamBytes)
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    DetailRow(
                        label = "Used RAM",
                        value = "${Formatters.bytes(hardware.usedRamBytes)} " +
                            "(${Formatters.percent(hardware.ramUsagePercent)})"
                    )
                }

                InfoCard(title = "Storage") {
                    DetailRow(
                        label = "Total Storage",
                        value = Formatters.bytes(hardware.totalStorageBytes)
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    DetailRow(
                        label = "Available Storage",
                        value = Formatters.bytes(hardware.availableStorageBytes)
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    DetailRow(
                        label = "Used Storage",
                        value = "${Formatters.bytes(hardware.usedStorageBytes)} " +
                            "(${Formatters.percent(hardware.storageUsagePercent)})"
                    )
                }

                InfoCard(title = "Processor (CPU)") {
                    DetailRow(label = "CPU Information", value = hardware.cpuModel)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    DetailRow(label = "CPU Cores", value = hardware.cpuCores.toString())
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    DetailRow(
                        label = "Max Frequency",
                        value = Formatters.megahertzFromKhz(hardware.cpuMaxFrequencyKhz)
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    DetailRow(
                        label = "Supported ABIs",
                        value = hardware.supportedAbis.joinToString(", ")
                            .ifBlank { Formatters.NOT_AVAILABLE }
                    )
                }
            }
        } ?: LoadingState(modifier = Modifier.padding(paddingValues))
    }
}
