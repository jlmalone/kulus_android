# CLAUDE.md - Kulus Android

This file provides context for Claude Code agents working on this repository.

## Project Overview

**Kulus Android** is a glucose monitoring mobile application that integrates with the Kulus Firebase backend (https://kulus.org). It features a Matrix-inspired neon theme borrowed from the REDO Android project.

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
- [x] Matrix neon color palette from REDO Android
- [x] Material3 dark theme
- [x] Typography system
- [x] Glucose level color coding (Green/Orange/Red/Purple)
- [x] Basic MainActivity with status screen

### 🚧 Next Steps for Remote Agents

#### Priority 1: Core UI Screens
1. **Readings List Screen** (`ui/screens/ReadingsListScreen.kt`)
   - Display all glucose readings in a LazyColumn
   - Show reading value, name, timestamp, color indicator
   - Implement pull-to-refresh for sync
   - Add search/filter by name
   - Show sync status (synced vs. local-only)

2. **Add Reading Screen** (`ui/screens/AddReadingScreen.kt`)
   - Form with OutlinedTextField for glucose value
   - Name input field
   - Unit selector (mmol/L vs. mg/dL) with SegmentedButton
   - Optional comment field
   - Snack pass toggle with explanation
   - Submit button that calls `repository.addReading()`
   - Success/error state handling

3. **Reading Detail Screen** (`ui/screens/ReadingDetailScreen.kt`)
   - Full reading information
   - Color-coded level indicator
   - Sync status
   - Edit/delete options (delete from local only)
   - Timestamp formatting

#### Priority 2: Navigation & ViewModel
1. **Navigation Setup** (`ui/navigation/NavGraph.kt`)
   - Use Navigation Compose
   - Define routes: home, addReading, readingDetail/{id}
   - Bottom navigation or top app bar navigation

2. **ViewModels**
   - `ReadingsViewModel` - Observe `repository.getAllReadingsLocal()`
   - `AddReadingViewModel` - Handle form state and submission
   - `ReadingDetailViewModel` - Load single reading

#### Priority 3: Sync & Error Handling
1. **Sync UI** (`ui/components/SyncIndicator.kt`)
   - Show last sync time
   - Display sync progress
   - Error messages for network issues
   - Retry button

2. **Background Sync**
   - WorkManager for periodic sync
   - Sync unsynced readings on connectivity change
   - Handle auth token expiration gracefully

#### Priority 4: Polish
1. **Empty States**
   - No readings yet message
   - No internet connection screen
   - Loading states

2. **Charts** (Optional)
   - Line chart for glucose trends using Vico or MPAndroidChart
   - Statistics cards (avg, min, max)

## Key Implementation Notes

### API Integration

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
- **Hamumu iOS**: ~/ios_code/hamumu (iOS version with Kulus integration)
- **REDO Android**: ~/StudioProjects/redo-android (theme source)

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
**Last Updated**: November 2025
**Status**: Core architecture complete, ready for UI development
**GitHub**: https://github.com/jlmalone/kulus_android
