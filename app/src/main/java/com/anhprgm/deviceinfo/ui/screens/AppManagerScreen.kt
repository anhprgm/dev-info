package com.anhprgm.deviceinfo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anhprgm.deviceinfo.R
import com.anhprgm.deviceinfo.data.models.AppInfo
import com.anhprgm.deviceinfo.ui.components.DetailRow
import com.anhprgm.deviceinfo.ui.components.DevInfoScaffold
import com.anhprgm.deviceinfo.ui.components.InfoCard
import com.anhprgm.deviceinfo.ui.components.LoadingState
import com.anhprgm.deviceinfo.ui.components.NoticeCard
import com.anhprgm.deviceinfo.ui.format.Formatters
import com.anhprgm.deviceinfo.ui.theme.Dimens
import com.anhprgm.deviceinfo.ui.viewmodel.DeviceInfoViewModel

enum class SortOption { NAME_ASC, NAME_DESC, INSTALL_DATE_ASC, INSTALL_DATE_DESC, PACKAGE_NAME }

private enum class AppFilter { ALL, USER, SYSTEM }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppManagerScreen(
    viewModel: DeviceInfoViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAppDetail: (String) -> Unit
) {
    val appManager by viewModel.appManagerInfo.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf(SortOption.NAME_ASC) }
    var filter by remember { mutableStateOf(AppFilter.ALL) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadAppManagerInfo() }

    DevInfoScaffold(
        title = stringResource(R.string.screen_apps),
        onNavigateBack = onNavigateBack,
        actions = {
            IconButton(onClick = { showFilterMenu = true }) {
                Icon(Icons.Default.FilterList, stringResource(R.string.action_filter))
            }
            DropdownMenu(showFilterMenu, { showFilterMenu = false }) {
                listOf(
                    AppFilter.ALL to R.string.apps_filter_all,
                    AppFilter.USER to R.string.apps_filter_user,
                    AppFilter.SYSTEM to R.string.apps_filter_system
                ).forEach { (value, labelRes) ->
                    DropdownMenuItem(
                        text = { Text(stringResource(labelRes)) },
                        onClick = { filter = value; showFilterMenu = false }
                    )
                }
            }

            IconButton(onClick = { showSortMenu = true }) {
                Icon(Icons.AutoMirrored.Filled.Sort, stringResource(R.string.action_sort))
            }
            DropdownMenu(showSortMenu, { showSortMenu = false }) {
                listOf(
                    SortOption.NAME_ASC to R.string.apps_sort_name_asc,
                    SortOption.NAME_DESC to R.string.apps_sort_name_desc,
                    SortOption.INSTALL_DATE_DESC to R.string.apps_sort_newest,
                    SortOption.INSTALL_DATE_ASC to R.string.apps_sort_oldest,
                    SortOption.PACKAGE_NAME to R.string.apps_sort_package
                ).forEach { (value, labelRes) ->
                    DropdownMenuItem(
                        text = { Text(stringResource(labelRes)) },
                        onClick = { sortOption = value; showSortMenu = false }
                    )
                }
            }
        }
    ) { padding ->
        val info = appManager
        if (info == null) {
            LoadingState(modifier = Modifier.padding(padding))
            return@DevInfoScaffold
        }

        val filtered = remember(info.apps, query, filter, sortOption) {
            info.apps
                .filter { app ->
                    query.isEmpty() ||
                        app.appName.contains(query, ignoreCase = true) ||
                        app.packageName.contains(query, ignoreCase = true)
                }
                // FLAG_SYSTEM, not a package-name prefix guess: com.android.chrome
                // is a user-updatable app, not a system one.
                .filter { app ->
                    when (filter) {
                        AppFilter.ALL -> true
                        AppFilter.USER -> !app.isSystemApp
                        AppFilter.SYSTEM -> app.isSystemApp
                    }
                }
                // Sorts on the raw timestamp; the old code sorted a formatted date.
                .let { apps ->
                    when (sortOption) {
                        SortOption.NAME_ASC -> apps.sortedBy { it.appName.lowercase() }
                        SortOption.NAME_DESC -> apps.sortedByDescending { it.appName.lowercase() }
                        SortOption.INSTALL_DATE_ASC -> apps.sortedBy { it.firstInstallTimeMillis }
                        SortOption.INSTALL_DATE_DESC ->
                            apps.sortedByDescending { it.firstInstallTimeMillis }
                        SortOption.PACKAGE_NAME -> apps.sortedBy { it.packageName }
                    }
                }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.cardSpacing)
        ) {
            item {
                InfoCard(title = stringResource(R.string.common_overview)) {
                    DetailRow(stringResource(R.string.apps_total), info.totalApps.toString())
                    DetailRow(stringResource(R.string.apps_user), info.userApps.toString())
                    DetailRow(stringResource(R.string.apps_system), info.systemApps.toString())
                    DetailRow(stringResource(R.string.apps_filtered), filtered.size.toString())
                }
            }

            // The list is limited by <queries>, not by a bug — say so up front.
            item {
                NoticeCard(
                    title = stringResource(R.string.screen_apps),
                    message = stringResource(R.string.apps_visibility_note)
                )
            }

            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.apps_search_hint)) },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true
                )
            }

            items(filtered, key = { it.packageName }) { app ->
                AppRow(
                    app = app,
                    viewModel = viewModel,
                    onClick = { onNavigateToAppDetail(app.packageName) }
                )
            }
        }
    }
}

@Composable
private fun AppRow(
    app: AppInfo,
    viewModel: DeviceInfoViewModel,
    onClick: () -> Unit
) {
    // Icons load per row rather than for every installed app up front.
    var icon by remember(app.packageName) { mutableStateOf(app.icon) }
    LaunchedEffect(app.packageName) {
        if (icon == null) icon = viewModel.getAppIcon(app.packageName)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let { drawable ->
                Image(
                    bitmap = drawable.toBitmap(96, 96).asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            } ?: Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(Dimens.spaceLg))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = Formatters.bytes(app.apkSizeBytes),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
