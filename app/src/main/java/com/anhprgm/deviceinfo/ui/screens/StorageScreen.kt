package com.anhprgm.deviceinfo.ui.screens

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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anhprgm.deviceinfo.R
import com.anhprgm.deviceinfo.ui.components.DetailColumn
import com.anhprgm.deviceinfo.ui.components.DetailRow
import com.anhprgm.deviceinfo.ui.components.DevInfoScaffold
import com.anhprgm.deviceinfo.ui.components.InfoCard
import com.anhprgm.deviceinfo.ui.components.LoadingState
import com.anhprgm.deviceinfo.ui.components.NoticeCard
import com.anhprgm.deviceinfo.ui.format.Formatters
import com.anhprgm.deviceinfo.ui.format.yesNo
import com.anhprgm.deviceinfo.ui.theme.Dimens
import com.anhprgm.deviceinfo.ui.viewmodel.SystemInfoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageScreen(
    onNavigateBack: () -> Unit,
    viewModel: SystemInfoViewModel = hiltViewModel()
) {
    val storage by viewModel.storage.collectAsStateWithLifecycle()

    DevInfoScaffold(
        title = stringResource(R.string.screen_storage),
        onNavigateBack = onNavigateBack,
        actions = {
            IconButton(onClick = { viewModel.refreshStorage() }) {
                Icon(Icons.Default.Refresh, stringResource(R.string.action_refresh))
            }
        }
    ) { padding ->
        val info = storage
        if (info == null) {
            LoadingState(modifier = Modifier.padding(padding))
            return@DevInfoScaffold
        }

        DetailColumn(padding) {
            InfoCard(title = stringResource(R.string.storage_section_internal)) {
                LinearProgressIndicator(
                    progress = { info.usagePercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )
                Spacer(Modifier.height(Dimens.spaceMd))
                DetailRow(
                    stringResource(R.string.common_used),
                    "${Formatters.bytes(info.usedBytes)} " +
                        "(${Formatters.percent(info.usagePercent, 0)})"
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(
                    stringResource(R.string.storage_free),
                    Formatters.bytes(info.availableBytes)
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(
                    stringResource(R.string.common_total),
                    Formatters.bytes(info.totalBytes)
                )
            }

            Spacer(Modifier.height(Dimens.cardSpacing))

            InfoCard(title = stringResource(R.string.storage_section_breakdown)) {
                DetailRow(
                    stringResource(R.string.storage_apps),
                    Formatters.bytes(info.appsBytes)
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(
                    stringResource(R.string.storage_system_data),
                    Formatters.bytes(info.systemAndDataBytes)
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(
                    stringResource(R.string.apps_total),
                    info.installedAppCount.toString()
                )
            }

            Spacer(Modifier.height(Dimens.cardSpacing))

            if (info.externalTotalBytes != null) {
                InfoCard(title = stringResource(R.string.storage_external)) {
                    DetailRow(
                        stringResource(R.string.common_total),
                        Formatters.bytes(info.externalTotalBytes)
                    )
                    HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                    DetailRow(
                        stringResource(R.string.common_available),
                        Formatters.bytes(info.externalAvailableBytes)
                    )
                    HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                    DetailRow(
                        stringResource(R.string.storage_emulated),
                        info.isEmulatedExternal.yesNo()
                    )
                }
                Spacer(Modifier.height(Dimens.cardSpacing))
            }

            // Explains why the breakdown is coarse rather than leaving the user
            // to assume the numbers are wrong.
            NoticeCard(
                title = stringResource(R.string.storage_section_breakdown),
                message = stringResource(R.string.storage_breakdown_note)
            )

            Spacer(Modifier.height(Dimens.spaceLg))
            Text(
                text = stringResource(R.string.action_long_press_to_copy),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
