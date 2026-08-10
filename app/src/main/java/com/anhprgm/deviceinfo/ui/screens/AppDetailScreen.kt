package com.anhprgm.deviceinfo.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anhprgm.deviceinfo.R
import com.anhprgm.deviceinfo.data.models.AppInfo
import com.anhprgm.deviceinfo.ui.components.DetailColumn
import com.anhprgm.deviceinfo.ui.components.DevInfoScaffold
import com.anhprgm.deviceinfo.ui.components.CopyableRow
import com.anhprgm.deviceinfo.ui.components.DetailRow
import com.anhprgm.deviceinfo.ui.components.EmptyState
import com.anhprgm.deviceinfo.ui.components.InfoCard
import com.anhprgm.deviceinfo.ui.components.LoadingState
import com.anhprgm.deviceinfo.ui.format.Formatters
import com.anhprgm.deviceinfo.ui.theme.Dimens
import com.anhprgm.deviceinfo.ui.theme.MonospaceValue
import com.anhprgm.deviceinfo.ui.viewmodel.AppDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    packageName: String,
    onNavigateBack: () -> Unit,
    viewModel: AppDetailViewModel = hiltViewModel()
) {
    val app by viewModel.app.collectAsStateWithLifecycle()
    val icon by viewModel.icon.collectAsStateWithLifecycle()
    val notFound by viewModel.notFound.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val copiedMessage = stringResource(R.string.action_copied)

    DevInfoScaffold(
        title = stringResource(R.string.screen_app_detail),
        onNavigateBack = onNavigateBack,
        actions = {
            app?.let { info ->
                IconButton(onClick = { shareApp(context, info) }) {
                    Icon(Icons.Default.Share, stringResource(R.string.action_share))
                }
                IconButton(onClick = { openAppSettings(context, info.packageName) }) {
                    Icon(Icons.Default.Settings, stringResource(R.string.app_open_settings))
                }
            }
        }
    ) { padding ->
        val current = app
        when {
            notFound -> EmptyState(
                icon = Icons.Default.SearchOff,
                title = stringResource(R.string.common_unknown),
                description = packageName,
                modifier = Modifier.padding(padding)
            )

            current == null -> LoadingState(modifier = Modifier.padding(padding))

            else -> DetailColumn(padding) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.spaceXl),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        icon?.let { drawable ->
                            Image(
                                bitmap = drawable.toBitmap(128, 128).asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.size(88.dp)
                            )
                        } ?: Box(
                            modifier = Modifier.size(88.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Apps,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(Modifier.height(Dimens.spaceLg))
                        Text(
                            text = current.appName,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "v${Formatters.text(current.versionName)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(Modifier.height(Dimens.cardSpacing))

                InfoCard(title = stringResource(R.string.device_section_basic)) {
                    CopyableRow(
                        label = stringResource(R.string.app_package),
                        value = current.packageName,
                        copiedMessage = copiedMessage,
                        valueStyle = MonospaceValue
                    )
                    HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                    DetailRow(
                        stringResource(R.string.app_version),
                        Formatters.text(current.versionName)
                    )
                    HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                    DetailRow(
                        stringResource(R.string.app_version_code),
                        current.versionCode.toString()
                    )
                    HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                    DetailRow(
                        stringResource(R.string.app_installed),
                        formatDate(current.firstInstallTimeMillis)
                    )
                    HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                    DetailRow(
                        stringResource(R.string.app_updated),
                        formatDate(current.lastUpdateTimeMillis)
                    )
                    HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                    DetailRow(
                        stringResource(R.string.app_size),
                        Formatters.bytes(current.apkSizeBytes)
                    )
                    HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                    DetailRow(
                        stringResource(R.string.app_type),
                        stringResource(
                            if (current.isSystemApp) R.string.apps_filter_system
                            else R.string.apps_filter_user
                        )
                    )
                    HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                    DetailRow(
                        stringResource(R.string.app_target_sdk),
                        current.targetSdk.toString()
                    )
                }

                if (current.permissions.isNotEmpty()) {
                    Spacer(Modifier.height(Dimens.cardSpacing))
                    InfoCard(
                        title = "${stringResource(R.string.app_permissions)} " +
                            "(${current.permissions.size})"
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceXs)) {
                            current.permissions.forEach { permission ->
                                Text(
                                    text = permission.substringAfterLast('.'),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun openAppSettings(context: Context, packageName: String) {
    runCatching {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
        )
    }
}

private fun shareApp(context: Context, appInfo: AppInfo) {
    val text = buildString {
        appendLine(appInfo.appName)
        appendLine(appInfo.packageName)
        appendLine("v${appInfo.versionName ?: "?"}")
        appendLine()
        append("https://play.google.com/store/apps/details?id=${appInfo.packageName}")
    }
    runCatching {
        context.startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, appInfo.appName)
                    putExtra(Intent.EXTRA_TEXT, text)
                },
                null
            )
        )
    }
}

/**
 * Install timestamps are stored raw on the model so the app list can sort by
 * the real epoch value; formatting happens only here.
 */
private fun formatDate(millis: Long): String =
    if (millis <= 0L) Formatters.NOT_AVAILABLE
    else SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(millis))
