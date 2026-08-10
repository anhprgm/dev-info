package com.anhprgm.deviceinfo.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import com.anhprgm.deviceinfo.ui.components.DetailColumn
import com.anhprgm.deviceinfo.ui.components.DetailRow
import com.anhprgm.deviceinfo.ui.components.DevInfoScaffold
import com.anhprgm.deviceinfo.ui.components.InfoCard
import com.anhprgm.deviceinfo.ui.components.LoadingState
import com.anhprgm.deviceinfo.ui.format.Formatters
import com.anhprgm.deviceinfo.ui.theme.Dimens
import com.anhprgm.deviceinfo.ui.viewmodel.DeviceInfoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HardwareScreen(
    viewModel: DeviceInfoViewModel,
    onNavigateBack: () -> Unit
) {
    val hardware by viewModel.hardwareInfo.collectAsStateWithLifecycle()

    DevInfoScaffold(
        title = stringResource(R.string.screen_hardware),
        onNavigateBack = onNavigateBack,
        actions = {
            IconButton(onClick = { viewModel.refreshHardwareInfo() }) {
                Icon(Icons.Default.Refresh, stringResource(R.string.action_refresh))
            }
        }
    ) { padding ->
        val info = hardware
        if (info == null) {
            LoadingState(modifier = Modifier.padding(padding))
            return@DevInfoScaffold
        }

        DetailColumn(padding) {
            InfoCard(title = stringResource(R.string.hardware_section_memory)) {
                DetailRow(
                    stringResource(R.string.hardware_total_ram),
                    Formatters.bytes(info.totalRamBytes)
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(
                    stringResource(R.string.hardware_available_ram),
                    Formatters.bytes(info.availableRamBytes)
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(
                    stringResource(R.string.hardware_used_ram),
                    "${Formatters.bytes(info.usedRamBytes)} " +
                        "(${Formatters.percent(info.ramUsagePercent, 0)})"
                )
            }

            Spacer(Modifier.height(Dimens.cardSpacing))

            InfoCard(title = stringResource(R.string.hardware_section_storage)) {
                DetailRow(
                    stringResource(R.string.hardware_total_storage),
                    Formatters.bytes(info.totalStorageBytes)
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(
                    stringResource(R.string.hardware_available_storage),
                    Formatters.bytes(info.availableStorageBytes)
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(
                    stringResource(R.string.hardware_used_storage),
                    "${Formatters.bytes(info.usedStorageBytes)} " +
                        "(${Formatters.percent(info.storageUsagePercent, 0)})"
                )
            }

            Spacer(Modifier.height(Dimens.cardSpacing))

            InfoCard(title = stringResource(R.string.hardware_section_cpu)) {
                DetailRow(stringResource(R.string.hardware_cpu_model), info.cpuModel)
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(stringResource(R.string.hardware_cpu_cores), info.cpuCores.toString())
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                // Null whenever SELinux blocks the sysfs read, which is common.
                DetailRow(
                    stringResource(R.string.hardware_cpu_max_freq),
                    Formatters.megahertzFromKhz(info.cpuMaxFrequencyKhz)
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(
                    stringResource(R.string.device_abis),
                    info.supportedAbis.joinToString(", ").ifBlank { Formatters.NOT_AVAILABLE }
                )
            }
        }
    }
}
