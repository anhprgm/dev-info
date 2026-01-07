# DevInfo Android App - Architecture & Code Structure

## Application Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         MainActivity                         │
│                     (Entry Point + Theme)                    │
└────────────────────────┬────────────────────────────────────┘
                         │
                         │ setContent
                         │
┌────────────────────────▼────────────────────────────────────┐
│                      Navigation Setup                        │
│              (NavHost + 7 Composable Routes)                 │
└──────┬─────────┬─────────┬─────────┬─────────┬─────────┬───┘
       │         │         │         │         │         │
       │         │         │         │         │         │
┌──────▼────┐ ┌─▼───┐ ┌───▼────┐ ┌──▼────┐ ┌─▼────┐ ┌─▼──────┐
│   Home    │ │Dev. │ │Hardware│ │Battery│ │Network│ │Display │
│  Screen   │ │Info │ │ Screen │ │Screen │ │Screen │ │Screen  │
└───────────┘ └─────┘ └────────┘ └───────┘ └───────┘ └────────┘
       │         │         │         │         │         │
       └─────────┴─────────┴─────────┴─────────┴─────────┘
                         │
                    shared reference
                         │
┌────────────────────────▼────────────────────────────────────┐
│                   DeviceInfoViewModel                        │
│                  (State Management Layer)                    │
│                                                              │
│  StateFlows:                                                 │
│  - deviceInfo: StateFlow<DeviceInfo?>                        │
│  - hardwareInfo: StateFlow<HardwareInfo?>                    │
│  - batteryInfo: StateFlow<BatteryInfo?>                      │
│  - networkInfo: StateFlow<NetworkInfo?>                      │
│  - displayInfo: StateFlow<DisplayInfo?>                      │
│  - cameraInfo: StateFlow<CameraInfo?>                        │
│                                                              │
│  Functions:                                                  │
│  - loadAllInfo()                                             │
│  - refreshBatteryInfo()                                      │
│  - refreshNetworkInfo()                                      │
└────────────────────────┬────────────────────────────────────┘
                         │
                    calls repository
                         │
┌────────────────────────▼────────────────────────────────────┐
│                  DeviceInfoRepository                        │
│                   (Data Access Layer)                        │
│                                                              │
│  Functions:                                                  │
│  - getDeviceInfo() → DeviceInfo                             │
│  - getHardwareInfo() → HardwareInfo                          │
│  - getBatteryInfo() → BatteryInfo                            │
│  - getNetworkInfo() → NetworkInfo                            │
│  - getDisplayInfo() → DisplayInfo                            │
│  - getCameraInfo() → CameraInfo                              │
└────────────────────────┬────────────────────────────────────┘
                         │
                   uses Android APIs
                         │
┌────────────────────────▼────────────────────────────────────┐
│                    Android System APIs                       │
│                                                              │
│  - Build (device info)                                       │
│  - ActivityManager (RAM)                                     │
│  - StatFs (storage)                                          │
│  - BatteryManager (battery)                                  │
│  - ConnectivityManager & WifiManager (network)               │
│  - WindowManager & DisplayMetrics (display)                  │
│  - CameraManager (camera)                                    │
└─────────────────────────────────────────────────────────────┘
```

## Data Flow

```
User Action → Screen → ViewModel → Repository → Android APIs
                ↑                       ↓
                └───────← StateFlow ←───┘
