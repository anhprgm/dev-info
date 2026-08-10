package com.anhprgm.deviceinfo.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anhprgm.deviceinfo.R
import com.anhprgm.deviceinfo.ui.components.CopyableRow
import com.anhprgm.deviceinfo.ui.components.DetailColumn
import com.anhprgm.deviceinfo.ui.components.DetailRow
import com.anhprgm.deviceinfo.ui.components.DevInfoScaffold
import com.anhprgm.deviceinfo.ui.components.InfoCard
import com.anhprgm.deviceinfo.ui.components.LoadingState
import com.anhprgm.deviceinfo.ui.format.Formatters
import com.anhprgm.deviceinfo.ui.theme.Dimens
import com.anhprgm.deviceinfo.ui.theme.MonospaceValue
import com.anhprgm.deviceinfo.ui.viewmodel.DeviceInfoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(
    viewModel: DeviceInfoViewModel,
    onNavigateBack: () -> Unit
) {
    val device by viewModel.deviceInfo.collectAsStateWithLifecycle()
    val copied = stringResource(R.string.action_copied)

    DevInfoScaffold(
        title = stringResource(R.string.screen_device),
        onNavigateBack = onNavigateBack
    ) { padding ->
        val info = device
        if (info == null) {
            LoadingState(modifier = Modifier.padding(padding))
            return@DevInfoScaffold
        }

        DetailColumn(padding) {
            InfoCard(title = stringResource(R.string.device_section_basic)) {
                DetailRow(stringResource(R.string.device_name), info.deviceName)
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(stringResource(R.string.device_manufacturer), info.manufacturer)
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(stringResource(R.string.device_model), info.model)
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(stringResource(R.string.device_brand), info.brand)
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(stringResource(R.string.device_codename), info.device)
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(stringResource(R.string.device_board), info.board)
            }

            Spacer(Modifier.height(Dimens.cardSpacing))

            InfoCard(title = stringResource(R.string.device_section_system)) {
                DetailRow(stringResource(R.string.device_android_version), info.androidVersion)
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(stringResource(R.string.device_api_level), info.apiLevel.toString())
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(
                    stringResource(R.string.device_security_patch),
                    Formatters.text(info.securityPatch)
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(
                    stringResource(R.string.device_uptime),
                    Formatters.duration(info.uptimeMillis)
                )
            }

            Spacer(Modifier.height(Dimens.cardSpacing))

            InfoCard(title = stringResource(R.string.device_section_build)) {
                DetailRow(stringResource(R.string.device_build_id), info.buildId)
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(
                    stringResource(R.string.device_bootloader),
                    Formatters.text(info.bootloader)
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(
                    stringResource(R.string.device_abis),
                    info.supportedAbis.joinToString(", ").ifBlank { Formatters.NOT_AVAILABLE }
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                // Long, and the single most-pasted value in a bug report.
                CopyableRow(
                    label = stringResource(R.string.device_fingerprint),
                    value = info.buildFingerprint,
                    copiedMessage = copied,
                    valueStyle = MonospaceValue
                )
                Spacer(Modifier.height(Dimens.spaceSm))
                Text(
                    text = stringResource(R.string.action_long_press_to_copy),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
