package com.anhprgm.deviceinfo.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.anhprgm.deviceinfo.ui.screens.*
import com.anhprgm.deviceinfo.ui.viewmodel.DeviceInfoViewModel

@Composable
fun DevInfoNavHost(
    navController: NavHostController = rememberNavController()
) {
    val animationsEnabled = rememberAnimationsEnabled()
    // Activity-scoped: one instance shared by every destination, so the device
    // data is read once rather than per screen.
    val viewModel: DeviceInfoViewModel = hiltViewModel()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                TopLevelDestination.entries.forEach { tab ->
                    val selected = currentDestination?.hierarchy
                        ?.any { it.hasRoute(tab.graphClass) } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick = { navController.switchTab(tab) },
                        icon = {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon
                                else tab.unselectedIcon,
                                contentDescription = null
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(tab.labelRes),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = InfoGraph,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { forwardEnter(animationsEnabled) },
            exitTransition = { forwardExit(animationsEnabled) },
            popEnterTransition = { backEnter(animationsEnabled) },
            popExitTransition = { backExit(animationsEnabled) }
        ) {
            infoGraph(navController, viewModel, padding, animationsEnabled)
            monitorGraph(navController, viewModel, padding, animationsEnabled)
            toolsGraph(navController, viewModel, padding, animationsEnabled)
        }
    }
}

/**
 * Standard M3 tab switching: pop to the tab's start destination while saving
 * the outgoing tab's stack, so returning to a tab restores where you were.
 */
private fun NavHostController.switchTab(tab: TopLevelDestination) {
    navigate(tab.graph) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

// ---- Info -----------------------------------------------------------------

private fun NavGraphBuilder.infoGraph(
    navController: NavHostController,
    viewModel: DeviceInfoViewModel,
    padding: PaddingValues,
    animationsEnabled: Boolean
) {
    navigation<InfoGraph>(
        startDestination = Dashboard,
        // Tabs are peers, so they cross-fade; only drilling down slides.
        enterTransition = { tabEnter(animationsEnabled) },
        exitTransition = { tabExit(animationsEnabled) }
    ) {
        composable<Dashboard> {
            DashboardScreen(
                viewModel = viewModel,
                contentPadding = padding,
                onNavigate = navController::navigate
            )
        }
        composable<DeviceDetail> {
            DeviceDetailScreen(viewModel, navController::popBackStack)
        }
        composable<Hardware> {
            HardwareScreen(viewModel, navController::popBackStack)
        }
        composable<Battery> {
            BatteryScreen(viewModel, navController::popBackStack)
        }
        composable<Display> {
            DisplayScreen(viewModel, navController::popBackStack)
        }
        composable<Network> {
            NetworkScreen(viewModel, navController::popBackStack)
        }
        composable<Camera> {
            CameraScreen(viewModel, navController::popBackStack)
        }
        composable<Sensors> {
            SensorScreen(viewModel, navController::popBackStack)
        }
    }
}

// ---- Monitor --------------------------------------------------------------

private fun NavGraphBuilder.monitorGraph(
    navController: NavHostController,
    viewModel: DeviceInfoViewModel,
    padding: PaddingValues,
    animationsEnabled: Boolean
) {
    navigation<MonitorGraph>(
        startDestination = Monitoring,
        enterTransition = { tabEnter(animationsEnabled) },
        exitTransition = { tabExit(animationsEnabled) }
    ) {
        composable<Monitoring> {
            MonitoringScreen(
                viewModel = viewModel,
                contentPadding = padding,
                onNavigateToHistory = { navController.navigate(History) },
                onNavigateToBenchmark = { navController.navigate(Benchmark) }
            )
        }
        composable<History> {
            HistoryScreen(viewModel, navController::popBackStack)
        }
        composable<Benchmark> {
            BenchmarkScreen(viewModel, navController::popBackStack)
        }
    }
}

// ---- Tools ----------------------------------------------------------------

private fun NavGraphBuilder.toolsGraph(
    navController: NavHostController,
    viewModel: DeviceInfoViewModel,
    padding: PaddingValues,
    animationsEnabled: Boolean
) {
    navigation<ToolsGraph>(
        startDestination = ToolsHub,
        enterTransition = { tabEnter(animationsEnabled) },
        exitTransition = { tabExit(animationsEnabled) }
    ) {
        composable<ToolsHub> {
            ToolsHubScreen(
                contentPadding = padding,
                onNavigate = navController::navigate
            )
        }
        composable<AppList> {
            AppManagerScreen(
                viewModel = viewModel,
                onNavigateBack = navController::popBackStack,
                onNavigateToAppDetail = { packageName ->
                    navController.navigate(AppDetail(packageName))
                }
            )
        }
        composable<AppDetail> { entry ->
            // The package name rides in the route, so this screen survives
            // process death and works from a deep link — unlike the old version,
            // which read it back out of shared ViewModel state.
            val route: AppDetail = entry.toRoute()
            AppDetailScreen(
                packageName = route.packageName,
                onNavigateBack = navController::popBackStack
            )
        }
        composable<Settings> {
            SettingsScreen(onNavigateBack = navController::popBackStack)
        }
    }
}