```

## File Organization

### 1. Entry Point & Setup
**MainActivity.kt**
- Extends ComponentActivity
- Sets up theme (DevInfoTheme)
- Creates navigation controller
- Initializes ViewModel with Repository
- Defines navigation graph

### 2. Data Layer

**data/models/**
- `DeviceInfo.kt` - Device basic information model
- `HardwareInfo.kt` - RAM, Storage, CPU information model
- `BatteryInfo.kt` - Battery status and specs model
- `NetworkInfo.kt` - Network connectivity model
- `DisplayInfo.kt` - Display specifications model
- `CameraInfo.kt` - Camera capabilities model

**data/DeviceInfoRepository.kt**
- Single source of truth for device data
- Methods to fetch each type of information
- Uses Android system services
- Handles data formatting and error cases

### 3. UI Layer

**ui/viewmodel/DeviceInfoViewModel.kt**
- Manages UI state using StateFlow
- Provides reactive data to screens
- Handles user actions (refresh)
- Coordinates with repository

**ui/screens/**
- `HomeScreen.kt` - Landing page with overview cards
- `DeviceDetailScreen.kt` - Device information details
- `HardwareScreen.kt` - Hardware specifications
- `BatteryScreen.kt` - Battery status with progress bar
- `NetworkScreen.kt` - Network connectivity details
- `DisplayScreen.kt` - Display specifications
- `CameraScreen.kt` - Camera capabilities list

**ui/components/CommonComponents.kt**
- `InfoCard` - Card container for information sections
- `InfoRow` - Row displaying label-value pairs
- `DetailRow` - Column layout for detailed information
- `LoadingState` - Loading indicator component

**ui/theme/**
- `Theme.kt` - Material 3 theme configuration
- `Color.kt` - Color definitions (Indigo, Cyan, etc.)
- `Typography.kt` - Text styles and fonts

### 4. Resources

**res/values/strings.xml**
- All user-facing strings
- Navigation labels
- Information labels
- Status messages

**res/values/themes.xml**
- Base theme configuration
- Links to Material theming

**res/mipmap-**
- Launcher icons for all densities
- Adaptive icons for Android 8.0+

**res/xml/**
- Backup rules
- Data extraction rules

**AndroidManifest.xml**
- Application configuration
- Permissions declarations
- Activity registration
- Icon and theme references

## Key Design Patterns

### 1. MVVM (Model-View-ViewModel)
- **Model**: Data classes in `data/models/`
- **View**: Composable functions in `ui/screens/`
- **ViewModel**: `DeviceInfoViewModel` managing state

### 2. Repository Pattern
- `DeviceInfoRepository` abstracts data source
- Provides clean API for ViewModel
- Handles Android API complexity

### 3. Unidirectional Data Flow
- User interactions → ViewModel
- ViewModel → Repository → Data
- Data → StateFlow → UI updates

### 4. Composition over Inheritance
- Reusable UI components
- Composable functions
- No deep inheritance hierarchies

### 5. Single Responsibility
- Each screen shows one type of information
- Each repository method fetches one type of data
- Each model represents one concept

## State Management

### StateFlow Pattern
```kotlin
private val _deviceInfo = MutableStateFlow<DeviceInfo?>(null)
val deviceInfo: StateFlow<DeviceInfo?> = _deviceInfo.asStateFlow()
```

Benefits:
- Reactive UI updates
- Type-safe data flow
- Lifecycle-aware
- Thread-safe

### Screen State Collection
```kotlin
val deviceInfo by viewModel.deviceInfo.collectAsState()
```

Benefits:
- Automatic UI updates
- Compose integration
- Memory efficient

## Navigation Structure

```
Home Screen (/)
├── Device Details (/device)
├── Hardware (/hardware)
├── Battery (/battery)
├── Network (/network)
├── Display (/display)
└── Camera (/camera)
```

All detail screens have:
- Back navigation
- TopAppBar with title
- Scrollable content
- Consistent layout

## Material 3 Theming

### Color Scheme
- **Primary**: Indigo (#3F51B5)
- **Secondary**: Cyan (#00BCD4)
- **Surface**: Light/Dark based on theme
- **Background**: Light/Dark based on theme

### Components Used
- `Card` - Information containers
- `TopAppBar` - Screen headers
- `IconButton` - Navigation and actions
- `LinearProgressIndicator` - Battery level
- `Text` - All text content
- `Icon` - Visual indicators

### Typography
- `titleLarge` - Screen titles
- `titleMedium` - Card headers
- `bodyLarge` - Primary content
- `bodyMedium` - Secondary content
- `bodySmall` - Labels

## Dependencies

### Core Android
- `androidx.core:core-ktx` - Kotlin extensions
- `androidx.lifecycle:lifecycle-runtime-ktx` - Lifecycle
- `androidx.activity:activity-compose` - Compose support

### Jetpack Compose
- `compose-bom` - Bill of Materials for version management
- `androidx.compose.ui` - Core UI
- `androidx.compose.material3` - Material 3 components

### Navigation
- `androidx.navigation:navigation-compose` - Navigation

### ViewModel
- `androidx.lifecycle:lifecycle-viewmodel-compose` - ViewModel
- `androidx.lifecycle:lifecycle-runtime-compose` - Runtime

## Code Quality Features

### Type Safety
- Strong typing throughout
- No raw types
- Null safety with Kotlin

### Error Handling
- Try-catch blocks for system calls
- Default values for failures
- Safe nullable handling

### Performance
- Efficient data retrieval
- No unnecessary allocations
- Lazy loading where appropriate

### Maintainability
- Clear separation of concerns
- Consistent naming conventions
- Well-structured packages
- Comprehensive documentation

## Testing Strategy

### Unit Tests (Future)
- Repository methods
- ViewModel state changes
- Data model validation

### UI Tests (Future)
- Screen rendering
- Navigation flow
- User interactions

### Integration Tests (Future)
- End-to-end workflows
- Data flow validation
