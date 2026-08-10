package com.anhprgm.deviceinfo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anhprgm.deviceinfo.R
import com.anhprgm.deviceinfo.ui.components.CategoryCard
import com.anhprgm.deviceinfo.ui.components.CircularGauge
import com.anhprgm.deviceinfo.ui.components.DetailRow
import com.anhprgm.deviceinfo.ui.components.InfoCard
import com.anhprgm.deviceinfo.ui.format.Formatters
import com.anhprgm.deviceinfo.ui.theme.Dimens
import com.anhprgm.deviceinfo.ui.theme.LocalStatusPalette
import com.anhprgm.deviceinfo.ui.viewmodel.DeviceInfoViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitoringScreen(
    viewModel: DeviceInfoViewModel,
    contentPadding: PaddingValues,
    onNavigateToHistory: () -> Unit,
    onNavigateToBenchmark: () -> Unit
) {
    val monitoring by viewModel.monitoringInfo.collectAsStateWithLifecycle()
    val status = LocalStatusPalette.current

    // Sampling stops as soon as the screen leaves composition, so the loop no
    // longer runs while the user is elsewhere in the app.
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.refreshMonitoringInfo()
            delay(2_000)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_monitoring)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Dimens.screenPadding,
                end = Dimens.screenPadding,
                top = innerPadding.calculateTopPadding() + Dimens.spaceSm,
                bottom = contentPadding.calculateBottomPadding() + Dimens.spaceXl
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.cardSpacing)
        ) {
            item {
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
                        val cpu = monitoring?.appCpuPercent
                        CircularGauge(
                            progress = cpu?.let { it / 100f },
                            label = stringResource(R.string.monitoring_app_cpu),
                            valueText = Formatters.percent(cpu, decimals = 1),
                            color = when {
                                cpu == null -> MaterialTheme.colorScheme.outline
                                cpu > 80 -> status.critical
                                cpu > 50 -> status.warning
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                        val ram = monitoring?.ramUsagePercent
                        CircularGauge(
                            progress = ram?.let { it / 100f },
                            label = stringResource(R.string.monitoring_ram),
                            valueText = Formatters.percent(ram, decimals = 0),
                            color = when {
                                ram == null -> MaterialTheme.colorScheme.outline
                                ram > 85 -> status.critical
                                ram > 65 -> status.warning
                                else -> MaterialTheme.colorScheme.secondary
                            }
                        )
                    }
                }
            }

            // Naming the limitation is better than showing a confident 0%.
            item {
                Text(
                    text = stringResource(R.string.monitoring_cpu_explanation),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Dimens.spaceXs)
                )
            }

            monitoring?.let { info ->
                item {
                    InfoCard(title = stringResource(R.string.monitoring_ram)) {
                        DetailRow(
                            stringResource(R.string.common_used),
                            Formatters.bytes(info.ramUsedBytes)
                        )
                        HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                        DetailRow(
                            stringResource(R.string.common_available),
                            Formatters.bytes(info.ramAvailableBytes)
                        )
                        HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                        DetailRow(
                            stringResource(R.string.common_total),
                            Formatters.bytes(info.ramTotalBytes)
                        )
                    }
                }
                item {
                    InfoCard(title = stringResource(R.string.monitoring_storage)) {
                        DetailRow(
                            stringResource(R.string.common_used),
                            "${Formatters.bytes(info.storageUsedBytes)} " +
                                "(${Formatters.percent(info.storageUsagePercent, 0)})"
                        )
                        HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                        DetailRow(
                            stringResource(R.string.common_total),
                            Formatters.bytes(info.storageTotalBytes)
                        )
                    }
                }
            }

            item {
                CategoryCard(
                    icon = Icons.Default.History,
                    title = stringResource(R.string.screen_history),
                    subtitle = stringResource(R.string.history_battery_chart),
                    onClick = onNavigateToHistory
                )
            }
            item {
                CategoryCard(
                    icon = Icons.Default.Timer,
                    title = stringResource(R.string.screen_benchmark),
                    subtitle = stringResource(R.string.benchmark_section_cpu),
                    onClick = onNavigateToBenchmark
                )
            }
        }
    }
}
