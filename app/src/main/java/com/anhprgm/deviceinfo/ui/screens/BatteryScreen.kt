package com.anhprgm.deviceinfo.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anhprgm.deviceinfo.R
import com.anhprgm.deviceinfo.ui.components.CircularGauge
import com.anhprgm.deviceinfo.ui.components.DetailColumn
import com.anhprgm.deviceinfo.ui.components.DetailRow
import com.anhprgm.deviceinfo.ui.components.DevInfoScaffold
import com.anhprgm.deviceinfo.ui.components.InfoCard
import com.anhprgm.deviceinfo.ui.components.LoadingState
import com.anhprgm.deviceinfo.ui.format.Formatters
import com.anhprgm.deviceinfo.ui.format.label
import com.anhprgm.deviceinfo.ui.theme.Dimens
import com.anhprgm.deviceinfo.ui.theme.LocalStatusPalette
import com.anhprgm.deviceinfo.ui.viewmodel.DeviceInfoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryScreen(
    viewModel: DeviceInfoViewModel,
    onNavigateBack: () -> Unit
) {
    val battery by viewModel.batteryInfo.collectAsStateWithLifecycle()
    val status = LocalStatusPalette.current

    DevInfoScaffold(
        title = stringResource(R.string.screen_battery),
        onNavigateBack = onNavigateBack,
        actions = {
            IconButton(onClick = { viewModel.refreshBatteryInfo() }) {
                Icon(Icons.Default.Refresh, stringResource(R.string.action_refresh))
            }
        }
    ) { padding ->
        val info = battery
        if (info == null) {
            LoadingState(modifier = Modifier.padding(padding))
            return@DevInfoScaffold
        }

        DetailColumn(padding) {
            InfoCard(title = stringResource(R.string.battery_level)) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val level = info.level
                    CircularGauge(
                        progress = level?.let { it / 100f },
                        label = info.status.label(),
                        valueText = Formatters.percentInt(level),
                        color = when {
                            level == null -> MaterialTheme.colorScheme.outline
                            level > 40 -> status.good
                            level > 15 -> status.warning
                            else -> status.critical
                        }
                    )
                }
            }

            Spacer(Modifier.height(Dimens.cardSpacing))

            InfoCard(title = stringResource(R.string.battery_section_status)) {
                DetailRow(
                    stringResource(R.string.battery_charging_status),
                    info.status.label()
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(stringResource(R.string.battery_health), info.health.label())
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(
                    stringResource(R.string.battery_technology),
                    Formatters.text(info.technology)
                )
            }

            Spacer(Modifier.height(Dimens.cardSpacing))

            InfoCard(title = stringResource(R.string.battery_section_technical)) {
                DetailRow(
                    stringResource(R.string.battery_temperature),
                    Formatters.celsius(info.temperatureCelsius)
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(
                    stringResource(R.string.battery_voltage),
                    Formatters.volts(info.voltageVolts)
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(
                    stringResource(R.string.battery_capacity),
                    Formatters.milliAmpHours(info.estimatedCapacityMah)
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(
                    stringResource(R.string.battery_cycles),
                    Formatters.count(
                        info.chargeCycles,
                        stringResource(R.string.battery_cycles_unit)
                    )
                )
                Spacer(Modifier.height(Dimens.spaceSm))
                // The number is an estimate; saying so avoids it being quoted
                // as the manufacturer's design capacity.
                Text(
                    text = stringResource(R.string.battery_capacity_note),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
