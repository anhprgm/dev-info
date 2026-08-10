package com.anhprgm.deviceinfo.ui.screens.test

import android.hardware.Sensor
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anhprgm.deviceinfo.R
import com.anhprgm.deviceinfo.ui.components.DetailColumn
import com.anhprgm.deviceinfo.ui.components.DetailRow
import com.anhprgm.deviceinfo.ui.components.DevInfoScaffold
import com.anhprgm.deviceinfo.ui.components.InfoCard
import com.anhprgm.deviceinfo.ui.components.LineChart
import com.anhprgm.deviceinfo.ui.components.NoticeCard
import com.anhprgm.deviceinfo.ui.format.Formatters
import com.anhprgm.deviceinfo.ui.theme.Dimens
import com.anhprgm.deviceinfo.ui.viewmodel.SensorLiveViewModel

private data class SensorChoice(val type: Int, val labelRes: Int)

private val choices = listOf(
    SensorChoice(Sensor.TYPE_ACCELEROMETER, R.string.sensor_accelerometer),
    SensorChoice(Sensor.TYPE_GYROSCOPE, R.string.sensor_gyroscope),
    SensorChoice(Sensor.TYPE_MAGNETIC_FIELD, R.string.sensor_magnetic_field),
    SensorChoice(Sensor.TYPE_LIGHT, R.string.sensor_light),
    SensorChoice(Sensor.TYPE_PROXIMITY, R.string.sensor_proximity),
    SensorChoice(Sensor.TYPE_PRESSURE, R.string.sensor_pressure)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorLiveScreen(
    initialSensorType: Int,
    onNavigateBack: () -> Unit,
    viewModel: SensorLiveViewModel = hiltViewModel()
) {
    var selectedType by remember { mutableIntStateOf(initialSensorType) }
    val reading by viewModel.reading.collectAsStateWithLifecycle()
    val heading by viewModel.heading.collectAsStateWithLifecycle()
    val available = remember(selectedType) { viewModel.isAvailable(selectedType) }

    // Restarts the sensor subscription whenever the choice changes; the old
    // listener is unregistered by the flow's awaitClose.
    androidx.compose.runtime.LaunchedEffect(selectedType) {
        viewModel.observe(selectedType)
    }

    // Ring buffer for the magnitude plot.
    val history = remember(selectedType) { mutableStateListOf<Float>() }
    androidx.compose.runtime.LaunchedEffect(reading) {
        val values = reading?.values ?: return@LaunchedEffect
        val magnitude = when {
            values.size >= 3 ->
                kotlin.math.sqrt(
                    values[0] * values[0] + values[1] * values[1] + values[2] * values[2]
                )
            values.isNotEmpty() -> values[0]
            else -> return@LaunchedEffect
        }
        history.add(magnitude)
        if (history.size > MAX_POINTS) history.removeAt(0)
    }

    DevInfoScaffold(
        title = stringResource(R.string.test_sensors),
        onNavigateBack = onNavigateBack
    ) { padding ->
        DetailColumn(padding) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
            ) {
                choices.forEach { choice ->
                    FilterChip(
                        selected = selectedType == choice.type,
                        onClick = { selectedType = choice.type },
                        label = { Text(stringResource(choice.labelRes)) }
                    )
                }
            }

            Spacer(Modifier.height(Dimens.cardSpacing))

            if (!available) {
                NoticeCard(
                    title = stringResource(R.string.test_sensors),
                    message = stringResource(R.string.test_sensor_absent)
                )
                return@DetailColumn
            }

            val values = reading?.values
            InfoCard(title = stringResource(R.string.test_sensor_value)) {
                if (values == null) {
                    Text(
                        text = stringResource(R.string.common_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    // Three-axis sensors get X/Y/Z; single-value ones (light,
                    // proximity, pressure) get one row.
                    if (values.size >= 3) {
                        DetailRow(
                            stringResource(R.string.test_sensor_x),
                            Formatters.decimal(values[0], 4)
                        )
                        HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                        DetailRow(
                            stringResource(R.string.test_sensor_y),
                            Formatters.decimal(values[1], 4)
                        )
                        HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                        DetailRow(
                            stringResource(R.string.test_sensor_z),
                            Formatters.decimal(values[2], 4)
                        )
                    } else {
                        DetailRow(
                            stringResource(R.string.test_sensor_value),
                            Formatters.decimal(values.firstOrNull(), 4)
                        )
                    }
                }
            }

            if (heading != null) {
                Spacer(Modifier.height(Dimens.cardSpacing))
                InfoCard(title = stringResource(R.string.test_sensor_heading)) {
                    DetailRow(
                        stringResource(R.string.test_sensor_heading),
                        "${Formatters.decimal(heading, 1)}°"
                    )
                }
            }

            if (history.size >= 2) {
                Spacer(Modifier.height(Dimens.cardSpacing))
                InfoCard(title = stringResource(R.string.test_sensors)) {
                    LineChart(
                        data = history.toList(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.chartHeight),
                        showPoints = false
                    )
                }
            }
        }
    }
}

private const val MAX_POINTS = 120
