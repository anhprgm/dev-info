package com.anhprgm.deviceinfo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.anhprgm.deviceinfo.R
import com.anhprgm.deviceinfo.navigation.AppList
import com.anhprgm.deviceinfo.navigation.Compare
import com.anhprgm.deviceinfo.navigation.ExportReport
import com.anhprgm.deviceinfo.navigation.Settings as SettingsRoute
import com.anhprgm.deviceinfo.ui.components.CategoryCard
import com.anhprgm.deviceinfo.ui.theme.Dimens

/**
 * Entry point of the Tools tab. Export, device card and comparison land here
 * in Phase 5; the widget setup entry in Phase 6.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsHubScreen(
    contentPadding: PaddingValues,
    onNavigate: (Any) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_tools)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Dimens.screenPadding,
                end = Dimens.screenPadding,
                top = innerPadding.calculateTopPadding() + Dimens.spaceSm,
                bottom = contentPadding.calculateBottomPadding() + Dimens.spaceXl
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.cardSpacing)
        ) {
            item {
                // Subtitles are single-line; the package-visibility caveat is
                // too long here and was being ellipsised. It lives on the list
                // screen itself instead.
                CategoryCard(
                    icon = Icons.Default.Apps,
                    title = stringResource(R.string.screen_apps),
                    subtitle = stringResource(R.string.tools_apps_desc),
                    onClick = { onNavigate(AppList) }
                )
            }
            item {
                CategoryCard(
                    icon = Icons.Default.IosShare,
                    title = stringResource(R.string.export_title),
                    subtitle = stringResource(R.string.export_desc),
                    onClick = { onNavigate(ExportReport) }
                )
            }
            item {
                CategoryCard(
                    icon = Icons.Default.CompareArrows,
                    title = stringResource(R.string.compare_title),
                    subtitle = stringResource(R.string.compare_desc),
                    onClick = { onNavigate(Compare) }
                )
            }
            item {
                CategoryCard(
                    icon = Icons.Default.Settings,
                    title = stringResource(R.string.screen_settings),
                    subtitle = stringResource(R.string.tools_settings_desc),
                    onClick = { onNavigate(SettingsRoute) }
                )
            }
        }
    }
}
