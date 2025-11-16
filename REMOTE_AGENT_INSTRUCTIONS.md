# Remote Agent Instructions: Kulus Android Feature Parity Mission

## 🎯 Mission Statement

Develop the Kulus Android app (`~/StudioProjects/kulus_android`) to achieve **100% feature parity** with the iOS Hamumu app (`~/ios_code/hamumu`). You will work autonomously to analyze, design, implement, and test all missing features while maintaining the existing Android architecture and Matrix-themed design.

---

## 📚 Project Context

### Overview

**Hamumu/Kulus** is a glucose monitoring mobile application designed for Indigenous communities. The iOS version (Hamumu) is the reference implementation with full features. The Android version (Kulus Android) has the core architecture but is missing many user-facing features.

### Key Facts

- **Purpose**: Indigenous-focused diabetes management and community care
- **Backend**: Kulus Firebase (https://kulus.org) - shared by both iOS and Android
- **iOS App Name**: Hamumu Mobile (version 1.0, build 10)
- **Android App Name**: Kulus Android (version 1.0)
- **Design Theme**: Matrix-inspired neon theme (dark mode with green/cyan accents)
- **Architecture**: Offline-first with cloud sync

### Codebase Locations

**iOS Reference (Read-Only)**:
```
~/ios_code/hamumu/
├── HamamuMobile/
│   ├── Services/          # Business logic
│   │   ├── BluetoothService.swift
│   │   ├── KulusAuthService.swift
│   │   ├── KulusAPIClient.swift
│   │   ├── KulusRemoteService.swift
│   │   ├── DataService.swift
│   │   ├── OCRService.swift
│   │   ├── CameraService.swift
│   │   └── AuthenticationService.swift
│   ├── Models/            # Data models
│   │   ├── GlucoseData.swift
│   │   └── DeviceType.swift
│   ├── Views/             # UI screens
│   │   ├── DashboardView.swift
│   │   ├── OnboardingFlow.swift
│   │   ├── AddReadingView.swift
│   │   ├── HistoryView.swift
│   │   ├── SettingsView.swift
│   │   ├── WelcomeView.swift
│   │   ├── PhoneNumberView.swift
│   │   ├── SetPasswordView.swift
│   │   ├── CompletionView.swift
│   │   ├── DeviceConnectionView.swift
│   │   └── [others]
│   ├── ContentView.swift  # Main coordinator
│   └── Config.swift       # API configuration
├── CLAUDE.md              # iOS project documentation
└── README.md
```

**Android Implementation (Your Workspace)**:
```
~/StudioProjects/kulus_android/
├── app/src/main/java/org/kulus/android/
│   ├── data/
│   │   ├── api/          # ✅ Retrofit services (COMPLETE)
│   │   ├── local/        # ✅ Room database (COMPLETE)
│   │   ├── model/        # ✅ Data models (COMPLETE)
│   │   └── repository/   # ✅ Repository pattern (COMPLETE)
│   ├── di/               # ✅ Hilt DI modules (COMPLETE)
│   ├── ui/
│   │   ├── screens/      # 🚧 NEEDS WORK - Only basic screens exist
│   │   ├── components/   # 🚧 NEEDS WORK - Minimal components
│   │   ├── navigation/   # 🚧 NEEDS WORK - Basic nav only
│   │   └── theme/        # ✅ Matrix theme (COMPLETE)
│   ├── util/             # ⚠️ TODO - Utilities needed
│   ├── KulusApplication.kt
│   └── MainActivity.kt
├── CLAUDE.md              # Android project documentation
├── README.md
├── FEATURE_PARITY_PROMPT.md
└── [this file]
```

---

## 🔍 Complete iOS Feature Inventory

### 1. **Authentication & User Management**

#### Onboarding Flow (9 steps)
- ✅ iOS: Complete multi-step onboarding
- ❌ Android: Missing entirely

**iOS Onboarding Steps**:
1. **Welcome Screen** (`WelcomeView.swift`)
   - App introduction with Hamumu logo
   - "Get Started" CTA button
   - Cultural imagery and messaging

2. **Phone Number** (`PhoneNumberView.swift`)
   - Phone number input with formatting
   - Validation (10 digits)
   - Continue button

3. **Password Setup** (`SetPasswordView.swift`)
   - Password input with show/hide toggle
   - Password confirmation field
   - Strength indicator (Weak/Medium/Strong)
   - Requirements checklist:
     - Minimum 8 characters
     - At least one uppercase letter
     - At least one number
     - At least one special character

4. **Device Selection** (`DeviceTypeView.swift`)
   - Currently simplified or skipped
   - Original design: Choose Contour Next One vs Other

5. **Bluetooth Pairing** (`DeviceConnectionView.swift`)
   - Scan for nearby Contour Next One devices
   - Display discovered devices with signal strength
   - Connect to selected device
   - Connection status feedback

6. **Device Testing** (`DeviceTestingView.swift`)
   - Prompt user to take a test glucose reading
   - Validate successful Bluetooth data reception
   - Display received reading

7. **SMS Notification** (`SMSNotificationView.swift`)
   - Opt-in for SMS alerts
   - Phone number confirmation
   - SMS permission request

8. **Completion** (`CompletionView.swift`)
   - Success message
   - Summary of setup
   - "Start Monitoring" button

9. **Dashboard** (Unlocked after onboarding)
   - Main app interface

#### Authentication State Management
- ✅ iOS: Full auth state machine (loading, needsAuth, needsOnboarding, authenticated)
- 🟡 Android: Basic auth exists via TokenStore, no onboarding flow

---

### 2. **Glucose Data Management**

#### Data Input Methods
| Feature | iOS Status | Android Status | Priority |
|---------|-----------|----------------|----------|
| Manual entry | ✅ Complete | ✅ Complete | - |
| Photo-based OCR | ✅ Complete | ❌ Missing | **HIGH** |
| Bluetooth reading | ✅ Complete | ❌ Missing | **HIGH** |

#### Manual Entry Features (`AddReadingView.swift`)
- ✅ iOS Implementation:
  - Glucose value input (numeric)
  - Unit selection (mg/dL ↔ mmol/L) with segmented control
  - Optional comment/notes field
  - Snack pass toggle (suppress alerts for expected spikes)
  - Source tracking (manual/bluetooth/photo)
  - Timestamp preservation
  - Validation (20-600 mg/dL or 1.1-33.3 mmol/L)
  - Real-time unit conversion
  - Save button with loading state
  - Success/error feedback

- 🟡 Android: Basic form exists, needs UI polish and all fields

#### Photo OCR Reading (`OCRService.swift`, `CameraService.swift`)
- ✅ iOS Implementation:
  - Camera permission handling
  - Photo capture interface
  - Vision Framework OCR for text recognition
  - Intelligent glucose value extraction
  - OpenAI Vision API integration (optional verification)
  - Confidence scoring (High/Medium/Low)
  - Unit detection (mg/dL vs mmol/L)
  - Photo preview before processing
  - Retry/retake options
  - Photo storage with reading
  - Fallback to manual entry if OCR fails

- ❌ Android: Not implemented
- **Android Requirements**:
  - Use CameraX for camera interface
  - ML Kit Text Recognition for OCR
  - Retrofit integration for OpenAI API (optional)
  - Store photo URI with reading in Room

#### Bluetooth Integration (`BluetoothService.swift`)
- ✅ iOS Implementation:
  - **Protocol**: Contour Next One glucose meter
  - **Service UUID**: `00001808-0000-1000-8000-00805F9B34FB` (Glucose Service)
  - **Characteristic UUID**: `00002A18-0000-1000-8000-00805F9B34FB` (Glucose Measurement)
  - **Data Format**: IEEE-11073 SFLOAT16
  - **Features**:
    - Device scanning with signal strength
    - Connection management (connect/disconnect/reconnect)
    - Automatic notification setup
    - SFLOAT16 decoding (mantissa + exponent)
    - Sequence number tracking
    - Timestamp parsing from device
    - Device info (manufacturer, model)
    - Connection state management (disconnected, scanning, connecting, connected, failed)
    - Error handling and recovery
    - Background reconnection

- ❌ Android: Not implemented
- **Android Requirements**:
  - Use Android Bluetooth LE APIs
  - Same UUIDs and protocol as iOS
  - Handle Android BLE permissions (BLUETOOTH_SCAN, BLUETOOTH_CONNECT)
  - Foreground service for background scanning
  - GATT connection management
  - Characteristic notification handling

---

### 3. **Data Synchronization** (Kulus Firebase Backend)

#### API Integration
- ✅ iOS: Complete integration (`KulusRemoteService.swift`, `KulusAuthService.swift`, `KulusAPIClient.swift`)
- ✅ Android: Complete integration (already implemented)

**Both platforms share**:
- Base URL: https://kulus.org
- API Key: `kulus-unified-api-key-2025`
- Password: `kulus2025`
- Token-based authentication
- Endpoints:
  - `POST /validatePassword` - Get auth token
  - `POST /verifyToken` - Verify token validity
  - `GET /api/v2/getAllReadings` - Fetch all readings
  - `GET /api/v2/addReadingFromUrl` - Add new reading

#### Sync Features
| Feature | iOS | Android | Notes |
|---------|-----|---------|-------|
| Token management | ✅ | ✅ | Keychain vs DataStore |
| Upload readings | ✅ | ✅ | Complete |
| Download readings | ✅ | ✅ | Complete |
| Conflict resolution | ✅ | ✅ | Remote ID tracking |
| Offline queue | ✅ | ✅ | Local-first |
| Background sync | ✅ | ❌ | Android needs WorkManager |
| Sync UI/indicators | ✅ | ❌ | Android needs UI component |

---

### 4. **UI Screens & Navigation**

#### Main Screens

| Screen | iOS File | Android Status | Priority |
|--------|----------|----------------|----------|
| **Dashboard** | `DashboardView.swift` | 🟡 Basic exists | **CRITICAL** |
| **History/List** | `HistoryView.swift` | ✅ Complete | - |
| **Add Reading** | `AddReadingView.swift` | 🟡 Partial | **HIGH** |
| **Reading Detail** | Embedded in Dashboard | ❌ Missing | **HIGH** |
| **Settings** | `SettingsView.swift` | ❌ Missing | **HIGH** |
| **Welcome** | `WelcomeView.swift` | ❌ Missing | **MEDIUM** |
| **Phone Number** | `PhoneNumberView.swift` | ❌ Missing | **MEDIUM** |
| **Set Password** | `SetPasswordView.swift` | ❌ Missing | **MEDIUM** |
| **Completion** | `CompletionView.swift` | ❌ Missing | **MEDIUM** |
| **Device Connection** | `DeviceConnectionView.swift` | ❌ Missing | **LOW** |
| **Camera Settings** | `CameraSettingsView.swift` | ❌ Missing | **LOW** |

#### Dashboard Features (`DashboardView.swift`, `DashboardTabView.swift`)

**iOS Dashboard Tabs**:
1. **Today Tab**:
   - Latest glucose reading (large display)
   - Glucose level color coding (Green/Orange/Red/Purple)
   - Level description (Low/Normal/Elevated/High)
   - Quick add reading button
   - Recent readings list (last 5-10)
   - Time since last reading
   - Sync status indicator

2. **History Tab** (Maps to Android's ReadingsListScreen):
   - Full reading list with infinite scroll
   - Search/filter by name or date
   - Date range picker
   - Export options (CSV/JSON/PDF)
   - Sort options (newest/oldest)
   - Swipe to delete
   - Pull to refresh for sync

3. **Trends Tab** (NOT YET IN ANDROID):
   - Line chart showing glucose over time
   - Time range selector (24h/7d/30d/90d/1y)
   - Average glucose calculation
   - Time in range statistics
   - A1C estimate
   - Pattern detection (morning highs, post-meal spikes)

4. **Settings Tab**:
   - User profile
   - Default name setting
   - Unit preference (mg/dL or mmol/L)
   - Dark mode toggle (System/Light/Dark)
   - OpenAI API key input
   - Bluetooth device management
   - Data export
   - Sign out

**Android Dashboard Status**:
- ❌ No tab navigation
- ❌ No "Today" overview
- ❌ No trends/charts
- ✅ Has readings list (as main screen)
- ❌ No settings screen

#### Reading Detail Screen
- ✅ iOS: Tap on reading → full detail view with:
  - Large glucose value display
  - Color-coded level indicator
  - Timestamp (date + time)
  - Name/profile
  - Comment/notes
  - Source (bluetooth/photo/manual)
  - Photo (if captured)
  - Sync status
  - Edit/Delete buttons
  - Share option

- ❌ Android: Not implemented
- **Android Requirement**: Create `ReadingDetailScreen.kt` with Navigation Compose

---

### 5. **Data Visualization & Analytics**

#### Charts (`GlucoseData.swift` - GlucoseTrend enum)
- ✅ iOS: Line chart with SwiftUI Charts
  - Time range filters (day/week/month/3months/year)
  - Color zones (low/normal/elevated/high)
  - Average line overlay
  - Data point tooltips
  - Zoom and pan

- ❌ Android: Not implemented
- **Android Recommendation**: Use Vico (Compose chart library) or MPAndroidChart

#### Statistics
- ✅ iOS: Dashboard statistics cards:
  - Average glucose (for selected range)
  - Minimum glucose
  - Maximum glucose
  - Standard deviation
  - Time in range (% in target)
  - A1C estimate
  - Total readings count

- ❌ Android: Not implemented

---

### 6. **Settings & Preferences** (`SettingsView.swift`)

| Setting | iOS | Android | Notes |
|---------|-----|---------|-------|
| User profile name | ✅ | ❌ | Default name for readings |
| Unit preference | ✅ | ❌ | mg/dL or mmol/L |
| Dark mode toggle | ✅ | ❌ | System/Light/Dark |
| OpenAI API key | ✅ | ❌ | Optional OCR verification |
| Target glucose range | ✅ | ❌ | Custom range for charts |
| Notification preferences | ✅ | ❌ | SMS/push toggles |
| Bluetooth device pairing | ✅ | ❌ | Manage connected device |
| Data export | ✅ | ❌ | CSV/JSON/PDF export |
| Clear local data | ✅ | ❌ | Privacy feature |
| Sign out | ✅ | ❌ | Clear credentials |
| About/Version | ✅ | ❌ | App info |

---

### 7. **Data Export & Sharing** (`GlucoseData.swift` - Export features)

#### Export Formats
- ✅ iOS: CSV export
  - Headers: Date, Time, Glucose, Units, Name, Comment, Source
  - Share sheet integration
  - Date range selection

- ✅ iOS: JSON export
  - Full data structure
  - Includes metadata

- ✅ iOS: PDF report generation
  - Summary statistics
  - Chart visualization
  - Tabular data

- ❌ Android: None implemented

#### Sharing
- ✅ iOS: Share individual reading
- ✅ iOS: Share date range
- ✅ iOS: Share report
- ❌ Android: Not implemented

---

### 8. **Advanced Features**

#### Photo Storage
- ✅ iOS: Photos stored with readings
- ❌ Android: Not implemented
- **Android Requirement**: Store photo URIs in Room, handle permissions

#### Tags System
- ✅ iOS: Custom tags for readings (fasting, post-meal, exercise, etc.)
- ❌ Android: Not implemented

#### Multiple Profiles
- ✅ iOS: Support for multiple user profiles (family sharing)
- ❌ Android: Single profile only

#### Notifications
- 🟡 iOS: SMS alerts via backend
- ❌ Android: No local notifications
- **Android Requirement**: Use NotificationManager for critical glucose alerts

#### Background Sync
- ✅ iOS: Background app refresh
- ❌ Android: No WorkManager implementation

---

## 📊 Feature Gap Analysis Summary

### ✅ Features Android HAS (Parity Achieved)
1. Manual glucose entry (basic)
2. Readings list display
3. Cloud sync (upload/download)
4. Token-based authentication
5. Offline-first architecture
6. Room database storage
7. Material3 Matrix theme
8. Unit conversion (backend)

### ❌ Features Android MISSING (Implementation Required)

#### Critical (Must Have - Week 1-2)
1. **Reading Detail Screen** - Users can't view full reading details
2. **Settings Screen** - No way to configure preferences
3. **Dashboard with Tabs** - No "Today" view or trends
4. **Onboarding Flow** - No first-time user experience
5. **Background Sync** - Data only syncs when app is open

#### High Priority (Week 3-4)
6. **Photo OCR Reading** - Major iOS feature missing
7. **Charts/Analytics** - No data visualization
8. **Enhanced Add Reading Form** - Missing comment, snackPass, photo
9. **Sync UI Indicators** - Users don't know if data is syncing
10. **Data Export** - No CSV/PDF export

#### Medium Priority (Week 5-6)
11. **Onboarding Screens** - Welcome, Phone, Password, Completion
12. **Profile Management** - Multiple users
13. **Tags System** - Categorize readings
14. **Notifications** - Critical glucose alerts
15. **Search/Filter** - Advanced reading queries

#### Low Priority (Nice to Have)
16. **Bluetooth Integration** - Contour Next One pairing
17. **Share Functionality** - Share readings/reports
18. **Camera Settings** - OCR configuration
19. **Widgets** - Home screen glucose display
20. **Accessibility** - Screen reader, large text

---

## 🛠️ Implementation Roadmap

### Phase 1: Core Missing Features (Days 1-7)

#### Day 1-2: Reading Detail Screen
**File**: `app/src/main/java/org/kulus/android/ui/screens/ReadingDetailScreen.kt`

**Requirements**:
- Navigation from ReadingsListScreen
- Large glucose value display
- Color-coded level indicator
- All reading fields (name, timestamp, comment, source, sync status)
- Edit/Delete buttons (delete from local Room only)
- Back navigation

**iOS Reference**: `DashboardView.swift` (reading detail section)

**Code Structure**:
```kotlin
@Composable
fun ReadingDetailScreen(
    readingId: String,
    viewModel: ReadingDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onDeleteClick: () -> Unit
)

@HiltViewModel
class ReadingDetailViewModel @Inject constructor(
    private val repository: KulusRepository,
    savedStateHandle: SavedStateHandle
)
```

---

#### Day 3-4: Settings Screen
**File**: `app/src/main/java/org/kulus/android/ui/screens/SettingsScreen.kt`

**Requirements**:
- User preferences with DataStore
- Default name input
- Unit preference toggle (mg/dL ↔ mmol/L)
- Dark mode selector (System/Light/Dark)
- OpenAI API key input (optional)
- Data export button
- Sign out button (clear tokens)
- About section with version info

**iOS Reference**: `SettingsView.swift`

**DataStore Setup**:
```kotlin
// app/src/main/java/org/kulus/android/data/preferences/UserPreferences.kt
data class UserPreferences(
    val defaultName: String = "mobile-user",
    val preferredUnit: GlucoseUnit = GlucoseUnit.MMOL_L,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val openAiApiKey: String? = null,
    val targetRangeLow: Double = 3.9,
    val targetRangeHigh: Double = 7.8
)

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}
```

---

#### Day 5-6: Dashboard with Tabs
**File**: `app/src/main/java/org/kulus/android/ui/screens/DashboardScreen.kt`

**Requirements**:
- Bottom navigation with 3-4 tabs:
  1. **Today** - Latest reading + recent list
  2. **History** - Full list (existing ReadingsListScreen)
  3. **Trends** - Placeholder for charts (Phase 2)
  4. **Settings** - Settings screen

**iOS Reference**: `DashboardTabView.swift`

**Code Structure**:
```kotlin
@Composable
fun DashboardScreen() {
    var selectedTab by remember { mutableStateOf(DashboardTab.TODAY) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                DashboardTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, tab.title) },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            DashboardTab.TODAY -> TodayScreen(Modifier.padding(padding))
            DashboardTab.HISTORY -> ReadingsListScreen(Modifier.padding(padding))
            DashboardTab.TRENDS -> TrendsScreen(Modifier.padding(padding))
            DashboardTab.SETTINGS -> SettingsScreen(Modifier.padding(padding))
        }
    }
}

enum class DashboardTab(val title: String, val icon: ImageVector) {
    TODAY("Today", Icons.Default.Home),
    HISTORY("History", Icons.Default.List),
    TRENDS("Trends", Icons.Default.ShowChart),
    SETTINGS("Settings", Icons.Default.Settings)
}
```

---

#### Day 7: Background Sync with WorkManager
**File**: `app/src/main/java/org/kulus/android/worker/SyncWorker.kt`

**Requirements**:
- Periodic sync every 15-60 minutes
- Sync on network connectivity change
- Exponential backoff on failure
- Notification on sync errors (optional)

**Code**:
```kotlin
class SyncWorker @Inject constructor(
    context: Context,
    params: WorkerParameters,
    private val repository: KulusRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            repository.syncReadingsFromServer()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}

// In KulusApplication.kt
WorkManager.getInstance(this).enqueueUniquePeriodicWork(
    "kulus_sync",
    ExistingPeriodicWorkPolicy.KEEP,
    PeriodicWorkRequestBuilder<SyncWorker>(30, TimeUnit.MINUTES)
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
        .build()
)
```

---

### Phase 2: High-Value Features (Days 8-21)

#### Day 8-11: Photo OCR Reading
**Files**:
- `app/src/main/java/org/kulus/android/ui/screens/CameraScreen.kt`
- `app/src/main/java/org/kulus/android/service/OCRService.kt`
- `app/src/main/java/org/kulus/android/service/CameraService.kt`

**Requirements**:
- CameraX integration for photo capture
- ML Kit Text Recognition for OCR
- Glucose value extraction from OCR text
- Unit detection (mg/dL vs mmol/L)
- Confidence scoring
- Photo preview before processing
- OpenAI Vision API integration (optional)
- Save photo URI with reading

**iOS Reference**: `OCRService.swift`, `CameraService.swift`

**Dependencies**:
```kotlin
// build.gradle.kts
implementation("androidx.camera:camera-camera2:1.3.0")
implementation("androidx.camera:camera-lifecycle:1.3.0")
implementation("androidx.camera:camera-view:1.3.0")
implementation("com.google.mlkit:text-recognition:16.0.0")
```

**OCR Service**:
```kotlin
class OCRService @Inject constructor(
    private val textRecognizer: TextRecognizer
) {
    suspend fun extractGlucoseValue(imageUri: Uri): OCRResult {
        val inputImage = InputImage.fromFilePath(context, imageUri)
        val text = textRecognizer.process(inputImage).await()

        // Extract glucose value using regex
        val glucosePattern = Regex("""(\d+\.?\d*)\s*(mg/dL|mmol/L)?""")
        val matches = glucosePattern.findAll(text.text)

        return matches.firstOrNull()?.let { match ->
            val value = match.groupValues[1].toDoubleOrNull() ?: return@let null
            val unit = match.groupValues[2].ifEmpty { detectUnit(value) }
            OCRResult.Success(value, unit, confidence = calculateConfidence(match))
        } ?: OCRResult.Failure("No glucose value found")
    }

    private fun detectUnit(value: Double): String {
        // mg/dL range: 20-600, mmol/L range: 1.1-33.3
        return if (value > 33.3) "mg/dL" else "mmol/L"
    }
}

sealed class OCRResult {
    data class Success(val value: Double, val unit: String, val confidence: Float) : OCRResult()
    data class Failure(val error: String) : OCRResult()
}
```

---

#### Day 12-16: Charts & Analytics
**Files**:
- `app/src/main/java/org/kulus/android/ui/screens/TrendsScreen.kt`
- `app/src/main/java/org/kulus/android/ui/components/GlucoseChart.kt`

**Requirements**:
- Line chart showing glucose over time
- Time range selector (24h, 7d, 30d, 90d, 1y)
- Color zones (low/normal/high)
- Statistics cards (avg, min, max, std dev)
- Time in range calculation
- A1C estimate

**Library**: Use Vico (https://github.com/patrykandpatrick/vico)

**Dependencies**:
```kotlin
implementation("com.patrykandpatrick.vico:compose:1.13.1")
implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")
```

**iOS Reference**: `GlucoseData.swift` (GlucoseTrend, TimeRange enums)

---

#### Day 17-18: Enhanced Add Reading Form
**File**: Update `AddReadingScreen.kt`

**Add Missing Fields**:
- Comment/notes text field
- Snack pass toggle with explanation
- Photo capture button (launches CameraScreen)
- Photo preview (if captured)
- Source selector (Manual/Photo/Bluetooth - future)
- Validation with error messages
- Save state across configuration changes

**iOS Reference**: `AddReadingView.swift`

---

#### Day 19-20: Sync UI Indicators
**Files**:
- `app/src/main/java/org/kulus/android/ui/components/SyncIndicator.kt`
- Update `ReadingsListScreen.kt`, `DashboardScreen.kt`

**Requirements**:
- Sync status badge (synced/pending/failed)
- Last sync time display
- Sync progress indicator
- Pull-to-refresh to trigger sync
- Error messages with retry
- Network connectivity awareness

---

#### Day 21: Data Export
**File**: `app/src/main/java/org/kulus/android/service/ExportService.kt`

**Requirements**:
- CSV export with date range
- JSON export
- PDF generation (using iText or similar)
- Share intent integration
- File provider for sharing

**iOS Reference**: `GlucoseData.swift` (export functions)

---

### Phase 3: Onboarding & Polish (Days 22-35)

#### Day 22-26: Onboarding Flow
**Files**:
- `OnboardingCoordinator.kt`
- `WelcomeScreen.kt`
- `PhoneNumberScreen.kt`
- `PasswordScreen.kt`
- `CompletionScreen.kt`

**Requirements**:
- Multi-step flow with progress indicator
- State persistence (in case of app kill)
- Password strength validator
- Phone number formatter
- Completion animation
- Navigation to dashboard

**iOS Reference**: All onboarding view files

---

#### Day 27-30: Notifications & Alerts
**File**: `app/src/main/java/org/kulus/android/service/NotificationService.kt`

**Requirements**:
- Critical glucose level alerts (local)
- Customizable thresholds
- Notification channels
- Alert sound/vibration
- Do Not Disturb integration

---

#### Day 31-35: Profile Management & Tags
**Features**:
- Multiple user profiles
- Profile switcher in settings
- Custom tags for readings
- Tag management UI
- Filter by tags

---

### Phase 4: Advanced Features (Optional - Days 36+)

#### Bluetooth Integration
**Complexity**: High (8-12 days)
**iOS Reference**: `BluetoothService.swift`

#### Widgets
**Complexity**: Medium (4-6 days)

#### Accessibility
**Complexity**: Medium (4-6 days)

---

## 📋 Development Guidelines

### Android Architecture Requirements

#### Follow Existing Patterns
- **Dependency Injection**: Use Hilt throughout
- **Async Operations**: Kotlin Coroutines + Flow
- **Database**: Room with Flow-based DAOs
- **Network**: Retrofit with KulusApiService
- **State Management**: ViewModel + StateFlow
- **UI**: Jetpack Compose + Material3
- **Navigation**: Navigation Compose

#### Theme Consistency
Use the existing Matrix theme:
```kotlin
KulusTheme {
    // Your composables
}

// Colors available:
MaterialTheme.colorScheme.primary       // MatrixNeon (#00FFB8)
MaterialTheme.colorScheme.secondary     // MatrixAmber (#FFC833)
MaterialTheme.colorScheme.background    // MatrixBackground (#020B09)
MaterialTheme.colorScheme.surface       // MatrixPanelStart (#031413)

// Glucose level colors:
GlucoseGreen, GlucoseOrange, GlucoseRed, GlucosePurple
```

#### Repository Usage
Always use `KulusRepository` for data operations:
```kotlin
// In ViewModel
class MyViewModel @Inject constructor(
    private val repository: KulusRepository
) : ViewModel() {

    val readings = repository.getAllReadingsLocal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addReading(value: Double, name: String) {
        viewModelScope.launch {
            repository.addReading(
                reading = value,
                name = name,
                units = GlucoseUnit.MMOL_L,
                comment = null,
                snackPass = false
            ).onSuccess {
                // Handle success
            }.onFailure { error ->
                // Handle error
            }
        }
    }
}
```

---

### Testing Requirements

#### For Each Feature
1. **Unit Tests** (if applicable):
   - ViewModels
   - Repository logic
   - Parsers (OCR, Bluetooth)

2. **Integration Tests**:
   - Room database operations
   - Network calls (mocked)

3. **Manual Testing**:
   - Test on real device (not just emulator)
   - Test offline mode
   - Test poor network conditions
   - Test with various glucose values
   - Test edge cases (very low/high values, empty states)

#### Test Checklist per Feature
- [ ] Happy path works
- [ ] Error handling works
- [ ] Loading states display correctly
- [ ] Empty states handled
- [ ] Offline mode works
- [ ] Network errors handled gracefully
- [ ] UI matches iOS design intent
- [ ] Navigation works correctly
- [ ] Rotation preserves state
- [ ] Theme colors applied correctly

---

### Build & Deployment

#### Build Commands
```bash
# Clean build
./gradlew clean assembleDebug

# Install on device
./gradlew installDebug

# Or use provided script
./build.sh
```

#### Code Style
- Follow Kotlin coding conventions
- Use Compose best practices
- Add KDoc comments for public APIs
- Keep composables small and focused
- Extract reusable components

---

## 📝 Documentation Requirements

### For Each Implemented Feature

Create a feature completion report:

**File**: `docs/features/[FEATURE_NAME].md`

**Template**:
```markdown
# Feature: [Name]

## Status
- ✅ Implementation Complete
- ✅ Unit Tests Passed
- ✅ Manual Testing Complete
- ✅ iOS Parity Verified

## Implementation Details

### iOS Reference
- File: `~/ios_code/hamumu/HamamuMobile/[File].swift`
- Key implementation: [brief description]

### Android Implementation
- Files:
  - `app/src/main/java/org/kulus/android/ui/screens/[Screen].kt`
  - `app/src/main/java/org/kulus/android/ui/components/[Component].kt`
- ViewModel: `[ViewModel].kt`
- Architecture notes: [any specific decisions]

### Differences from iOS
- [List any intentional differences and why]
- [Or state "Matches iOS behavior exactly"]

### Testing Notes
- Tested on: [Device/Emulator]
- Edge cases tested: [list]
- Known issues: [if any]

### Screenshots
[Before/After or key screens]
```

---

### Update Main Documentation

After completing each phase:

1. **Update CLAUDE.md**:
   - Move features from "🚧 Next Steps" to "✅ Completed"
   - Add new implementation notes

2. **Update README.md**:
   - Update feature list
   - Update build status
   - Add screenshots

3. **Update FEATURE_PARITY_PROMPT.md**:
   - Check off completed features
   - Update gap analysis

---

## ✅ Completion Checklist

### Phase 1 Complete When:
- [ ] Reading Detail screen implemented and working
- [ ] Settings screen with all preferences
- [ ] Dashboard with tab navigation
- [ ] Background sync with WorkManager
- [ ] All screens follow Material3 + Matrix theme
- [ ] Navigation updated for all new screens
- [ ] Documentation updated

### Phase 2 Complete When:
- [ ] Photo OCR fully functional
- [ ] Charts displaying glucose trends
- [ ] Enhanced add reading form with all fields
- [ ] Sync indicators visible and accurate
- [ ] Data export (CSV/JSON) working
- [ ] All high-priority features tested

### Phase 3 Complete When:
- [ ] Complete onboarding flow
- [ ] Notifications for critical levels
- [ ] Profile management
- [ ] Tags system
- [ ] All medium-priority features tested

### Phase 4 Complete When:
- [ ] Bluetooth integration (if implemented)
- [ ] Widgets (if implemented)
- [ ] Accessibility features
- [ ] All optional features documented

### 100% Parity Achieved When:
- [ ] All iOS features implemented or documented as "not applicable"
- [ ] Feature matrix shows complete parity
- [ ] App builds without errors
- [ ] All tests passing
- [ ] Documentation complete
- [ ] Manual testing on real device successful
- [ ] User experience matches iOS quality
- [ ] Performance acceptable (no lag, crashes)

---

## 🚀 Getting Started as Remote Agent

### Initial Setup (First Session)

1. **Read All Documentation**:
   ```bash
   cd ~/StudioProjects/kulus_android
   cat README.md
   cat CLAUDE.md
   cat FEATURE_PARITY_PROMPT.md
   cat REMOTE_AGENT_INSTRUCTIONS.md  # This file
   ```

2. **Review iOS Codebase** (Read-Only):
   ```bash
   cd ~/ios_code/hamumu
   cat CLAUDE.md
   cat README.md

   # Read key services
   cat HamamuMobile/Services/BluetoothService.swift
   cat HamamuMobile/Services/KulusRemoteService.swift
   cat HamamuMobile/Services/OCRService.swift

   # Read key views
   cat HamamuMobile/Views/DashboardView.swift
   cat HamamuMobile/Views/SettingsView.swift
   cat HamamuMobile/Views/AddReadingView.swift
   ```

3. **Verify Android Build**:
   ```bash
   cd ~/StudioProjects/kulus_android
   ./build.sh
   # Should build successfully and generate APK
   ```

4. **Create Your Roadmap**:
   - Review this document's implementation roadmap
   - Adjust timeline based on your assessment
   - Document your plan in `MY_ROADMAP.md`

### Development Workflow

#### Start of Each Session
1. Pull latest changes: `git pull origin main`
2. Review previous session's progress
3. Pick next feature from roadmap
4. Read relevant iOS implementation
5. Design Android equivalent

#### During Development
1. Implement feature following architecture patterns
2. Test frequently: `./gradlew installDebug`
3. Commit often: `git commit -m "Implement [feature]: [description]"`
4. Update documentation as you go

#### End of Each Session
1. Final build: `./build.sh`
2. Test on device
3. Update feature documentation
4. Update CLAUDE.md
5. Push: `git push origin main`
6. Write session summary in `PROGRESS.md`

### Communication Protocol

#### If You Have Questions
1. Check this document first
2. Check CLAUDE.md
3. Check iOS implementation
4. Check existing Android code for patterns
5. Document the blocker in `BLOCKERS.md`

#### Regular Updates
Create `PROGRESS.md` with weekly updates:
```markdown
# Progress Report - Week [N]

## Completed This Week
- [Feature 1] - [Brief description]
- [Feature 2] - [Brief description]

## Currently Working On
- [Feature X] - [Status]

## Blockers
- [Blocker 1] - [Details]

## Next Week Plan
- [ ] Feature A
- [ ] Feature B
```

---

## 📞 Support & Resources

### Key Documentation
- **Android Project**: `~/StudioProjects/kulus_android/CLAUDE.md`
- **iOS Reference**: `~/ios_code/hamumu/CLAUDE.md`
- **This Guide**: `~/StudioProjects/kulus_android/REMOTE_AGENT_INSTRUCTIONS.md`
- **API Docs**: `~/WebstormProjects/Kulus-App/README.md` (Kulus backend)

### Useful Links
- Jetpack Compose: https://developer.android.com/jetpack/compose
- Material3: https://m3.material.io/
- Hilt: https://dagger.dev/hilt/
- Room: https://developer.android.com/training/data-storage/room
- CameraX: https://developer.android.com/training/camerax
- ML Kit: https://developers.google.com/ml-kit/vision/text-recognition
- Vico Charts: https://github.com/patrykandpatrick/vico

### Android vs iOS Equivalents
| iOS | Android |
|-----|---------|
| SwiftUI | Jetpack Compose |
| Combine | Kotlin Flow |
| Core Data | Room |
| URLSession | Retrofit + OkHttp |
| Keychain | DataStore (encrypted) |
| UserDefaults | DataStore (preferences) |
| Core Bluetooth | Bluetooth LE API |
| Vision Framework | ML Kit |

---

## 🎯 Success Criteria

You will know you've succeeded when:

1. **Feature Parity**: Every iOS feature has Android equivalent (or documented as future work)
2. **Quality**: App feels professional, no crashes, smooth performance
3. **Architecture**: Code follows established patterns, maintainable
4. **Testing**: All features manually tested and working
5. **Documentation**: Complete and accurate
6. **User Experience**: Matches iOS in functionality and polish

---

## 📄 License & Credits

- **Project**: Kulus/Hamumu - Indigenous Health Technology Initiative
- **iOS Implementation**: Joseph Malone (original)
- **Android Implementation**: [Your name/handle]
- **License**: Copyright 2025 - See project LICENSE

---

**Last Updated**: November 2025
**Version**: 1.0
**Status**: Active Development
**Contact**: See project repository for issues/PRs

---

## Quick Reference Card

### Essential Commands
```bash
# Build
./build.sh

# Install
./gradlew installDebug

# Test
./gradlew test

# Clean
./gradlew clean
```

### Essential Files
```
Android:
- CLAUDE.md (architecture guide)
- data/repository/KulusRepository.kt (data layer)
- ui/theme/ (Matrix theme)
- MainActivity.kt (entry point)

iOS:
- CLAUDE.md (feature reference)
- Services/ (business logic examples)
- Views/ (UI examples)
- Models/ (data model examples)
```

### Priority Order
1. Reading Detail Screen
2. Settings Screen
3. Dashboard Tabs
4. Background Sync
5. Photo OCR
6. Charts
7. Onboarding
8. Everything else

**BEGIN WITH PHASE 1, DAY 1: READING DETAIL SCREEN**

Good luck! 🚀
