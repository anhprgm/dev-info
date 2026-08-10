package com.anhprgm.deviceinfo.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anhprgm.deviceinfo.R
import com.anhprgm.deviceinfo.data.export.DeviceReport
import com.anhprgm.deviceinfo.ui.components.DetailColumn
import com.anhprgm.deviceinfo.ui.components.DevInfoScaffold
import com.anhprgm.deviceinfo.ui.components.InfoCard
import com.anhprgm.deviceinfo.ui.components.LoadingState
import com.anhprgm.deviceinfo.ui.components.NoticeCard
import com.anhprgm.deviceinfo.ui.format.Formatters
import com.anhprgm.deviceinfo.ui.theme.Dimens
import com.anhprgm.deviceinfo.ui.viewmodel.CompareViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    onNavigateBack: () -> Unit,
    viewModel: CompareViewModel = hiltViewModel()
) {
    val current by viewModel.current.collectAsStateWithLifecycle()
    val other by viewModel.other.collectAsStateWithLifecycle()
    val loadError by viewModel.loadError.collectAsStateWithLifecycle()

    // OpenDocument rather than GetContent: it returns a persistable URI and
    // shows the system file picker, so no storage permission is involved.
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(viewModel::load) }

    DevInfoScaffold(
        title = stringResource(R.string.compare_title),
        onNavigateBack = onNavigateBack,
        actions = {
            if (other != null) {
                TextButton(onClick = viewModel::clear) {
                    Text(stringResource(R.string.action_clear))
                }
            }
        }
    ) { padding ->
        val mine = current
        if (mine == null) {
            LoadingState(modifier = Modifier.padding(padding))
            return@DevInfoScaffold
        }

        DetailColumn(padding) {
            val theirs = other
            if (theirs == null) {
                NoticeCard(
                    title = stringResource(R.string.compare_title),
                    message = stringResource(R.string.compare_instructions),
                    icon = Icons.Default.CompareArrows,
                    action = {
                        Button(onClick = { picker.launch(arrayOf("application/json")) }) {
                            Text(stringResource(R.string.compare_pick_file))
                        }
                    }
                )
                if (loadError) {
                    Spacer(Modifier.height(Dimens.cardSpacing))
                    Text(
                        text = stringResource(R.string.compare_load_failed),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                return@DetailColumn
            }

            InfoCard(title = stringResource(R.string.compare_title)) {
                CompareRow(
                    label = stringResource(R.string.device_name),
                    mine = mine.device.name,
                    theirs = theirs.device.name,
                    header = true
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                CompareRow(
                    stringResource(R.string.device_android_version),
                    "${mine.device.androidVersion} (${mine.device.apiLevel})",
                    "${theirs.device.androidVersion} (${theirs.device.apiLevel})"
                )
                CompareRow(
                    stringResource(R.string.hardware_cpu_model),
                    mine.hardware.cpuModel,
                    theirs.hardware.cpuModel
                )
                CompareRow(
                    stringResource(R.string.hardware_cpu_cores),
                    mine.hardware.cpuCores.toString(),
                    theirs.hardware.cpuCores.toString()
                )
                CompareRow(
                    stringResource(R.string.hardware_total_ram),
                    Formatters.bytes(mine.hardware.totalRamBytes),
                    Formatters.bytes(theirs.hardware.totalRamBytes)
                )
                CompareRow(
                    stringResource(R.string.hardware_total_storage),
                    Formatters.bytes(mine.hardware.totalStorageBytes),
                    Formatters.bytes(theirs.hardware.totalStorageBytes)
                )
                CompareRow(
                    stringResource(R.string.display_resolution),
                    Formatters.resolution(mine.display.widthPixels, mine.display.heightPixels),
                    Formatters.resolution(theirs.display.widthPixels, theirs.display.heightPixels)
                )
                CompareRow(
                    stringResource(R.string.display_refresh_rate),
                    Formatters.hertz(mine.display.refreshRateHz),
                    Formatters.hertz(theirs.display.refreshRateHz)
                )
                CompareRow(
                    stringResource(R.string.gpu_renderer),
                    Formatters.text(mine.gpu?.renderer),
                    Formatters.text(theirs.gpu?.renderer)
                )
            }

            if (mine.benchmark != null && theirs.benchmark != null) {
                Spacer(Modifier.height(Dimens.cardSpacing))
                InfoCard(title = stringResource(R.string.screen_benchmark)) {
                    CompareRow(
                        stringResource(R.string.benchmark_single_time),
                        Formatters.elapsed(mine.benchmark.singleCoreMillis),
                        Formatters.elapsed(theirs.benchmark.singleCoreMillis)
                    )
                    CompareRow(
                        stringResource(R.string.benchmark_multi_time),
                        Formatters.elapsed(mine.benchmark.multiCoreMillis),
                        Formatters.elapsed(theirs.benchmark.multiCoreMillis)
                    )
                    CompareRow(
                        stringResource(R.string.benchmark_memory_time),
                        Formatters.elapsed(mine.benchmark.memoryMillis),
                        Formatters.elapsed(theirs.benchmark.memoryMillis)
                    )
                }
            }
        }
    }
}

/** Three columns: label, this device, the imported one. */
@Composable
private fun CompareRow(
    label: String,
    mine: String,
    theirs: String,
    header: Boolean = false
) {
    Column(modifier = Modifier.padding(vertical = Dimens.spaceXs)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = mine,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (header) FontWeight.Bold else null,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.padding(horizontal = Dimens.spaceXs))
            Text(
                text = theirs,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (header) FontWeight.Bold else null,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
