# CLAUDE.md - Kulus Android

This file provides context for Claude Code agents working on this repository.

## Kulus Ecosystem Membership

This project is part of the **Kulus glucose monitoring ecosystem**. The flagship project is **Hamumu iOS** (`~/ios_code/hamumu/`). See the [Ecosystem Doc](~/ios_code/hamumu/ECOSYSTEM.md) for full inventory, API contract, shared constants, and development priorities.

| Project | Path | Role |
|---------|------|------|
| **Hamumu iOS** (flagship) | `~/ios_code/hamumu/` | iOS app, Bluetooth, Swift Packages |
| **kulus_android** (this repo) | `~/StudioProjects/kulus_android/` | Android app (Compose, Hilt, Room) |
| KulusCLI | `~/IdeaProjects/KulusCLI/` | Kotlin CLI client |
| kulus-azure.ca | `~/WebstormProjects/kulus-azure.ca/` | Azure backend (production) |
| kulus | `~/WebstormProjects/kulus/` | Firebase backend (original) |
| Kulus-App | `~/WebstormProjects/Kulus-App/` | Firebase web app |
| Kulus-WebApp | `~/WebstormProjects/Kulus-WebApp/` | Vanilla JS web app (Nodin) |
| Kulus_website | `~/WebstormProjects/Kulus_website/` | Marketing landing page |
| Kulus-V2 | `~/WebstormProjects/Kulus-V2/` | Archived legacy |

**This project's role:** Android glucose monitoring client with v3 API integration, photo OCR, charts, onboarding, notifications, and tags.

## 🚀 First-Time Setup for AI Agents

**IMPORTANT**: Before doing anything else, check if setup is complete:

```bash
test -f .setup_complete && cat .setup_complete
```

If the file doesn't exist, run the idempotent setup script:

```bash
./setup.sh
```

This script is **safe to run multiple times**. If already set up, it will just report the current configuration. See `SETUP.md` for details.

## Project Overview

