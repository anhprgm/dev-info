# DevInfo Android App - Build and Run Guide

## Overview
This is a complete Android application that displays comprehensive device information.

## What the App Does

### 1. Home Screen
- Displays quick overview cards for all information categories:
  - Device (Manufacturer, Model)
  - Hardware (RAM, CPU Cores)
  - Battery (Level, Charging Status)
  - Network (Connection Type, Network Name)
  - Display (Resolution, Screen Size)
  - Camera (Number of Cameras)
- Each card is clickable and navigates to detailed information

### 2. Device Details Screen
Shows:
- Device Name (e.g., "Samsung Galaxy S21")
- Manufacturer (e.g., "Samsung")
- Model (e.g., "SM-G991B")
- Brand (e.g., "Samsung")
- Android Version (e.g., "13")
- API Level (e.g., "33")
- Security Patch Date
- Build Fingerprint

### 3. Hardware Screen
Shows:
- Total RAM (e.g., "8.00 GB")
- Available RAM (e.g., "3.45 GB")
- Total Storage (e.g., "128.00 GB")
- Available Storage (e.g., "45.23 GB")
- CPU Information (processor name)
- Number of CPU Cores (e.g., "8")

### 4. Battery Screen
Shows:
- Battery Level as percentage with visual progress bar
- Charging Status (Charging/Discharging/Full)
- Temperature (in Celsius)
- Voltage (in Volts)
- Health Status (Good/Overheat/etc.)
- Technology (e.g., "Li-ion")
- Refresh button to update information

### 5. Network Screen
Shows:
- Connection Type (WiFi/Mobile Data/Ethernet/Disconnected)
- Network Name (WiFi SSID or "Mobile Network")
- IP Address (IPv4)
- Signal Strength (for WiFi connections)
- Refresh button to update information

### 6. Display Screen
Shows:
- Screen Resolution (width x height in pixels)
- Physical Screen Size (diagonal in inches)
- Pixel Density (DPI and category like xhdpi, xxhdpi)
- Refresh Rate (in Hz)

### 7. Camera Screen
Shows:
- Total number of cameras
- For each camera:
  - Camera ID
  - Facing (Front/Back/External)
  - Flash Available (Yes/No)
  - Sensor Orientation (in degrees)

## UI Design

### Material 3 Theme
- Primary Color: Indigo (#3F51B5)
- Secondary Color: Cyan (#00BCD4)
- Clean card-based layout
- Smooth animations and transitions
- Responsive to different screen sizes

### Navigation
- Home screen with overview cards
- Tap any card to see detailed information
- Back button on all detail screens
- Smooth navigation transitions

## Technical Implementation

### Architecture: MVVM
- **Models**: Data classes for each information type
- **Repository**: DeviceInfoRepository fetches data from Android system APIs
- **ViewModel**: DeviceInfoViewModel manages UI state with StateFlow
- **Views**: Jetpack Compose screens with Material 3 components

### Data Sources
All information is retrieved using native Android APIs:
- `Build` class for device info
- `ActivityManager` for RAM
- `StatFs` for storage
- `BatteryManager` for battery info
- `ConnectivityManager` and `WifiManager` for network
- `DisplayMetrics` and `WindowManager` for display
- `CameraManager` for camera capabilities

### Permissions
The app requests minimal permissions:
- `ACCESS_NETWORK_STATE`: Check network connectivity
- `ACCESS_WIFI_STATE`: Get WiFi information
- `INTERNET`: Get IP address
- `CAMERA`: Enumerate camera capabilities (no actual camera usage)

## Building the Project

### Prerequisites
1. Android Studio Hedgehog (2023.1.1) or later
2. JDK 17 or later
3. Android SDK 34
4. Internet connection for initial Gradle sync

### Build Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/anhprgm/dev-info.git
   cd dev-info
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open" and navigate to the project directory
   - Wait for Gradle sync (first sync downloads dependencies)

3. **Build the project**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run on device/emulator**
   - Connect an Android device (API 24+) or start an emulator
   - Click the "Run" button in Android Studio
   - Or use: `./gradlew installDebug`

### Build Configuration
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)
- Compile SDK: 34
- Kotlin: 1.9.20
- AGP: 8.1.0
- Compose: 1.5.4

## Project Files Summary

### Kotlin Files (20 total)
1. MainActivity.kt - Entry point with navigation setup
2. DeviceInfoRepository.kt - Data fetching logic
3-8. Data models (6 files)
9. DeviceInfoViewModel.kt - State management
10-16. UI Screens (7 files)
17. CommonComponents.kt - Reusable UI components
18-20. Theme files (3 files)

### Resource Files
- AndroidManifest.xml - App configuration and permissions
- strings.xml - All text resources
- themes.xml - Material theme configuration
- Launcher icons (various densities)
- Backup and extraction rules

### Build Files
- build.gradle.kts (root) - Project-level Gradle configuration
- build.gradle.kts (app) - Module-level configuration with dependencies
- settings.gradle.kts - Project settings
- gradle.properties - Gradle configuration
- proguard-rules.pro - ProGuard rules for release builds

## Code Quality Features

- Type-safe navigation with Jetpack Navigation Compose
- State management with StateFlow
- Material 3 theming
- Responsive layouts
- Error handling
- Memory-efficient data retrieval
- No external libraries for system info (uses Android APIs)

## Testing the App

Once built and installed, you can:
1. Launch the app to see the home screen
2. Tap each card to view detailed information
3. Use refresh buttons on Battery and Network screens
4. Navigate back with the back button
5. Verify all information matches your device specs

## Troubleshooting

### Gradle Sync Fails
- Ensure you have internet connection
- Check that Android SDK is installed
- Verify ANDROID_HOME environment variable is set

### Build Fails
- Clean the project: `./gradlew clean`
- Invalidate caches in Android Studio
- Ensure Java 17 is being used

### Runtime Permissions
- Grant permissions when prompted
- For older devices, check Settings > Apps > DevInfo > Permissions

## Expected Output

When you run the app, you'll see:
- A polished Material 3 app with Indigo/Cyan theme
- Home screen with 6 information category cards
- Each screen showing real data from your device
- Smooth navigation between screens
- Working refresh functionality for battery and network
- Professional layout with proper spacing and typography
