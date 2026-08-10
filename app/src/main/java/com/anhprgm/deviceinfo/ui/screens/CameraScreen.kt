package com.anhprgm.deviceinfo.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NoPhotography
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
fun CameraScreen(
    viewModel: DeviceInfoViewModel,
    onNavigateBack: () -> Unit
) {
    val camera by viewModel.cameraInfo.collectAsStateWithLifecycle()

    DevInfoScaffold(
        title = stringResource(R.string.screen_camera),
        onNavigateBack = onNavigateBack
    ) { padding ->
        val info = camera
        when {
            info == null -> LoadingState(modifier = Modifier.padding(padding))

            info.cameras.isEmpty() -> EmptyState(
                icon = Icons.Default.NoPhotography,
                title = stringResource(R.string.common_not_supported),
                modifier = Modifier.padding(padding)
            )

            else -> DetailColumn(padding) {
                InfoCard(title = stringResource(R.string.common_overview)) {
                    DetailRow(
                        stringResource(R.string.camera_count),
                        info.cameraCount.toString()
                    )
                }

                info.cameras.forEachIndexed { index, lens ->
                    Spacer(Modifier.height(Dimens.cardSpacing))
                    InfoCard(title = stringResource(R.string.camera_number, index + 1)) {
                        DetailRow(stringResource(R.string.camera_id), lens.cameraId)
                        HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                        DetailRow(stringResource(R.string.camera_facing), lens.facing.label())
                        HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                        DetailRow(
                            stringResource(R.string.camera_resolution),
                            Formatters.megapixels(lens.megapixels)
                        )
                        HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                        DetailRow(
                            stringResource(R.string.camera_image_size),
                            Formatters.resolution(lens.pixelWidth, lens.pixelHeight)
                        )
                        HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                        DetailRow(
                            stringResource(R.string.camera_focal_length),
                            lens.focalLengthsMm
                                .joinToString(", ") { Formatters.millimetres(it) }
                                .ifBlank { Formatters.NOT_AVAILABLE }
                        )
                        HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                        DetailRow(
                            stringResource(R.string.camera_aperture),
                            lens.aperturesFStop
                                .joinToString(", ") { Formatters.aperture(it) }
                                .ifBlank { Formatters.NOT_AVAILABLE }
                        )
                        HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                        DetailRow(
                            stringResource(R.string.camera_flash),
                            lens.flashAvailable.yesNo()
                        )
                        HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                        DetailRow(
                            stringResource(R.string.camera_ois),
                            lens.opticalStabilization.yesNo()
                        )
                        HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                        DetailRow(
                            stringResource(R.string.camera_ae_lock),
                            lens.autoExposureLock.yesNo()
                        )
                        HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                        DetailRow(
                            stringResource(R.string.camera_awb_lock),
                            lens.autoWhiteBalanceLock.yesNo()
                        )
                        HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                        DetailRow(
                            stringResource(R.string.camera_orientation),
                            "${lens.sensorOrientation}°"
                        )
                        HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                        DetailRow(
                            stringResource(R.string.camera_formats),
                            lens.outputFormats.joinToString(", ")
                                .ifBlank { Formatters.NOT_AVAILABLE }
                        )
                    }
                }
            }
        }
    }
}
