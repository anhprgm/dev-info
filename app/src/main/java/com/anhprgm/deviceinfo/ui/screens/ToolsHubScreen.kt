package com.anhprgm.deviceinfo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
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
                CategoryCard(
                    icon = Icons.Default.Apps,
                    title = stringResource(R.string.screen_apps),
                    subtitle = stringResource(R.string.apps_visibility_note),
                    onClick = { onNavigate(AppList) }
                )
            }
            item {
                CategoryCard(
                    icon = Icons.Default.Settings,
                    title = stringResource(R.string.screen_settings),
                    subtitle = stringResource(R.string.settings_appearance),
                    onClick = { onNavigate(SettingsRoute) }
                )
            }
        }
    }
}
