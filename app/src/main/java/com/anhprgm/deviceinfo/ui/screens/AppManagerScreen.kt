package com.anhprgm.deviceinfo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

enum class SortOption {
    NAME_ASC, NAME_DESC, INSTALL_DATE_ASC, INSTALL_DATE_DESC, PACKAGE_NAME
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppManagerScreen(
    viewModel: DeviceInfoViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAppDetail: (AppInfo) -> Unit
) {
    val appManagerInfo by viewModel.appManagerInfo.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf(SortOption.NAME_ASC) }
    var showSortMenu by remember { mutableStateOf(false) }
    var filterType by remember { mutableStateOf("All") }
    var showFilterMenu by remember { mutableStateOf(false) }

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
                actions = {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Apps") },
                            onClick = {
                                filterType = "All"
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("User Apps") },
                            onClick = {
                                filterType = "User"
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("System Apps") },
                            onClick = {
                                filterType = "System"
                                showFilterMenu = false
                            }
                        )
                    }
                    
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Sort")
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Name (A-Z)") },
                            onClick = {
                                sortOption = SortOption.NAME_ASC
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Name (Z-A)") },
                            onClick = {
                                sortOption = SortOption.NAME_DESC
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Install Date (Newest)") },
                            onClick = {
                                sortOption = SortOption.INSTALL_DATE_DESC
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Install Date (Oldest)") },
                            onClick = {
                                sortOption = SortOption.INSTALL_DATE_ASC
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Package Name") },
                            onClick = {
                                sortOption = SortOption.PACKAGE_NAME
                                showSortMenu = false
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        appManagerInfo?.let { appManager ->
            // Filter and sort apps
            val filteredApps = remember(appManager.apps, searchQuery, filterType, sortOption) {
                var apps = appManager.apps
                
                // Apply search filter
                if (searchQuery.isNotEmpty()) {
                    apps = apps.filter { app ->
                        app.appName.contains(searchQuery, ignoreCase = true) ||
                        app.packageName.contains(searchQuery, ignoreCase = true)
                    }
                }
                
                // Apply type filter
                apps = when (filterType) {
                    "User" -> apps.filter { !it.packageName.startsWith("com.android") && 
                                           !it.packageName.startsWith("android") }
                    "System" -> apps.filter { it.packageName.startsWith("com.android") || 
                                              it.packageName.startsWith("android") }
                    else -> apps
                }
                
                // Apply sorting
                when (sortOption) {
                    SortOption.NAME_ASC -> apps.sortedBy { it.appName.lowercase() }
                    SortOption.NAME_DESC -> apps.sortedByDescending { it.appName.lowercase() }
                    SortOption.INSTALL_DATE_ASC -> apps.sortedBy { it.installTime }
                    SortOption.INSTALL_DATE_DESC -> apps.sortedByDescending { it.installTime }
                    SortOption.PACKAGE_NAME -> apps.sortedBy { it.packageName }
                }
            }
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    InfoCard(title = "Overview") {
                        DetailRow(label = "Total Apps", value = appManager.totalApps.toString())
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        DetailRow(label = "User Apps", value = appManager.userApps.toString())
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        DetailRow(label = "System Apps", value = appManager.systemApps.toString())
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        DetailRow(label = "Filtered", value = filteredApps.size.toString())
                    }
                }
                
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search apps...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true
                    )
                }

                items(filteredApps, key = { it.packageName }) { app ->
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
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // App Icon - Load lazily
                            app.icon?.let { drawable ->
                                Image(
                                    bitmap = drawable.toBitmap(48, 48).asImageBitmap(),
                                    contentDescription = "App Icon",
                                    modifier = Modifier.size(40.dp)
                                )
                            } ?: Box(
                                modifier = Modifier.size(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Apps,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            // App Info
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.appName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                
                                Text(
                                    text = app.packageName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                
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
                                            text = "${app.permissions.size}+ perms",
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
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        } ?: LoadingState(modifier = Modifier.padding(paddingValues))
    }
}
