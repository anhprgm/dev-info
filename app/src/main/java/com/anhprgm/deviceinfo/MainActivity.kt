package com.anhprgm.deviceinfo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anhprgm.deviceinfo.ui.screens.*
import com.anhprgm.deviceinfo.ui.theme.DevInfoTheme
import com.anhprgm.deviceinfo.ui.viewmodel.DeviceInfoViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DevInfoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DevInfoApp()
                }
            }
        }
    }
}

@Composable
fun DevInfoApp() {
    val navController = rememberNavController()
    // Scoped to the Activity so every destination shares one instance; Hilt
    // builds it, so the repository is no longer reconstructed on recomposition.
    val viewModel: DeviceInfoViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToDevice = { navController.navigate("device") },
                onNavigateToHardware = { navController.navigate("hardware") },
                onNavigateToBattery = { navController.navigate("battery") },
                onNavigateToNetwork = { navController.navigate("network") },
                onNavigateToDisplay = { navController.navigate("display") },
                onNavigateToCamera = { navController.navigate("camera") },
                onNavigateToSensor = { navController.navigate("sensor") },
                onNavigateToHistory = { navController.navigate("history") },
                onNavigateToAppManager = { navController.navigate("appmanager") },
                onNavigateToMonitoring = { navController.navigate("monitoring") },
                onNavigateToBenchmark = { navController.navigate("benchmark") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("device") {
            DeviceDetailScreen(viewModel = viewModel, onNavigateBack = navController::popBackStack)
        }
        composable("hardware") {
            HardwareScreen(viewModel = viewModel, onNavigateBack = navController::popBackStack)
        }
        composable("battery") {
            BatteryScreen(viewModel = viewModel, onNavigateBack = navController::popBackStack)
        }
        composable("network") {
            NetworkScreen(viewModel = viewModel, onNavigateBack = navController::popBackStack)
        }
        composable("display") {
            DisplayScreen(viewModel = viewModel, onNavigateBack = navController::popBackStack)
        }
        composable("camera") {
            CameraScreen(viewModel = viewModel, onNavigateBack = navController::popBackStack)
        }
        composable("sensor") {
            SensorScreen(viewModel = viewModel, onNavigateBack = navController::popBackStack)
        }
        composable("history") {
            HistoryScreen(viewModel = viewModel, onNavigateBack = navController::popBackStack)
        }
        composable("appmanager") {
            AppManagerScreen(
                viewModel = viewModel,
                onNavigateBack = navController::popBackStack,
                onNavigateToAppDetail = { appInfo ->
                    viewModel.selectApp(appInfo)
                    navController.navigate("appdetail")
                }
            )
        }
        composable("appdetail") {
            // Phase 2 replaces this with a typed route carrying the package name;
            // today a process death here leaves the screen with nothing to show.
            val selectedApp = viewModel.selectedApp.collectAsState().value
            selectedApp?.let { app ->
                AppDetailScreen(
                    appInfo = app,
                    viewModel = viewModel,
                    onNavigateBack = navController::popBackStack
                )
            }
        }
        composable("monitoring") {
            MonitoringScreen(viewModel = viewModel, onNavigateBack = navController::popBackStack)
        }
        composable("benchmark") {
            BenchmarkScreen(viewModel = viewModel, onNavigateBack = navController::popBackStack)
        }
        composable("settings") {
            SettingsScreen(onNavigateBack = navController::popBackStack)
        }
    }
}
