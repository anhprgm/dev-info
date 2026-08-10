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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anhprgm.deviceinfo.R
import com.anhprgm.deviceinfo.data.models.ConnectionType
import com.anhprgm.deviceinfo.ui.components.CopyableRow
import com.anhprgm.deviceinfo.ui.components.DetailColumn
import com.anhprgm.deviceinfo.ui.components.DetailRow
import com.anhprgm.deviceinfo.ui.components.DevInfoScaffold
import com.anhprgm.deviceinfo.ui.components.InfoCard
import com.anhprgm.deviceinfo.ui.components.LoadingState
import com.anhprgm.deviceinfo.ui.components.NoticeCard
import com.anhprgm.deviceinfo.ui.format.Formatters
import com.anhprgm.deviceinfo.ui.format.label
import com.anhprgm.deviceinfo.ui.format.yesNo
import com.anhprgm.deviceinfo.ui.theme.Dimens
import com.anhprgm.deviceinfo.ui.theme.MonospaceValue
import com.anhprgm.deviceinfo.ui.viewmodel.DeviceInfoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkScreen(
    viewModel: DeviceInfoViewModel,
    onNavigateBack: () -> Unit
) {
    val network by viewModel.networkInfo.collectAsStateWithLifecycle()
    val copied = stringResource(R.string.action_copied)

    DevInfoScaffold(
        title = stringResource(R.string.screen_network),
        onNavigateBack = onNavigateBack,
        actions = {
            IconButton(onClick = { viewModel.refreshNetworkInfo() }) {
                Icon(Icons.Default.Refresh, stringResource(R.string.action_refresh))
            }
        }
    ) { padding ->
        val info = network
        if (info == null) {
            LoadingState(modifier = Modifier.padding(padding))
            return@DevInfoScaffold
        }

        DetailColumn(padding) {
            InfoCard(title = stringResource(R.string.network_section_connection)) {
                DetailRow(
                    stringResource(R.string.network_type),
                    info.connectionType.label()
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(stringResource(R.string.network_name), Formatters.text(info.ssid))
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(stringResource(R.string.network_metered), info.isMetered.yesNo())
            }

            Spacer(Modifier.height(Dimens.cardSpacing))

            InfoCard(title = stringResource(R.string.network_section_details)) {
                CopyableRow(
                    label = stringResource(R.string.network_ipv4),
                    value = Formatters.text(info.ipv4Address),
                    copiedMessage = copied,
                    valueStyle = MonospaceValue
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                CopyableRow(
                    label = stringResource(R.string.network_ipv6),
                    value = Formatters.text(info.ipv6Address),
                    copiedMessage = copied,
                    valueStyle = MonospaceValue
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(
                    stringResource(R.string.network_signal),
                    info.signalLevel?.let { level ->
                        stringResource(
                            R.string.network_signal_format,
                            level,
                            info.maxSignalLevel,
                            Formatters.dbm(info.rssiDbm)
                        )
                    } ?: Formatters.NOT_AVAILABLE
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(
                    stringResource(R.string.network_link_speed),
                    info.linkSpeedMbps
                        ?.let { stringResource(R.string.network_link_speed_format, it) }
                        ?: Formatters.NOT_AVAILABLE
                )
            }

            // The platform withholds the SSID on Android 10+ without location
            // permission. Explaining the gap beats printing a bare "N/A".
            if (info.connectionType == ConnectionType.WIFI && info.ssid == null) {
                Spacer(Modifier.height(Dimens.cardSpacing))
                NoticeCard(
                    title = stringResource(R.string.network_ssid_hidden_title),
                    message = stringResource(R.string.network_ssid_hidden_message),
                    icon = Icons.Default.Info
                )
            }
        }
    }
}
