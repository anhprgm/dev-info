package com.anhprgm.deviceinfo.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
        historyInfo?.let { history ->
            if (history.history.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No history data yet. Data is collected automatically.",
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
                            data = history.history.map { it.batteryLevel.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            lineColor = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Last ${history.history.size} data points",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    InfoCard(title = "CPU Usage History") {
                        LineChart(
                            data = history.history.map { it.cpuUsage },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            lineColor = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "CPU usage over time (%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    InfoCard(title = "Recent Data Points") {
                        history.history.takeLast(5).reversed().forEach { data ->
                            val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                            val time = dateFormat.format(Date(data.timestamp))
                            
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Text(
                                    text = time,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Battery: ${data.batteryLevel}% | CPU: ${"%.1f".format(data.cpuUsage)}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (data != history.history.takeLast(5).reversed().last()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        } ?: LoadingState(modifier = Modifier.padding(paddingValues))
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
        val maxValue = data.maxOrNull() ?: 100f
        val minValue = data.minOrNull() ?: 0f
        val range = maxValue - minValue
        
        if (data.size == 1) {
            // Draw single point
            val y = height - ((data[0] - minValue) / range * height)
            drawCircle(
                color = lineColor,
                radius = 4f,
                center = Offset(width / 2, y)
            )
            return@Canvas
        }

        val stepX = width / (data.size - 1)
        val path = Path()
        
        // Start path
        val firstY = height - ((data[0] - minValue) / range * height)
        path.moveTo(0f, firstY)
        
        // Draw line through all points
        data.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - ((value - minValue) / range * height)
            path.lineTo(x, y)
        }
        
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3f)
        )
        
        // Draw points
        data.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - ((value - minValue) / range * height)
            drawCircle(
                color = lineColor,
                radius = 4f,
                center = Offset(x, y)
            )
        }
    }
}