**Kulus Android** is a glucose monitoring mobile application that integrates with the Kulus Firebase backend (https://kulus.org).

### UI Design (January 2026)
The UI has been redesigned to match the iOS Hamumu app for cross-platform consistency:
- **iOS-adaptive theme** - Light/dark mode support matching iOS system colors
- **Dashboard with greeting** - Time-based greeting ("Good Morning"), Kulus logo, sync status
- **iOS-style cards** - 16dp rounded corners, proper shadows, capsule badges
- **Latest Reading card** - Large glucose value with level capsule badge (Normal/Elevated/High/Low)
- **Quick Actions grid** - 2-column grid matching iOS QuickActionsGrid
- **Recent Readings** - Card with "View All" button and compact reading rows
- **History with stats** - Period selector chips, statistics 2x2 grid, time-in-range bars
- **Level icons** - CheckCircle (Normal), Warning (Elevated), Error (High), ArrowDownward (Low)

## Current Status

### ✅ Completed (November 2025)

#### Build System & Configuration
- [x] Modern Android project structure (API 26-34)
- [x] Gradle build configuration with Kotlin DSL
- [x] Gradle wrapper configured (Gradle 8.2)
- [x] JDK 17 configured for builds
- [x] Successfully builds debug APK (15MB)
- [x] App launcher icons created (Matrix theme)

#### Core Architecture
- [x] Hilt dependency injection setup
- [x] Room database for local storage
- [x] Retrofit + OkHttp for networking
- [x] Repository pattern implementation
- [x] MVVM architecture foundation

#### Data Layer
- [x] `GlucoseReading` entity with Room
- [x] `GlucoseReadingDao` with Flow support
- [x] `KulusApiService` with all endpoints
- [x] `AuthInterceptor` for automatic token injection
- [x] `TokenStore` with DataStore for persistence
- [x] `KulusRepository` with offline-first logic

#### Theme & Design
- [x] iOS-adaptive color scheme (light/dark mode)
- [x] Material3 theme matching iOS system colors
- [x] Typography system
- [x] WCAG AA compliant glucose level colors (Normal/Elevated/High/Low)
- [x] iOS-style card design with 16dp rounded corners

#### UI Screens (Phase 1 - COMPLETED)
- [x] `ReadingsListScreen` - Full list with pull-to-refresh
- [x] `AddReadingScreen` - Manual glucose entry form
- [x] `ReadingDetailScreen` - Detailed view with edit/delete
- [x] `SettingsScreen` - User preferences and configuration
- [x] `DashboardScreen` - Main screen with tab navigation
- [x] `TodayScreen` - Latest reading overview
- [x] `TrendsScreen` - Placeholder for Phase 2 charts

#### User Preferences (Phase 1 - COMPLETED)
- [x] `UserPreferences` data class
- [x] `PreferencesRepository` with DataStore
- [x] Default name configuration
- [x] Preferred unit setting (mmol/L or mg/dL)
- [x] Theme mode selector (System/Light/Dark)
- [x] OpenAI API key storage (optional)
- [x] Clear data and sign out functionality

#### Background Sync (Phase 1 - COMPLETED)
- [x] `SyncWorker` with WorkManager
- [x] Periodic sync every 30 minutes
- [x] Network-aware sync (only when connected)
- [x] Exponential backoff on failure
- [x] Hilt integration for DI

#### Navigation (Phase 1 - COMPLETED)
- [x] Navigation Compose setup
- [x] Bottom tab navigation (Today/History/Trends/Settings)
- [x] Screen routes: dashboard, add_reading, reading_detail
- [x] Proper back stack management

#### High-Value Features (Phase 2 - COMPLETED)
- [x] **Photo OCR Reading** - Complete camera and OCR functionality
  - CameraX integration for photo capture
  - ML Kit Text Recognition with 3 extraction strategies
  - Automatic glucose value extraction (explicit unit, labeled, standalone)
  - Unit detection (mg/dL vs mmol/L)
  - Confidence scoring and validation
  - Photo URI storage in GlucoseReading entity
- [x] **Charts & Analytics** - Full TrendsScreen implementation
  - Vico line chart showing glucose over time
  - Time range selector (24h, 7d, 30d, 90d, 1y) with FilterChips
  - Statistics cards (avg, min, max, std dev, CV, A1C)
  - Time in range analysis with color-coded progress bars
  - GlucoseStatistics calculator with comprehensive metrics
- [x] **Enhanced Add Reading Form** - Photo integration and validation
  - Photo preview with Coil image loading
  - Remove photo functionality
  - Source selector UI (Manual/Photo) with auto-detection
  - Comprehensive validation (number format, value ranges, unusual values)
  - Better error messages with specific guidance
  - Database schema v2 with photoUri field and migration
- [x] **Sync UI Indicators** - Visual sync status and feedback
  - Last sync timestamp with relative time (just now, Xm ago, Xh ago)
  - Unsynced readings count display
  - SyncStatusBar with visual distinction for pending syncs
  - Manual sync button
  - Success/error snackbar notifications
  - Pull-to-refresh improvements
- [x] **Data Export** - Multiple export formats with sharing
  - CSV export with proper escaping and headers
  - JSON export with structured metadata
  - Text report export with statistics and time in range
  - Android share intent integration
  - Auto-cleanup of old exports (keep last 5)
  - Export UI in Settings Data Management section

### ✅ Phase 3 Features (COMPLETED - November 2025)

#### Onboarding Flow - COMPLETE
- [x] **Complete 6-Screen Onboarding Experience**
  - WelcomeScreen - App introduction and feature overview
  - PhoneNumberScreen - Optional SMS alert setup
  - ProfileNameScreen - Display name configuration
  - DeviceSelectionScreen - Meter type selection (Contour Next One)
  - NotificationPreferencesScreen - Alert preferences and Snack Pass explanation
  - CompletionScreen - Summary and next steps
  - OnboardingViewModel with state management
  - OnboardingNav navigation graph
  - First-run detection and routing in MainActivity
  - DeviceType enum for meter selection
  - Persistent onboarding completion tracking

#### Notification System - COMPLETE
- [x] **Local Glucose Alerts**
  - NotificationService with critical level detection
  - Critical high threshold (>13.9 mmol/L / ~250 mg/dL)
  - Critical low threshold (<3.0 mmol/L / ~54 mg/dL)
  - Notification channels (Alerts & Reminders)
  - Android 13+ POST_NOTIFICATIONS permission handling
  - Respects "Snack Pass" flag to suppress expected highs
  - Integration with AddReadingViewModel
  - Settings toggle for local alerts
  - Separate from backend SMS alerts for multi-layered safety

#### Tags System - COMPLETE
- [x] **Reading Categorization**
  - Tags field in GlucoseReading entity
  - Database schema v3 with tags migration
  - 12 predefined tags (Fasting, Pre-Meal, Post-Meal, Exercise, Bedtime, etc.)
  - TagSelector composable with FilterChips
  - Multi-select tag functionality
  - Tag display in GlucoseReadingCard with AssistChips
  - Integration with AddReadingScreen
  - Tags stored as comma-separated strings in Room

### ✅ Phase 4 Features (COMPLETED - November 2025)

#### Tag Filtering - COMPLETE
- [x] **Filter Readings by Tags**
  - ReadingsViewModel with tag filtering logic
  - Combines allReadings with selectedTags for real-time filtering
  - Extract availableTags from readings (unique, sorted)
  - TagFilterBar composable with FilterChips
  - Multi-select tag filtering with OR logic
  - Clear filters button and active filter display
  - Dynamic visibility based on tag existence

#### Testing Reminders - COMPLETE (Backend)
- [x] **Scheduled Reminder Notifications**
  - ReminderWorker with WorkManager for daily reminders
  - ReminderService for centralized reminder management
  - Morning and evening reminder support
  - Configurable reminder times (default 8:00 AM, 8:00 PM)
  - UserPreferences extended with reminder settings
  - PreferencesRepository methods for reminder config
  - Background scheduling with battery optimization

### 🚧 Next Steps for Remote Agents (Phase 5 - Lower Priority)

#### Advanced Features
1. **Profile Management**
   - Multiple user profiles
   - Profile switcher
   - Family sharing

2. **Tag Analytics**
   - Tag-based analytics in TrendsScreen
   - Tag management UI in Settings
   - Custom tag creation

#### Bluetooth Integration
1. **Contour Next One Integration** (`service/BluetoothService.kt`)
   - Device scanning
   - Connection management
   - GATT communication
   - Data parsing (IEEE-11073 SFLOAT16)
   - Background reconnection

#### Additional Polish
1. **Reminder Settings UI**
   - Settings screen UI for configuring reminders
   - Time pickers for morning and evening times
   - Master reminder toggle

2. **Accessibility Enhancements**
   - Elder mode with larger typography
   - Content descriptions for all icons
   - Non-color severity indicators

## Key Implementation Notes

### API Integration

#### V3 API (NEW - Recommended)

**Base URL**: https://kulus.web.app/api/v3
**API Key**: kulus_0e8ccd93621cb523b30ede3eb5082f86 (in BuildConfig)
**Partner ID**: malone
**Auth**: Just `x-api-key` header (no token management needed!)

V3 API uses phone numbers (E.164 format) instead of names for user identification.

```kotlin
// Using KulusV3Repository
@Inject lateinit var v3Repository: KulusV3Repository

// Add reading
viewModelScope.launch {
    v3Repository.addReading(
        reading = 6.5,
        units = GlucoseUnits.MMOL_L,
        comment = "Morning reading",
        snackPass = false
    ).onSuccess { reading ->
        // Success
    }.onFailure { error ->
        // Error
    }
}

// Sync from server
viewModelScope.launch {
    v3Repository.syncReadingsFromServer()
}
```

**V3 Components:**
- `data/api/v3/KulusV3ApiService` - Retrofit interface
- `data/api/v3/ApiKeyInterceptor` - Adds x-api-key header
- `data/api/v3/GlucoseLevel` - Level classification (Green/Orange/Red/Purple)
- `data/api/v3/GlucoseUnits` - Units with conversion (factor: 18.0182)
- `data/api/v3/PhoneValidator` - E.164 phone validation (libphonenumber)
- `data/api/v3/ReadingValidator` - 0.1-50 mmol/L range validation
- `data/api/v3/dto/*` - Request/Response DTOs
- `data/repository/KulusV3Repository` - High-level repository

**V3 Tests:** 45 unit tests covering all validation and conversion logic

#### V2 API (Legacy)

**Base URL**: https://kulus.org
**API Key**: kulus-unified-api-key-2025 (in BuildConfig)
**Password**: kulus2025 (in BuildConfig)

#### Authentication Flow
1. `TokenStore` checks for valid token
2. If expired, `AuthInterceptor` triggers re-authentication
3. All API calls automatically include `Authorization: Bearer <token>` header

#### Adding a Reading
```kotlin
viewModelScope.launch {
    repository.addReading(
        reading = 6.5,
        name = "User Name",
        units = GlucoseUnit.MMOL_L,
        comment = "Morning reading",
        snackPass = false
    ).onSuccess { reading ->
        // Navigate back or show success
    }.onFailure { error ->
        // Show error message
    }
}
```

#### Fetching Readings
```kotlin
// In ViewModel
val readings: StateFlow<List<GlucoseReading>> = repository
    .getAllReadingsLocal()
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

// In Composable
val readings by viewModel.readings.collectAsState()
```

#### Sync from Server
```kotlin
viewModelScope.launch {
    repository.syncReadingsFromServer()
        .onSuccess { readings ->
            // UI auto-updates via Flow
        }
        .onFailure { error ->
            // Show error
        }
}
```

### Room Database

**Database**: `KulusDatabase`
**Table**: `glucose_readings`

Key fields:
- `id` (String, primary key) - UUID
- `reading` (Double) - Glucose value
- `units` (String) - "mmol/L" or "mg/dL"
- `name` (String) - User name
- `comment` (String?) - Optional notes
- `snackPass` (Boolean) - Suppress alerts
- `source` (String) - "android", "manual", etc.
- `timestamp` (Long) - Epoch milliseconds
- `synced` (Boolean) - Sync status

### Theme Usage

```kotlin
@Composable
fun MyScreen() {
    KulusTheme {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            Text(
                text = "Reading",
                color = MaterialTheme.colorScheme.primary // MatrixNeon
            )
        }
    }
}
```

#### Color Palette
- **Primary**: MatrixNeon (#00FFB8) - Use for buttons, highlights
- **Secondary**: MatrixAmber (#FFC833) - Use for secondary actions
- **Background**: MatrixBackground (#020B09) - Dark base
- **Surface**: MatrixPanelStart (#031413) - Cards and panels
- **OnBackground**: MatrixTextPrimary (#B8FFE6) - Main text

#### Glucose Level Colors
- **GlucoseGreen**: Normal range
- **GlucoseOrange**: Caution
- **GlucoseRed**: Dangerous
- **GlucosePurple**: Critical

```kotlin
val levelColor = when (reading.color) {
    "Green" -> GlucoseGreen
    "Orange" -> GlucoseOrange
    "Red" -> GlucoseRed
    "Purple" -> GlucosePurple
    else -> MaterialTheme.colorScheme.onSurface
}
```

### Testing

```bash
# Build and run
./gradlew assembleDebug
./gradlew installDebug

# Run tests
./gradlew test
./gradlew connectedAndroidTest
```

### Common Issues

1. **Build errors with JDK**:
   - Solution: Stop Gradle daemons and rebuild
   ```bash
   ./gradlew --stop
   ./gradlew clean assembleDebug
   ```
   - Or use the provided build script: `./build.sh`
   - Ensure `gradle.properties` has: `org.gradle.java.home=/path/to/jdk-17`

2. **Hilt errors**: Check `@HiltAndroidApp` on Application class

3. **API errors**: Verify BuildConfig fields are generated

4. **Room errors**: Rebuild project after schema changes

5. **Gradle daemon using wrong JDK**:
   - Clear transform cache: `rm -rf ~/.gradle/caches/transforms-3`
   - Stop daemons: `./gradlew --stop`
   - Build without daemon: `./gradlew clean assembleDebug --no-daemon`

### Dependencies Reference

```kotlin
// Core
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

// Compose
implementation("androidx.compose.material3:material3")
implementation("androidx.navigation:navigation-compose:2.7.5")

// Hilt
implementation("com.google.dagger:hilt-android:2.48")
ksp("com.google.dagger:hilt-android-compiler:2.48")

// Room
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")

// Network
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
```

## Project Structure

```
app/src/main/java/org/kulus/android/
├── data/
│   ├── api/          # Retrofit service & interceptors
│   ├── local/        # Room database & DAO
│   ├── model/        # Data models & DTOs
│   └── repository/   # Repository implementation
├── di/               # Hilt modules
├── ui/
│   ├── screens/      # Full-screen Composables (TODO)
│   ├── components/   # Reusable UI components (TODO)
│   ├── navigation/   # Nav graph (TODO)
│   └── theme/        # Material3 theme
├── util/             # Utilities (TODO)
├── KulusApplication.kt
└── MainActivity.kt
```

## Related Projects

- **Kulus Web**: ~/WebstormProjects/Kulus-App (Firebase backend)
- **Hamumu iOS**: ~/ios_code/hamumu (iOS version with Kulus v3 integration)
- **KulusCLI**: ~/IdeaProjects/KulusCLI (Kotlin CLI for v3 API)
- **REDO Android**: ~/StudioProjects/redo-android (theme source)

### Cross-Platform Compatibility

All platforms (Android, iOS, Kotlin CLI) use **identical** logic for:
- Glucose level classification (Purple <3.9, Green 3.9-7.8, Orange 7.8-11.1, Red >11.1 mmol/L)
- Reading validation (0.1-50 mmol/L valid range, <3.0 or >20.0 dangerous)
- Unit conversion (factor: 18.0182 mg/dL per mmol/L)
- Phone validation (E.164 regex: `^\+[1-9]\d{1,14}$`)

See `~/ios_code/hamumu/docs/CROSS_PLATFORM_COMPATIBILITY.md` for full compatibility report.

## Git Workflow

```bash
# Feature branch
git checkout -b feature/readings-list-screen
git commit -m "Implement readings list screen

- LazyColumn with glucose readings
- Color-coded level indicators
- Pull-to-refresh for sync
- Search/filter functionality"
git push origin feature/readings-list-screen
```

## Build Instructions for CI/CD

```bash
# Clean build
./gradlew clean assembleDebug

# Release build (requires signing config)
./gradlew assembleRelease

# Generate APK location
# app/build/outputs/apk/debug/app-debug.apk
```

---

**Created**: November 2025
**Last Updated**: November 2025 (Phase 4 Complete)
**Status**: Phases 1-4 complete - Core features, high-value features, onboarding/notifications/tags, and tag filtering/reminders
**GitHub**: https://github.com/jlmalone/kulus_android
