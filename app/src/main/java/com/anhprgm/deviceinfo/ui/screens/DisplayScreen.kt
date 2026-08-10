package com.anhprgm.deviceinfo.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import com.anhprgm.deviceinfo.ui.format.Labels
import com.anhprgm.deviceinfo.ui.format.supported
import com.anhprgm.deviceinfo.ui.theme.Dimens
import com.anhprgm.deviceinfo.ui.viewmodel.DeviceInfoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayScreen(
    viewModel: DeviceInfoViewModel,
    onNavigateBack: () -> Unit
) {
    val display by viewModel.displayInfo.collectAsStateWithLifecycle()

    DevInfoScaffold(
        title = stringResource(R.string.screen_display),
        onNavigateBack = onNavigateBack
    ) { padding ->
        val info = display
        if (info == null) {
            LoadingState(modifier = Modifier.padding(padding))
            return@DevInfoScaffold
        }

        DetailColumn(padding) {
            InfoCard(title = stringResource(R.string.display_section_screen)) {
                DetailRow(
                    stringResource(R.string.display_resolution),
                    Formatters.resolution(info.widthPixels, info.heightPixels)
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                // Null when the device reports bogus xdpi/ydpi.
                DetailRow(
                    stringResource(R.string.display_size),
                    Formatters.inches(info.diagonalInches)
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(
                    stringResource(R.string.display_density),
                    stringResource(
                        R.string.display_density_format,
                        info.densityDpi,
                        Labels.of(info.densityBucket)
                    )
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(
                    stringResource(R.string.display_refresh_rate),
                    Formatters.hertz(info.refreshRateHz)
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(stringResource(R.string.display_hdr), info.hdrSupported.supported())
            }
        }
    }
}
