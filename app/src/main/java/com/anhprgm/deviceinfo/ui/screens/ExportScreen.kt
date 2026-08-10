package com.anhprgm.deviceinfo.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anhprgm.deviceinfo.R
import com.anhprgm.deviceinfo.data.export.ExportFormat
import com.anhprgm.deviceinfo.ui.components.DetailColumn
import com.anhprgm.deviceinfo.ui.components.DevInfoScaffold
import com.anhprgm.deviceinfo.ui.components.HeroCard
import com.anhprgm.deviceinfo.ui.components.InfoCard
import com.anhprgm.deviceinfo.ui.components.LoadingState
import com.anhprgm.deviceinfo.ui.format.Formatters
import com.anhprgm.deviceinfo.ui.theme.Dimens
import com.anhprgm.deviceinfo.ui.theme.MonospaceValue
import com.anhprgm.deviceinfo.ui.viewmodel.ExportViewModel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExportViewModel = hiltViewModel()
) {
    val report by viewModel.report.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val shareIntent by viewModel.shareIntent.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // The chooser is launched as a side effect so the ViewModel stays free of
    // Activity references.
    LaunchedEffect(shareIntent) {
        shareIntent?.let { intent ->
            runCatching { context.startActivity(intent) }
            viewModel.consumeShareIntent()
        }
    }

    // Captures the composed hero card as a PNG. This is the supported Compose
    // capture path; the old approach of attaching a ComposeView to a window
    // just to call drawToBitmap is no longer needed.
    val graphicsLayer = rememberGraphicsLayer()

    DevInfoScaffold(
        title = stringResource(R.string.export_title),
        onNavigateBack = onNavigateBack
    ) { padding ->
        val current = report
        if (current == null) {
            LoadingState(modifier = Modifier.padding(padding))
            return@DevInfoScaffold
        }

        DetailColumn(padding) {
            InfoCard(title = stringResource(R.string.export_card_title)) {
                Text(
                    text = stringResource(R.string.export_card_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Dimens.spaceMd))

                HeroCard(
                    deviceName = current.device.name,
                    model = "${current.hardware.cpuModel} · " +
                        Formatters.bytes(current.hardware.totalRamBytes),
                    androidVersion = current.device.androidVersion,
                    apiLevel = current.device.apiLevel,
                    uptime = Formatters.resolution(
                        current.display.widthPixels,
                        current.display.heightPixels
                    ),
                    androidLabel = stringResource(R.string.dashboard_android),
                    uptimeLabel = stringResource(R.string.display_resolution),
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawWithContent {
                            graphicsLayer.record { this@drawWithContent.drawContent() }
                            drawLayer(graphicsLayer)
                        }
                )

                Spacer(Modifier.height(Dimens.spaceMd))
                Button(
                    onClick = {
                        scope.launch {
                            val bitmap = graphicsLayer.toImageBitmap()
                            val stream = ByteArrayOutputStream()
                            bitmap.asAndroidBitmap().compress(
                                android.graphics.Bitmap.CompressFormat.PNG, 100, stream
                            )
                            viewModel.shareDeviceCard(stream.toByteArray())
                        }
                    },
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Image, null)
                    Spacer(Modifier.width(Dimens.spaceSm))
                    Text(stringResource(R.string.export_share_card))
                }
            }

            Spacer(Modifier.height(Dimens.cardSpacing))

            InfoCard(title = stringResource(R.string.export_formats)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.export(ExportFormat.TEXT) },
                        enabled = !busy,
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.export_format_txt)) }
                    OutlinedButton(
                        onClick = { viewModel.export(ExportFormat.JSON) },
                        enabled = !busy,
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.export_format_json)) }
                    OutlinedButton(
                        onClick = { viewModel.export(ExportFormat.PDF) },
                        enabled = !busy,
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.export_format_pdf)) }
                }

                if (busy) {
                    Spacer(Modifier.height(Dimens.spaceMd))
                    CircularProgressIndicator(modifier = Modifier.height(24.dp))
                }
                if (error) {
                    Spacer(Modifier.height(Dimens.spaceMd))
                    Text(
                        text = stringResource(R.string.export_failed),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.height(Dimens.cardSpacing))

            InfoCard(title = stringResource(R.string.export_preview)) {
                Text(
                    text = viewModel.preview().take(PREVIEW_CHARS),
                    style = MonospaceValue,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                )
            }
        }
    }
}

/** Enough to show the shape of the report without composing the whole thing. */
private const val PREVIEW_CHARS = 1500
