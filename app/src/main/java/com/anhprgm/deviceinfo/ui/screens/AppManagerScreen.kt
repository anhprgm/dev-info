package com.anhprgm.deviceinfo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.anhprgm.deviceinfo.data.models.AppInfo
import com.anhprgm.deviceinfo.ui.components.DetailRow
import com.anhprgm.deviceinfo.ui.components.InfoCard
import com.anhprgm.deviceinfo.ui.components.LoadingState
import com.anhprgm.deviceinfo.ui.viewmodel.DeviceInfoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppManagerScreen(
    viewModel: DeviceInfoViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAppDetail: (AppInfo) -> Unit
) {
    val appManagerInfo by viewModel.appManagerInfo.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAppManagerInfo()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "App Manager",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        appManagerInfo?.let { appManager ->
            val maxAppsToShow = 50
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    InfoCard(title = "Overview") {
                        DetailRow(label = "Total Apps", value = appManager.totalApps.toString())
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        DetailRow(label = "User Apps", value = appManager.userApps.toString())
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        DetailRow(label = "System Apps", value = appManager.systemApps.toString())
                    }
                }

                items(appManager.apps.take(maxAppsToShow)) { app ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToAppDetail(app) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // App Icon
                            app.icon?.let { drawable ->
                                Image(
                                    bitmap = drawable.toBitmap(96, 96).asImageBitmap(),
                                    contentDescription = "App Icon",
                                    modifier = Modifier.size(48.dp)
                                )
                            } ?: Box(
                                modifier = Modifier.size(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Apps,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            // App Info
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.appName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = app.packageName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "v${app.versionName}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    if (app.permissions.isNotEmpty()) {
                                        Text(
                                            text = "${app.permissions.size}+ permissions",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                            
                            // Arrow indicator
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "View Details",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                if (appManager.apps.size > maxAppsToShow) {
                    item {
                        Text(
                            text = "Showing first $maxAppsToShow apps out of ${appManager.apps.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        } ?: LoadingState(modifier = Modifier.padding(paddingValues))
    }
}
