# Kulus Android - Standalone Build Instructions

**For Remote Web Agents / Developers Without Access to Related Codebases**

This document provides complete instructions for building and developing the Kulus Android app independently, without requiring access to the iOS codebase, backend, or other related projects.

---

## Quick Start

```bash
# Clone the repository (if needed)
git clone <repository-url>
cd kulus_android

# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug

# Or install manually
adb install app/build/outputs/apk/debug/app-debug.apk
```

**Build output**: `app/build/outputs/apk/debug/app-debug.apk`

---

## Project Overview

**Kulus Android** is a glucose monitoring mobile application with:
- 🎨 Matrix-inspired neon theme (green/amber on dark background)
- 📊 Glucose reading tracking (manual entry, photo OCR, future Bluetooth)
- ☁️ Cloud sync with Firebase backend (https://kulus.org)
- 📈 Charts, trends, and analytics
- 🔐 **Client-side data segregation** by user name
- 📱 Rich onboarding experience

---

## Requirements

### Build Environment
- **JDK**: 17 or higher
- **Gradle**: 8.2+ (uses wrapper, no manual install needed)
- **Android SDK**: API 26 (Android 8.0) minimum, API 34 target
- **Kotlin**: 1.9.0+

### Recommended Tools
- Android Studio Hedgehog (2023.1.1) or newer
- Android Emulator or physical device for testing

---

## Build Commands

### Debug Build
```bash
# Clean build
./gradlew clean assembleDebug

# Build without cleaning (faster)
./gradlew assembleDebug

# Build and install in one command
./gradlew installDebug
```

### Release Build
```bash
./gradlew assembleRelease
# Requires signing configuration in gradle.properties or local.properties
```

### Testing
```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest
```

### Troubleshooting Build Issues

#### JDK Version Issues
```bash
# Stop all Gradle daemons
./gradlew --stop

# Build without daemon
./gradlew clean assembleDebug --no-daemon

# Check JDK version
java -version  # Should show 17+
```

#### Clear Gradle Caches
```bash
rm -rf ~/.gradle/caches/transforms-3
./gradlew clean
./gradlew assembleDebug
```

---

## Project Structure

```
kulus_android/
├── app/src/main/
│   ├── java/org/kulus/android/
│   │   ├── data/
│   │   │   ├── api/          # Retrofit service & interceptors
│   │   │   ├── local/        # Room database & DAO
│   │   │   ├── model/        # Data models & DTOs
│   │   │   ├── preferences/  # DataStore preferences
│   │   │   └── repository/   # Repository pattern (CRITICAL: data segregation here)
│   │   ├── di/               # Hilt dependency injection
│   │   ├── service/          # Background services (sync, notifications)
│   │   ├── ui/
│   │   │   ├── screens/      # Full-screen Composables & ViewModels
│   │   │   ├── components/   # Reusable UI components
│   │   │   └── theme/        # Material3 theme (Matrix colors)
│   │   ├── util/             # Utilities & helpers
│   │   ├── KulusApplication.kt
│   │   └── MainActivity.kt
│   ├── res/                  # Android resources
│   │   ├── mipmap-*/         # App icons (Hamumu butterfly design)
│   │   ├── values/           # Colors, strings, themes
│   │   └── xml/              # Configs
│   └── AndroidManifest.xml
├── build.gradle.kts          # App-level build config
├── gradle/                   # Gradle wrapper
├── CLAUDE.md                 # Detailed project context
└── BUILD_INSTRUCTIONS.md     # This file
```

---

## Key Architecture Concepts

### 1. Data Segregation (CRITICAL)

**Problem**: Users must NOT see each other's glucose data.

**Solution**: Client-side filtering + server-side enforcement

#### Repository Pattern
```kotlin
// In KulusRepository.kt

// ❌ NEVER use this directly in UI:
fun getAllReadingsLocal(): Flow<List<GlucoseReading>>

// ✅ ALWAYS use this instead:
fun getCurrentUserReadings(): Flow<List<GlucoseReading>> {
    return preferencesRepository.userPreferencesFlow
        .flatMapLatest { userPrefs ->
            val userName = userPrefs.defaultName
            glucoseReadingDao.getReadingsByName(userName)
        }
}
```

#### ViewModels
All ViewModels (ReadingsViewModel, TrendsViewModel, etc.) **must** use `getCurrentUserReadings()` to prevent data leakage.

### 2. Authentication Flow

```kotlin
// 1. App authenticates with backend password
val token = apiService.authenticate(
    AuthRequest(password = BuildConfig.API_PASSWORD)
)

// 2. Token stored in DataStore
tokenStore.saveToken(token, expiresIn)

// 3. AuthInterceptor automatically adds token to all API calls
Authorization: Bearer <token>
```

**Backend**: https://kulus.org (Firebase Cloud Functions)
- Password: `kulus2025` (in BuildConfig.API_PASSWORD)
- API Key: `kulus-unified-api-key-2025` (in BuildConfig.API_KEY)

### 3. Offline-First Architecture

```
User Action → Local Room DB (immediate) → Background Sync → Firebase
                     ↓
            UI updates via Flow
```

- All readings saved locally first
- Background WorkManager syncs every 30 minutes
- Manual sync via pull-to-refresh
- App works fully offline

### 4. Onboarding Flow

**Screens** (in order):
1. Welcome - App introduction
2. Features - Feature showcase
3. Privacy - Data segregation explanation
4. Account Setup - User name entry (CRITICAL: this name is used for filtering)
5. Completion - Success message

**First-run detection**: `userPreferences.defaultName == "mobile-user"`

The user's name from onboarding is **the primary identifier** for data segregation.

---

## Backend Integration

### API Endpoints

**Base URL**: `https://kulus.org`

#### Authentication
```http
POST /validatePassword
Content-Type: application/json

{
  "password": "kulus2025"
}

Response:
{
  "success": true,
  "token": "<jwt-token>",
  "expiresIn": 86400000
}
```

#### Get Readings
```http
GET /reportingApiV2?action=readings&name=<userName>
Authorization: Bearer <token>

Response:
{
  "readings": [
    {
      "id": "uuid",
      "reading": 5.0,
      "units": "mmol/L",
      "name": "UserName",
      "timestamp": {...},
      "color": "Green",
      "level": 3,
      ...
    }
  ]
}
```

#### Add Reading
```http
GET /api/v2/addReadingFromUrl?name=<userName>&reading=5.0&units=mmol/L&comment=Test&snackPass=false&source=android
Authorization: Bearer <token>
```

### Data Model Mapping

**Firebase → Android**:
- `reading` (Double) → `GlucoseReading.reading`
- `units` (String) → `GlucoseReading.units` ("mmol/L" or "mg/dL")
- `timestamp._seconds` → `GlucoseReading.timestamp` (convert to millis)
- `color` (String) → UI color (Green/Orange/Red/Purple)
- `level` (Int) → Severity classification (1-8)

---

## Development Workflow

### 1. Feature Development

```bash
# Create feature branch
git checkout -b feature/my-feature

# Make changes

# Build and test
./gradlew assembleDebug
./gradlew installDebug

# Commit
git add .
git commit -m "Add feature: description"
```

### 2. Testing Data Segregation

```bash
# Clear app data
adb shell pm clear org.kulus.android

# Open app, complete onboarding with "Alice"
# Add some readings, sync

# Clear app data again
adb shell pm clear org.kulus.android

# Open app, complete onboarding with "Bob"
# Add readings, sync

# Verify Bob doesn't see Alice's data
```

### 3. Debugging

```bash
# View logs
adb logcat | grep -i kulus

# View database
adb shell "run-as org.kulus.android sqlite3 databases/kulus_database"
sqlite> SELECT id, reading, name FROM glucose_readings;
```

### 4. Check Logs for Data Segregation

Look for these log messages:
```
D/KulusRepository: 🔒 [LOCAL] Filtering readings for user: Alice
D/KulusRepository: 🔒 [SYNC] Fetching readings for user: Alice
D/KulusRepository: ✅ [SYNC] Fetched 5 readings from Kulus for Alice
```

---

## Common Issues & Solutions

### Issue: Build Fails with "Unresolved reference"
**Solution**: Rebuild project
```bash
./gradlew clean build
```

### Issue: "Lint found errors"
**Solution**: Check lint report or disable lint for build
```bash
# View lint report
cat app/build/reports/lint-results-debug.html

# Skip lint (not recommended for production)
./gradlew assembleDebug -x lintDebug
```

### Issue: App shows other users' data
**Cause**: Not using `getCurrentUserReadings()`
**Solution**: Check ViewModel is calling `repository.getCurrentUserReadings()` not `getAllReadingsLocal()`

### Issue: WorkManager initialization error
**Solution**: Ensure `KulusApplication` implements `Configuration.Provider` and InitializationProvider is NOT in AndroidManifest.xml

### Issue: Gradle daemon using wrong JDK
```bash
./gradlew --stop
rm -rf ~/.gradle/caches/transforms-3
./gradlew clean assembleDebug --no-daemon
```

---

## Configuration Files

### gradle.properties
```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
android.enableJetifier=true

# Optional: Set JDK path if multiple versions installed
# org.gradle.java.home=/path/to/jdk-17
```

### local.properties
```properties
sdk.dir=/path/to/Android/sdk

# Optional: Signing config for release builds
keyAlias=kulus-key
keyPassword=<password>
storeFile=/path/to/keystore.jks
storePassword=<password>
```

### BuildConfig Secrets
These are committed in code (not sensitive for this project):
- `API_KEY = "kulus-unified-api-key-2025"`
- `API_PASSWORD = "kulus2025"`
- `API_BASE_URL = "https://kulus.org"`

---

## Testing Checklist

Before submitting changes:

- [ ] Build succeeds: `./gradlew assembleDebug`
- [ ] Unit tests pass: `./gradlew test`
- [ ] App installs: `./gradlew installDebug`
- [ ] Onboarding flow works
- [ ] Data segregation verified (multiple users don't see each other's data)
- [ ] Offline functionality works
- [ ] Sync completes successfully
- [ ] No crashes in logcat

---

## Related Documentation

- **CLAUDE.md**: Comprehensive project context and implementation status
- **ANDROID_AUTH_UPDATE.md**: Authentication security implementation
- **KULUS_SECURITY_AND_ONBOARDING_COMPLETE.md**: Security and onboarding completion summary

---

## Support & Resources

### Documentation
- Android Developers: https://developer.android.com
- Jetpack Compose: https://developer.android.com/jetpack/compose
- Room Database: https://developer.android.com/training/data-storage/room
- Hilt DI: https://dagger.dev/hilt/

### Project Specific
- Backend project: `~/WebstormProjects/Kulus-App` (Firebase)
- iOS version: `~/ios_code/hamumu` (reference for features)

---

## Quick Reference

### Build Times
- Clean build: ~2 minutes
- Incremental build: ~30 seconds
- Install: ~10 seconds

### APK Size
- Debug: ~15-20 MB
- Release (minified): ~8-12 MB

### Minimum Requirements
- Android 8.0 (API 26)
- 50 MB storage
- Internet for cloud sync (optional, works offline)

---

**Last Updated**: November 2025
**Status**: Production-ready with Phase 1-4 features complete
**Current Version**: 1.0

---

## Emergency Commands

```bash
# Complete reset
adb uninstall org.kulus.android
./gradlew clean
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk

# Clear all Gradle caches
./gradlew --stop
rm -rf ~/.gradle/caches
rm -rf .gradle
./gradlew clean assembleDebug

# Force reinstall
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

**Remember**: This project is standalone. All dependencies are declared in `build.gradle.kts`. No external codebases required for building.
