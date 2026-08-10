package com.anhprgm.deviceinfo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import com.anhprgm.deviceinfo.data.models.MediaCodecDetail
import com.anhprgm.deviceinfo.ui.components.DetailRow
import com.anhprgm.deviceinfo.ui.components.DevInfoScaffold
import com.anhprgm.deviceinfo.ui.components.InfoCard
import com.anhprgm.deviceinfo.ui.components.LoadingState
import com.anhprgm.deviceinfo.ui.theme.Dimens
import com.anhprgm.deviceinfo.ui.viewmodel.SystemInfoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodecScreen(
    onNavigateBack: () -> Unit,
    viewModel: SystemInfoViewModel = hiltViewModel()
) {
    val codecs by viewModel.codecs.collectAsStateWithLifecycle()
    var showEncoders by remember { mutableStateOf(false) }

    DevInfoScaffold(
        title = stringResource(R.string.screen_codecs),
        onNavigateBack = onNavigateBack
    ) { padding ->
        val info = codecs
        if (info == null) {
            LoadingState(modifier = Modifier.padding(padding))
            return@DevInfoScaffold
        }

        val shown = if (showEncoders) info.encoders else info.decoders

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.cardSpacing)
        ) {
            item {
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
                ) {
                    FilterChip(
                        selected = !showEncoders,
                        onClick = { showEncoders = false },
                        label = {
                            Text(
                                stringResource(R.string.codec_decoders) +
                                    " (${info.decoders.size})"
                            )
                        }
                    )
                    FilterChip(
                        selected = showEncoders,
                        onClick = { showEncoders = true },
                        label = {
                            Text(
                                stringResource(R.string.codec_encoders) +
                                    " (${info.encoders.size})"
                            )
                        }
                    )
                }
            }

            items(shown, key = { it.name }) { codec -> CodecCard(codec) }
        }
    }
}

@Composable
private fun CodecCard(codec: MediaCodecDetail) {
    InfoCard(title = codec.name) {
        DetailRow(
            label = stringResource(R.string.codec_hardware),
            // isHardwareAccelerated only exists from API 29; below that the
            // data source leaves it false rather than guessing from the name.
            value = if (codec.isHardwareAccelerated) {
                stringResource(R.string.codec_hardware)
            } else if (codec.isSoftwareOnly) {
                stringResource(R.string.codec_software)
            } else {
                stringResource(R.string.common_unknown)
            }
        )
        Text(
            text = codec.mimeTypes.joinToString(", "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
