# dev-info

## DevInfo - Android Device Information Application

A complete Android application built with **Kotlin** and **Jetpack Compose** that displays comprehensive device information including hardware specs, battery status, network details, display information, and camera capabilities.

### 📱 Features

- **Device Information**: Manufacturer, model, Android version, API level, security patch
- **Hardware Information**: RAM, Storage, CPU details
- **Battery Information**: Level with progress indicator, charging status, temperature, voltage, health
- **Network Information**: Connection type, network name, IP address, signal strength
- **Display Information**: Resolution, screen size, pixel density, refresh rate
- **Camera Information**: Camera count and detailed specs for each camera

### 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM with ViewModel and Repository pattern
- **Navigation**: Jetpack Navigation Compose
- **Minimum SDK**: 24
- **Target SDK**: 34

### 📁 Project Structure

```
app/
├── src/main/
│   ├── java/com/anhprgm/deviceinfo/
│   │   ├── MainActivity.kt
│   │   ├── data/
│   │   │   ├── DeviceInfoRepository.kt
│   │   │   └── models/
│   │   │       ├── DeviceInfo.kt
│   │   │       ├── HardwareInfo.kt
│   │   │       ├── BatteryInfo.kt
│   │   │       ├── NetworkInfo.kt
│   │   │       ├── DisplayInfo.kt
│   │   │       └── CameraInfo.kt
│   │   └── ui/
│   │       ├── viewmodel/
│   │       │   └── DeviceInfoViewModel.kt
│   │       ├── components/
│   │       │   └── CommonComponents.kt
│   │       ├── screens/
│   │       │   ├── HomeScreen.kt
│   │       │   ├── DeviceDetailScreen.kt
│   │       │   ├── HardwareScreen.kt
│   │       │   ├── BatteryScreen.kt
│   │       │   ├── NetworkScreen.kt
│   │       │   ├── DisplayScreen.kt
│   │       │   └── CameraScreen.kt
│   │       └── theme/
│   │           ├── Theme.kt
│   │           ├── Color.kt
│   │           └── Typography.kt
│   └── AndroidManifest.xml
```

### 🔧 Build Requirements

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later
- Android SDK 34
- Gradle 8.2

### 🚀 Getting Started

1. **Clone the repository**
   ```bash
   git clone https://github.com/anhprgm/dev-info.git
   cd dev-info
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned repository
   - Wait for Gradle sync to complete

3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Run on device or emulator**
   - Connect an Android device or start an emulator
   - Click "Run" in Android Studio or use:
   ```bash
   ./gradlew installDebug
   ```

### 📋 Permissions

The app requires the following permissions:
- `ACCESS_NETWORK_STATE` - To check network connectivity
- `ACCESS_WIFI_STATE` - To get WiFi information
- `INTERNET` - To get IP address
- `CAMERA` - To enumerate camera capabilities (no actual camera access)

### 🎨 UI/UX

- Clean Material 3 design with Indigo primary and Cyan secondary colors
- Card-based layout for organized information display
- Smooth navigation between information screens
- Pull-to-refresh functionality for battery and network screens
- Responsive design that adapts to different screen sizes

### 📱 Screenshots

The application consists of:
- **Home Screen**: Quick overview cards for all device information categories
- **Detail Screens**: Comprehensive information for each category
- **Interactive Elements**: Refresh buttons, progress indicators, and smooth transitions

### 🔍 Data Sources

The app uses native Android APIs to retrieve information:
- `Build` class for device information
- `ActivityManager` and `StatFs` for hardware info
- `BatteryManager` for battery details
- `ConnectivityManager` and `WifiManager` for network info
- `DisplayMetrics` and `WindowManager` for display specs
- `CameraManager` for camera capabilities

### 📄 License

This project is open source and available under the MIT License.

### 👤 Author

**anhprgm**

### 🤝 Contributing

Contributions, issues, and feature requests are welcome!
