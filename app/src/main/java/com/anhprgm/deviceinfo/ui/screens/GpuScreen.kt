package com.anhprgm.deviceinfo.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anhprgm.deviceinfo.R
import com.anhprgm.deviceinfo.ui.components.CopyableRow
import com.anhprgm.deviceinfo.ui.components.DetailColumn
import com.anhprgm.deviceinfo.ui.components.DetailRow
import com.anhprgm.deviceinfo.ui.components.DevInfoScaffold
import com.anhprgm.deviceinfo.ui.components.InfoCard
import com.anhprgm.deviceinfo.ui.components.LoadingState
import com.anhprgm.deviceinfo.ui.format.Formatters
import com.anhprgm.deviceinfo.ui.format.supported
import com.anhprgm.deviceinfo.ui.theme.Dimens
import com.anhprgm.deviceinfo.ui.theme.MonospaceValue
import com.anhprgm.deviceinfo.ui.viewmodel.SystemInfoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpuScreen(
    onNavigateBack: () -> Unit,
    viewModel: SystemInfoViewModel = hiltViewModel()
) {
    val gpu by viewModel.gpu.collectAsStateWithLifecycle()
    val copied = stringResource(R.string.action_copied)
    var extensionsExpanded by remember { mutableStateOf(false) }

    DevInfoScaffold(
        title = stringResource(R.string.gpu_section),
        onNavigateBack = onNavigateBack
    ) { padding ->
        val info = gpu
        if (info == null) {
            LoadingState(modifier = Modifier.padding(padding))
            return@DevInfoScaffold
        }

        DetailColumn(padding) {
            InfoCard(title = stringResource(R.string.gpu_section)) {
                // Renderer/vendor come from a throwaway 1x1 EGL pbuffer; they
                // are null only if a GL context could not be created at all.
                CopyableRow(
                    label = stringResource(R.string.gpu_renderer),
                    value = Formatters.text(info.renderer),
                    copiedMessage = copied
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(stringResource(R.string.gpu_vendor), Formatters.text(info.vendor))
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(
                    stringResource(R.string.gpu_gl_version),
                    Formatters.text(info.glVersion)
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(stringResource(R.string.gpu_gles_version), info.glesVersion)
            }

            Spacer(Modifier.height(Dimens.cardSpacing))

            InfoCard(title = stringResource(R.string.vulkan_section)) {
                DetailRow(
                    stringResource(R.string.vulkan_support),
                    info.vulkan.supported.supported()
                )
                if (info.vulkan.supported) {
                    HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                    DetailRow(
                        stringResource(R.string.vulkan_api_version),
                        Formatters.text(info.vulkan.apiVersion)
                    )
                    HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                    DetailRow(
                        stringResource(R.string.vulkan_hardware_level),
                        info.vulkan.hardwareLevel?.toString() ?: Formatters.NOT_AVAILABLE
                    )
                }
                Spacer(Modifier.height(Dimens.spaceSm))
                Text(
                    text = stringResource(R.string.vulkan_device_note),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (info.extensions.isNotEmpty()) {
                Spacer(Modifier.height(Dimens.cardSpacing))
                InfoCard(title = stringResource(R.string.gpu_extensions)) {
                    // Hundreds of entries; collapsed by default so the screen
                    // stays scannable.
                    Text(
                        text = stringResource(
                            R.string.gpu_extension_count,
                            info.extensions.size
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { extensionsExpanded = !extensionsExpanded }
                            .padding(vertical = Dimens.spaceSm)
                    )
                    if (extensionsExpanded) {
                        Text(
                            text = info.extensions.joinToString("\n"),
                            style = MonospaceValue,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
