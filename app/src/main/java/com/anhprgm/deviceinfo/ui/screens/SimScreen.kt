package com.anhprgm.deviceinfo.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anhprgm.deviceinfo.R
import com.anhprgm.deviceinfo.data.models.PhoneType
import com.anhprgm.deviceinfo.data.models.SimState
import com.anhprgm.deviceinfo.ui.components.DetailColumn
import com.anhprgm.deviceinfo.ui.components.DetailRow
import com.anhprgm.deviceinfo.ui.components.DevInfoScaffold
import com.anhprgm.deviceinfo.ui.components.EmptyState
import com.anhprgm.deviceinfo.ui.components.InfoCard
import com.anhprgm.deviceinfo.ui.components.LoadingState
import com.anhprgm.deviceinfo.ui.components.NoticeCard
import com.anhprgm.deviceinfo.ui.format.Formatters
import com.anhprgm.deviceinfo.ui.format.yesNo
import com.anhprgm.deviceinfo.ui.theme.Dimens
import com.anhprgm.deviceinfo.ui.viewmodel.SystemInfoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimScreen(
    onNavigateBack: () -> Unit,
    viewModel: SystemInfoViewModel = hiltViewModel()
) {
    val telephony by viewModel.telephony.collectAsStateWithLifecycle()
    var permissionDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionDenied = !granted
        viewModel.refreshTelephony()
    }

    DevInfoScaffold(
        title = stringResource(R.string.sim_section),
        onNavigateBack = onNavigateBack
    ) { padding ->
        val info = telephony
        when {
            info == null -> LoadingState(modifier = Modifier.padding(padding))

            !info.hasTelephony -> EmptyState(
                icon = Icons.Default.SignalCellularAlt,
                title = stringResource(R.string.sim_no_telephony),
                modifier = Modifier.padding(padding)
            )

            else -> DetailColumn(padding) {
                // Everything permission-free renders first, unconditionally.
                // The screen is never blocked on the permission prompt.
                InfoCard(title = stringResource(R.string.sim_section)) {
                    DetailRow(
                        stringResource(R.string.sim_state),
                        stringResource(simStateLabel(info.simState))
                    )
                    HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                    DetailRow(
                        stringResource(R.string.sim_operator_name),
                        Formatters.text(info.simOperatorName ?: info.networkOperatorName)
                    )
                    HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                    DetailRow(
                        stringResource(R.string.sim_operator_code),
                        Formatters.text(info.simOperator)
                    )
                    HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                    DetailRow(
                        stringResource(R.string.sim_country),
                        Formatters.text(info.simCountryIso)
                    )
                    HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                    DetailRow(
                        stringResource(R.string.sim_phone_type),
                        when (info.phoneType) {
                            PhoneType.GSM -> "GSM"
                            PhoneType.CDMA -> "CDMA"
                            PhoneType.SIP -> "SIP"
                            PhoneType.NONE -> stringResource(R.string.common_no)
                            PhoneType.UNKNOWN -> stringResource(R.string.common_unknown)
                        }
                    )
                    HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                    DetailRow(
                        stringResource(R.string.sim_modem_count),
                        info.activeModemCount?.toString() ?: Formatters.NOT_AVAILABLE
                    )
                    HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                    DetailRow(
                        stringResource(R.string.sim_roaming),
                        info.isRoaming?.yesNo() ?: Formatters.NOT_AVAILABLE
                    )
                }

                Spacer(Modifier.height(Dimens.cardSpacing))

                if (info.hasPhoneStatePermission) {
                    InfoCard(title = stringResource(R.string.sim_network_type)) {
                        DetailRow(
                            stringResource(R.string.sim_network_type),
                            Formatters.text(info.dataNetworkType)
                        )
                    }
                } else {
                    // Offer the grant inline; denial leaves the rest usable.
                    NoticeCard(
                        title = stringResource(R.string.sim_permission_title),
                        message = if (permissionDenied) {
                            stringResource(R.string.sim_permission_denied)
                        } else {
                            stringResource(R.string.sim_permission_message)
                        },
                        icon = Icons.Default.Info,
                        action = {
                            Button(onClick = {
                                permissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
                            }) {
                                Text(stringResource(R.string.action_grant))
                            }
                        }
                    )
                }

                Spacer(Modifier.height(Dimens.spaceLg))
                Text(
                    text = stringResource(R.string.sim_identifiers_note),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun simStateLabel(state: SimState): Int = when (state) {
    SimState.ABSENT -> R.string.sim_state_absent
    SimState.READY -> R.string.sim_state_ready
    SimState.NETWORK_LOCKED, SimState.PIN_REQUIRED, SimState.PUK_REQUIRED ->
        R.string.sim_state_locked
    SimState.NOT_READY -> R.string.sim_state_not_ready
    SimState.PERM_DISABLED, SimState.CARD_IO_ERROR, SimState.CARD_RESTRICTED ->
        R.string.sim_state_error
    SimState.UNKNOWN -> R.string.common_unknown
}
