package com.anhprgm.deviceinfo.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anhprgm.deviceinfo.ui.components.InfoCard
import com.anhprgm.deviceinfo.ui.components.LoadingState
import com.anhprgm.deviceinfo.ui.format.Formatters
import com.anhprgm.deviceinfo.ui.format.Labels
import com.anhprgm.deviceinfo.ui.viewmodel.DeviceInfoViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: DeviceInfoViewModel,
    onNavigateBack: () -> Unit
) {
    val historyInfo by viewModel.historyInfo.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "History Tracking",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearHistory() }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Clear History",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
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
        run {
            val history = historyInfo
            if (history.samples.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No history yet. Samples are recorded while the " +
                            "Real-time Monitoring screen is open.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InfoCard(title = "Battery Level History") {
                        LineChart(
                            data = history.samples.map { it.batteryLevel.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            lineColor = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Last ${history.samples.size} data points",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Samples with no CPU reading are dropped rather than plotted
                    // as zero — see MonitoringInfo.appCpuPercent.
                    val cpuSamples = history.samples.mapNotNull { it.cpuPercent }
                    InfoCard(title = "App CPU Usage History") {
                        if (cpuSamples.isEmpty()) {
                            Text(
                                "No CPU measurements recorded.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            LineChart(
                                data = cpuSamples,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                lineColor = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "CPU used by DevInfo over time (%)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    InfoCard(title = "Recent Data Points") {
                        val recent = history.samples.takeLast(5).reversed()
                        val dateFormat = remember {
                            SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        }
                        recent.forEachIndexed { index, sample ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = dateFormat.format(Date(sample.timestamp)),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Battery: ${Formatters.percentInt(sample.batteryLevel)}" +
                                        " | CPU: ${Formatters.percent(sample.cpuPercent)}" +
                                        " | Free RAM: ${Formatters.bytes(sample.availableRamBytes)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (index != recent.lastIndex) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LineChart(
    data: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color.Blue
) {
    if (data.isEmpty()) return

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val maxValue = data.max()
        val minValue = data.min()
        val range = maxValue - minValue

        // Flat data makes range 0. Dividing by it yields NaN for every point,
        // which silently renders an empty chart — draw a centred line instead.
        fun yOf(value: Float): Float =
            if (range <= 0f) height / 2f
            else height - ((value - minValue) / range * height)

        if (data.size == 1) {
            drawCircle(
                color = lineColor,
                radius = 4f,
                center = Offset(width / 2, yOf(data[0]))
            )
            return@Canvas
        }

        val stepX = width / (data.size - 1)
        val path = Path()
        path.moveTo(0f, yOf(data[0]))
        data.forEachIndexed { index, value ->
            path.lineTo(index * stepX, yOf(value))
        }

        drawPath(path = path, color = lineColor, style = Stroke(width = 3f))

        data.forEachIndexed { index, value ->
            drawCircle(
                color = lineColor,
                radius = 4f,
                center = Offset(index * stepX, yOf(value))
            )
        }
    }
}
