# Kulus Android

A modern Android glucose monitoring application that integrates with the Kulus Firebase backend.

## Overview

Kulus Android is a native Android application for tracking and managing blood glucose readings. It features offline-first architecture, cloud synchronization with Firebase, and a beautiful Matrix-inspired neon theme.

## Features

### Core Functionality
- ✅ **Add Glucose Readings**: Record glucose values with name, units (mmol/L or mg/dL), comments, and snack pass flag
- ✅ **Local-First Architecture**: All data stored locally in Room database for offline access
- ✅ **Cloud Sync**: Automatic synchronization with Kulus Firebase backend
- ✅ **Authentication**: Secure token-based authentication with automatic refresh
- ✅ **Color-Coded Levels**: Visual glucose level indicators (Green/Orange/Red/Purple)
- 🚧 **Reading History**: View all past readings with filtering and sorting (UI in progress)
- 🚧 **Analytics**: Glucose trends and patterns visualization (planned)

### Technical Stack

#### Architecture
- **MVVM**: Model-View-ViewModel pattern with Jetpack Compose
- **Repository Pattern**: Clean separation of data sources
- **Dependency Injection**: Hilt for DI
- **Reactive Programming**: Kotlin Coroutines and Flow

#### Libraries
- **Jetpack Compose**: Modern declarative UI
- **Material3**: Latest Material Design components
- **Room**: Local SQLite database with Flow support
- **Retrofit**: HTTP client for API calls
- **OkHttp**: Network interceptors and logging
- **Hilt**: Dependency injection
- **DataStore**: Preferences storage for auth tokens

#### Database
- **Room Database**: `kulus_database`
  - Table: `glucose_readings`
  - Supports offline-first with sync flag
  - Automatic type converters

#### API Integration
- **Base URL**: https://kulus.org (or https://us-east1-kulus-85de3.cloudfunctions.net)
- **Authentication**: Custom token-based (password: kulus2025)
- **Endpoints**:
  - POST `/validatePassword` - Acquire auth token
  - GET `/api/v2/getAllReadings` - Fetch all readings
  - GET `/api/v2/addReadingFromUrl` - Submit new reading

## Project Structure

```
app/src/main/java/org/kulus/android/
├── data/
│   ├── api/
│   │   ├── KulusApiService.kt         # Retrofit API interface
│   │   └── AuthInterceptor.kt         # OkHttp auth interceptor
│   ├── local/
│   │   ├── KulusDatabase.kt           # Room database
│   │   ├── GlucoseReadingDao.kt       # Room DAO
│   │   └── TokenStore.kt              # DataStore for tokens
│   ├── model/
│   │   └── GlucoseReading.kt          # Data models and DTOs
│   └── repository/
│       └── KulusRepository.kt         # Data layer coordinator
├── di/
│   └── AppModule.kt                   # Hilt dependency injection
├── ui/
│   ├── screens/                       # Composable screens (TODO)
│   ├── components/                    # Reusable UI components (TODO)
│   └── theme/
│       ├── Color.kt                   # Matrix neon color palette
│       ├── Theme.kt                   # Material3 theme
│       └── Type.kt                    # Typography definitions
├── KulusApplication.kt                # Application class
└── MainActivity.kt                    # Entry point with basic UI
```

## Setup

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17
- Android SDK 26+ (minimum)
- Android SDK 34 (target)

### Building

1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/kulus_android.git
   cd kulus_android
   ```

2. **Open in Android Studio**:
   - File → Open → Select `kulus_android` directory
   - Wait for Gradle sync to complete

3. **Run the app**:
   - Connect an Android device or start an emulator
   - Click Run (▶️) or press Shift+F10

### Gradle Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test

# Clean and rebuild
./gradlew clean build
```

## Configuration

API configuration is set in `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://kulus.org\"")
buildConfigField("String", "API_KEY", "\"kulus-unified-api-key-2025\"")
buildConfigField("String", "API_PASSWORD", "\"kulus2025\"")
```

## Design System

### Matrix Neon Theme

Inspired by REDO Android, the app uses a cyberpunk Matrix aesthetic:

- **Background**: `#020B09` (Dark greenish-black)
- **Panels**: `#031413` to `#061F18` (Dark panels with gradient)
- **Primary (Neon)**: `#00FFB8` (Bright cyan/mint green)
- **Secondary (Amber)**: `#FFC833` (Gold/amber accent)
- **Text Primary**: `#B8FFE6` (Light cyan)
- **Text Secondary**: `#99B8FFE6` (Dimmed cyan)

### Glucose Level Colors

- **Green** (`#00D68F`): Normal range (4.0-7.0 mmol/L)
- **Orange** (`#FF9500`): Caution levels
- **Red** (`#FF3B30`): Dangerous levels
- **Purple** (`#AF52DE`): Critical levels

## Development Status

### ✅ Completed
- [x] Project structure and Gradle setup
- [x] Data models and Room database
- [x] Retrofit API client with authentication
- [x] Repository layer with offline-first logic
- [x] Hilt dependency injection
- [x] Material3 theme with Matrix colors
- [x] Basic MainActivity with status display

### 🚧 In Progress
- [ ] Full UI implementation with Jetpack Compose
- [ ] Readings list screen
- [ ] Add reading screen with form
- [ ] Sync functionality and error handling
- [ ] Settings screen

### 📋 Planned
- [ ] Charts and analytics
- [ ] Notifications for critical levels
- [ ] Export data functionality
- [ ] User profiles
- [ ] Widget support
- [ ] Wear OS companion app

## API Reference

### Adding a Reading

```kotlin
val repository: KulusRepository // Injected via Hilt

repository.addReading(
    reading = 6.5,
    name = "User Name",
    units = GlucoseUnit.MMOL_L,
    comment = "Morning reading",
    snackPass = false
)
```

### Fetching Readings

```kotlin
// Observe local readings (reactive)
repository.getAllReadingsLocal()
    .collect { readings ->
        // Update UI
    }

// Sync from server
repository.syncReadingsFromServer()
```

### Authentication

Authentication is handled automatically by `AuthInterceptor` and `TokenStore`. Tokens are refreshed when expired.

## Related Projects

- **Kulus Web App**: https://github.com/yourusername/Kulus-App
- **Kulus iOS (Hamumu)**: ~/ios_code/hamumu (integrates Kulus API)
- **REDO Android**: ~/StudioProjects/redo-android (theme inspiration)

## Contributing

This is a personal health tracking project. Contributions for bug fixes and feature enhancements are welcome via pull requests.

## License

MIT License - see LICENSE file for details

## Acknowledgments

- Matrix neon theme inspired by REDO Android
- API backend powered by Firebase Cloud Functions
- Built with Jetpack Compose and Material3

---

**Project created**: November 2025
**Last updated**: November 2025
**Status**: Core architecture complete, UI implementation in progress
