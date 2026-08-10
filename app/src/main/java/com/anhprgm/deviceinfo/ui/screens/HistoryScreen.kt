package com.anhprgm.deviceinfo.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anhprgm.deviceinfo.R
import com.anhprgm.deviceinfo.ui.components.DetailColumn
import com.anhprgm.deviceinfo.ui.components.DevInfoScaffold
import com.anhprgm.deviceinfo.ui.components.EmptyState
import com.anhprgm.deviceinfo.ui.components.InfoCard
import com.anhprgm.deviceinfo.ui.components.LineChart
import com.anhprgm.deviceinfo.ui.format.Formatters
import com.anhprgm.deviceinfo.ui.theme.Dimens
import com.anhprgm.deviceinfo.ui.viewmodel.DeviceInfoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: DeviceInfoViewModel,
    onNavigateBack: () -> Unit
) {
    val history by viewModel.historyInfo.collectAsStateWithLifecycle()

    DevInfoScaffold(
        title = stringResource(R.string.screen_history),
        onNavigateBack = onNavigateBack,
        actions = {
            if (history.samples.isNotEmpty()) {
                IconButton(onClick = { viewModel.clearHistory() }) {
                    Icon(Icons.Default.Delete, stringResource(R.string.action_clear))
                }
            }
        }
    ) { padding ->
        if (history.samples.isEmpty()) {
            EmptyState(
                icon = Icons.Default.QueryStats,
                title = stringResource(R.string.history_empty_title),
                description = stringResource(R.string.history_empty_message),
                modifier = Modifier.padding(padding)
            )
            return@DevInfoScaffold
        }

        DetailColumn(padding) {
            InfoCard(title = stringResource(R.string.history_battery_chart)) {
                LineChart(
                    data = history.samples.map { it.batteryLevel.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.chartHeight),
                    lineColor = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(Dimens.spaceSm))
                Text(
                    text = stringResource(R.string.history_point_count, history.samples.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(Dimens.cardSpacing))

            // Samples with no CPU reading are dropped rather than plotted as
            // zero — see MonitoringInfo.appCpuPercent for why they can be null.
            val cpuSamples = history.samples.mapNotNull { it.cpuPercent }
            InfoCard(title = stringResource(R.string.history_cpu_chart)) {
                if (cpuSamples.isEmpty()) {
                    Text(
                        text = stringResource(R.string.history_no_cpu),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LineChart(
                        data = cpuSamples,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.chartHeight),
                        lineColor = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(Modifier.height(Dimens.cardSpacing))

            InfoCard(title = stringResource(R.string.history_recent)) {
                val recent = history.samples.takeLast(5).reversed()
                val format = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
                recent.forEachIndexed { index, sample ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Dimens.spaceXs)
                    ) {
                        Text(
                            text = format.format(Date(sample.timestamp)),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(
                                R.string.history_sample_summary,
                                Formatters.percentInt(sample.batteryLevel),
                                Formatters.percent(sample.cpuPercent),
                                Formatters.bytes(sample.availableRamBytes)
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (index != recent.lastIndex) {
                        HorizontalDivider(Modifier.padding(vertical = Dimens.spaceXs))
                    }
                }
            }
        }
    }
}
