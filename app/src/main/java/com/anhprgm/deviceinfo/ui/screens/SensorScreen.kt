package com.anhprgm.deviceinfo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SensorsOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anhprgm.deviceinfo.R
import com.anhprgm.deviceinfo.ui.components.DetailRow
import com.anhprgm.deviceinfo.ui.components.DevInfoScaffold
import com.anhprgm.deviceinfo.ui.components.EmptyState
import com.anhprgm.deviceinfo.ui.components.InfoCard
import com.anhprgm.deviceinfo.ui.components.LoadingState
import com.anhprgm.deviceinfo.ui.format.Formatters
import com.anhprgm.deviceinfo.ui.format.label
import com.anhprgm.deviceinfo.ui.format.yesNo
import com.anhprgm.deviceinfo.ui.theme.Dimens
import com.anhprgm.deviceinfo.ui.viewmodel.DeviceInfoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorScreen(
    viewModel: DeviceInfoViewModel,
    onNavigateBack: () -> Unit
) {
    val sensors by viewModel.sensorInfo.collectAsStateWithLifecycle()

    DevInfoScaffold(
        title = stringResource(R.string.screen_sensors),
        onNavigateBack = onNavigateBack,
        actions = {
            IconButton(onClick = { viewModel.refreshSensorInfo() }) {
                Icon(Icons.Default.Refresh, stringResource(R.string.action_refresh))
            }
        }
    ) { padding ->
        val info = sensors
        when {
            info == null -> LoadingState(modifier = Modifier.padding(padding))

            info.sensors.isEmpty() -> EmptyState(
                icon = Icons.Default.SensorsOff,
                title = stringResource(R.string.common_not_supported),
                modifier = Modifier.padding(padding)
            )

            // A LazyColumn rather than a scrolling Column: devices routinely
            // report 40+ sensors, and the old version composed them all.
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(Dimens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(Dimens.cardSpacing)
            ) {
                item {
                    InfoCard(title = stringResource(R.string.common_overview)) {
                        DetailRow(
                            stringResource(R.string.sensor_count),
                            info.sensorCount.toString()
                        )
                    }
                }
                itemsIndexed(info.sensors) { _, sensor ->
                    InfoCard(title = sensor.name) {
                        DetailRow(
                            stringResource(R.string.sensor_type),
                            sensor.kind.label(sensor.rawType)
                        )
                        HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                        DetailRow(
                            stringResource(R.string.sensor_vendor),
                            Formatters.text(sensor.vendor)
                        )
                        HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                        DetailRow(
                            stringResource(R.string.sensor_power),
                            Formatters.milliAmps(sensor.powerMilliAmps)
                        )
                        HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                        DetailRow(
                            stringResource(R.string.sensor_max_range),
                            Formatters.decimal(sensor.maximumRange)
                        )
                        HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                        DetailRow(
                            stringResource(R.string.sensor_resolution),
                            Formatters.decimal(sensor.resolution, decimals = 4)
                        )
                        HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                        DetailRow(
                            stringResource(R.string.sensor_wakeup),
                            sensor.isWakeUpSensor.yesNo()
                        )
                    }
                }
            }
        }
    }
}
