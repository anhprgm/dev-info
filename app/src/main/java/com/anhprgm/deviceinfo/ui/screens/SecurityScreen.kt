package com.anhprgm.deviceinfo.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anhprgm.deviceinfo.R
import com.anhprgm.deviceinfo.data.models.EncryptionStatus
import com.anhprgm.deviceinfo.ui.components.DetailColumn
import com.anhprgm.deviceinfo.ui.components.DetailRow
import com.anhprgm.deviceinfo.ui.components.DevInfoScaffold
import com.anhprgm.deviceinfo.ui.components.InfoCard
import com.anhprgm.deviceinfo.ui.components.LoadingState
import com.anhprgm.deviceinfo.ui.components.NoticeCard
import com.anhprgm.deviceinfo.ui.format.Formatters
import com.anhprgm.deviceinfo.ui.format.yesNo
import com.anhprgm.deviceinfo.ui.theme.Dimens
import com.anhprgm.deviceinfo.ui.theme.MonospaceValue
import com.anhprgm.deviceinfo.ui.viewmodel.SystemInfoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    onNavigateBack: () -> Unit,
    viewModel: SystemInfoViewModel = hiltViewModel()
) {
    val security by viewModel.security.collectAsStateWithLifecycle()

    DevInfoScaffold(
        title = stringResource(R.string.security_title),
        onNavigateBack = onNavigateBack
    ) { padding ->
        val info = security
        if (info == null) {
            LoadingState(modifier = Modifier.padding(padding))
            return@DevInfoScaffold
        }

        DetailColumn(padding) {
            // The caveat comes first: this screen is easy to misread as a
            // verdict on whether the device is compromised.
            NoticeCard(
                title = stringResource(R.string.security_title),
                message = stringResource(R.string.security_heuristic_note),
                icon = Icons.Default.Info
            )

            Spacer(Modifier.height(Dimens.cardSpacing))

            InfoCard(title = stringResource(R.string.security_encryption)) {
                DetailRow(
                    stringResource(R.string.security_encryption),
                    stringResource(
                        when (info.encryptionStatus) {
                            EncryptionStatus.UNSUPPORTED -> R.string.encryption_unsupported
                            EncryptionStatus.INACTIVE -> R.string.encryption_inactive
                            EncryptionStatus.ACTIVE -> R.string.encryption_active
                            EncryptionStatus.ACTIVE_DEFAULT_KEY ->
                                R.string.encryption_active_default_key
                            EncryptionStatus.ACTIVE_PER_USER ->
                                R.string.encryption_active_per_user
                            EncryptionStatus.UNKNOWN -> R.string.common_unknown
                        }
                    )
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(
                    stringResource(R.string.device_security_patch),
                    Formatters.text(info.securityPatch)
                )
            }

            Spacer(Modifier.height(Dimens.cardSpacing))

            InfoCard(title = stringResource(R.string.security_bootloader)) {
                DetailRow(
                    stringResource(R.string.security_bootloader),
                    Formatters.text(info.bootloader),
                    valueStyle = MonospaceValue
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(
                    stringResource(R.string.security_verified_boot),
                    Formatters.text(info.verifiedBootState)
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(
                    stringResource(R.string.security_bootloader_locked),
                    info.bootloaderLocked?.yesNo() ?: Formatters.NOT_AVAILABLE
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(
                    stringResource(R.string.security_selinux),
                    Formatters.text(info.selinuxMode)
                )
            }

            Spacer(Modifier.height(Dimens.cardSpacing))

            InfoCard(title = stringResource(R.string.security_root_signals)) {
                DetailRow(
                    stringResource(R.string.security_test_keys),
                    info.hasTestKeys.yesNo()
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(
                    stringResource(R.string.security_debuggable),
                    info.isDebuggableBuild.yesNo()
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                // Listing the actual signals lets the user judge, instead of
                // the app asserting "rooted" or "clean".
                DetailRow(
                    stringResource(R.string.security_root_signals),
                    info.rootIndicators.joinToString("\n")
                        .ifBlank { stringResource(R.string.security_root_none) },
                    valueStyle = if (info.rootIndicators.isNotEmpty()) MonospaceValue else null
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
