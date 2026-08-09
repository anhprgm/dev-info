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
fun CameraScreen(
    viewModel: DeviceInfoViewModel,
    onNavigateBack: () -> Unit
) {
    val cameraInfo by viewModel.cameraInfo.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Camera Information",
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
        cameraInfo?.let { camera ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoCard(title = "Overview") {
                    DetailRow(label = "Total Cameras", value = camera.cameraCount.toString())
                }

                camera.cameras.forEachIndexed { index, cameraDetail ->
                    InfoCard(title = "Camera ${index + 1}") {
                        DetailRow(label = "Camera ID", value = cameraDetail.cameraId)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        DetailRow(label = "Facing", value = Labels.of(cameraDetail.facing))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        DetailRow(
                            label = "Resolution",
                            value = Formatters.megapixels(cameraDetail.megapixels)
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        DetailRow(
                            label = "Image Size",
                            value = Formatters.resolution(
                                cameraDetail.pixelWidth,
                                cameraDetail.pixelHeight
                            )
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        DetailRow(
                            label = "Focal Length",
                            value = cameraDetail.focalLengthsMm
                                .joinToString(", ") { Formatters.millimetres(it) }
                                .ifBlank { Formatters.NOT_AVAILABLE }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        DetailRow(
                            label = "Aperture",
                            value = cameraDetail.aperturesFStop
                                .joinToString(", ") { Formatters.aperture(it) }
                                .ifBlank { Formatters.NOT_AVAILABLE }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        DetailRow(
                            label = "Flash Available",
                            value = Labels.yesNo(cameraDetail.flashAvailable)
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        DetailRow(
                            label = "Optical Stabilization",
                            value = Labels.yesNo(cameraDetail.opticalStabilization)
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        DetailRow(
                            label = "Auto Exposure Lock",
                            value = Labels.yesNo(cameraDetail.autoExposureLock)
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        DetailRow(
                            label = "Auto White Balance Lock",
                            value = Labels.yesNo(cameraDetail.autoWhiteBalanceLock)
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        DetailRow(
                            label = "Sensor Orientation",
                            value = "${cameraDetail.sensorOrientation}°"
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        DetailRow(
                            label = "Output Formats",
                            value = cameraDetail.outputFormats.joinToString(", ")
                                .ifBlank { Formatters.NOT_AVAILABLE }
                        )
                    }
                }
            }
        } ?: LoadingState(modifier = Modifier.padding(paddingValues))
    }
}
