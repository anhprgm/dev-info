package com.anhprgm.deviceinfo.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anhprgm.deviceinfo.R
import com.anhprgm.deviceinfo.data.models.ThermalStatus
import com.anhprgm.deviceinfo.ui.components.DetailColumn
import com.anhprgm.deviceinfo.ui.components.DetailRow
import com.anhprgm.deviceinfo.ui.components.DevInfoScaffold
import com.anhprgm.deviceinfo.ui.components.InfoCard
import com.anhprgm.deviceinfo.ui.components.LoadingState
import com.anhprgm.deviceinfo.ui.components.NoticeCard
import com.anhprgm.deviceinfo.ui.format.Formatters
import com.anhprgm.deviceinfo.ui.theme.Dimens
import com.anhprgm.deviceinfo.ui.viewmodel.SystemInfoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThermalScreen(
    onNavigateBack: () -> Unit,
    viewModel: SystemInfoViewModel = hiltViewModel()
) {
    val thermal by viewModel.thermal.collectAsStateWithLifecycle()

    DevInfoScaffold(
        title = stringResource(R.string.dashboard_temperature),
        onNavigateBack = onNavigateBack,
        actions = {
            IconButton(onClick = { viewModel.refreshThermal() }) {
                Icon(Icons.Default.Refresh, stringResource(R.string.action_refresh))
            }
        }
    ) { padding ->
        val info = thermal
        if (info == null) {
            LoadingState(modifier = Modifier.padding(padding))
            return@DevInfoScaffold
        }

        DetailColumn(padding) {
            InfoCard(title = stringResource(R.string.dashboard_temperature)) {
                DetailRow(
                    stringResource(R.string.thermal_battery_temp),
                    Formatters.celsius(info.batteryTemperatureCelsius)
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                // Naming the API level is more useful than a bare "N/A".
                DetailRow(
                    stringResource(R.string.thermal_status),
                    info.thermalStatus?.let { stringResource(labelOf(it)) }
                        ?: info.requiresApiForStatus?.let {
                            stringResource(R.string.requires_android_api, it)
                        }
                        ?: Formatters.NOT_AVAILABLE
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(
                    stringResource(R.string.thermal_headroom),
                    info.thermalHeadroom?.let { Formatters.decimal(it) }
                        ?: info.requiresApiForHeadroom?.let {
                            stringResource(R.string.requires_android_api, it)
                        }
                        ?: Formatters.NOT_AVAILABLE
                )
            }

            Spacer(Modifier.height(Dimens.cardSpacing))

            NoticeCard(
                title = stringResource(R.string.dashboard_temperature),
                message = stringResource(R.string.thermal_note),
                icon = Icons.Default.Info
            )
        }
    }
}

private fun labelOf(status: ThermalStatus): Int = when (status) {
    ThermalStatus.NONE -> R.string.thermal_status_none
    ThermalStatus.LIGHT -> R.string.thermal_status_light
    ThermalStatus.MODERATE -> R.string.thermal_status_moderate
    ThermalStatus.SEVERE -> R.string.thermal_status_severe
    ThermalStatus.CRITICAL -> R.string.thermal_status_critical
    ThermalStatus.EMERGENCY -> R.string.thermal_status_emergency
    ThermalStatus.SHUTDOWN -> R.string.thermal_status_shutdown
    ThermalStatus.UNKNOWN -> R.string.common_unknown
}
