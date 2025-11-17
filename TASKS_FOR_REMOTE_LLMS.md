# Tasks for Remote LLMs (GitHub Repo Access Only)

**Repository**: https://github.com/jlmalone/kulus_android
**Context**: You have NO access to iOS codebase or local filesystem. All context is in this repo.

---

## ⚠️ CRITICAL: Fix Build Issues FIRST

**Priority**: BLOCKING - Must complete before any new features
**Estimated Time**: 2-4 hours

### Task 1A: Fix Vico Chart API Compatibility

**File**: `app/src/main/java/org/kulus/android/ui/components/GlucoseChart.kt`

**Problem**: Vico 1.13.1 API has changed. Multiple type mismatches and unresolved references.

**Errors**:
```
Cannot find parameter: lineThicknessDp
Type mismatch: ChartEntryModel
Unresolved reference: color, textSizeSp
```

**Solution**: Consult Vico 1.13.1 documentation and update:
- `lineSpec()` parameters (line 111-116)
- `textComponent` configuration (lines 123-126, 131-134)
- ChartEntryModel usage (line 120)

**Reference**: https://github.com/patrykandpatrick/vico

**Test**: Build must succeed: `./gradlew assembleDebug`

---

### Task 1B: Add Missing ML Kit Dependency

**File**: `app/build.gradle.kts`

**Problem**: OCRService uses `await()` which requires kotlinx-coroutines-play-services

**Solution**: Add after line 116:
```kotlin
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
```

**Test**: OCRService should compile without "Unresolved reference: await" error

---

### Task 1C: Fix WorkManager Configuration

**File**: `app/src/main/java/org/kulus/android/KulusApplication.kt`

**Problem**: Configuration.Provider expects property, not method

**Current (lines 22-26)**:
```kotlin
override fun getWorkManagerConfiguration(): Configuration {
    return Configuration.Builder()
        .setWorkerFactory(workerFactory)
        .build()
}
```

**Replace with**:
```kotlin
override val workManagerConfiguration: Configuration
    get() = Configuration.Builder()
        .setWorkerFactory(workerFactory)
        .build()
```

**Test**: Build should succeed

---

### Task 1D: Fix Missing HorizontalDivider

**File**: `app/src/main/java/org/kulus/android/ui/screens/AddReadingScreen.kt`

**Problem**: Lines 162, 169 use `HorizontalDivider` which doesn't exist in current Material3

**Solution**: Replace with:
```kotlin
Divider(modifier = Modifier.padding(vertical = 16.dp))
```

**Test**: AddReadingScreen should compile

---

## ✅ Verification Checklist

After completing all build fixes:

```bash
# Must pass
./gradlew clean assembleDebug

# Should generate APK at:
# app/build/outputs/apk/debug/app-debug.apk
```

Once build succeeds, commit:
```bash
git checkout -b fix/build-issues
git add -A
git commit -m "Fix build issues: Vico API, ML Kit dependency, WorkManager syntax"
git push origin fix/build-issues
```

---

## 🎯 Phase 3 Features (After Build Fixed)

### Task 2: Onboarding Flow

**Priority**: HIGH
**Estimated Time**: 6-8 hours
**Files to Create**:
- `ui/screens/onboarding/WelcomeScreen.kt`
- `ui/screens/onboarding/PhoneNumberScreen.kt`
- `ui/screens/onboarding/PasswordScreen.kt`
- `ui/screens/onboarding/CompletionScreen.kt`
- `ui/screens/onboarding/OnboardingCoordinator.kt`

**Requirements**:

#### WelcomeScreen:
- Hamumu logo (or Kulus logo)
- App tagline: "Indigenous diabetes management"
- "Get Started" button
- Matrix theme colors

#### PhoneNumberScreen:
- Phone number input with formatting
- Validation (10 digits)
- Continue button (disabled until valid)

#### PasswordScreen:
- Password input with show/hide toggle
- Confirm password field
- Strength indicator (Weak/Medium/Strong):
  - Weak: < 8 chars
  - Medium: 8+ chars, 1 uppercase, 1 number
  - Strong: Medium + 1 special char
- Requirements checklist display
- Continue button (disabled until match + strong)

#### CompletionScreen:
- Success message
- "Start Monitoring" button → navigate to Dashboard
- Optional: celebration animation

#### OnboardingCoordinator:
- Stepper progress indicator (1/4, 2/4, etc.)
- Navigation between screens
- Save completion state to DataStore
- On completion: set `onboardingComplete = true`

**DataStore Addition**:
```kotlin
// In UserPreferences.kt, add:
val onboardingComplete: Boolean = false
val phoneNumber: String? = null
```

**MainActivity Integration**:
```kotlin
// Check onboarding state on startup
if (!preferencesRepository.onboardingComplete) {
    // Navigate to OnboardingCoordinator
} else {
    // Navigate to Dashboard
}
```

**Test**:
- Fresh install should show onboarding
- Completing onboarding should save state
- Re-opening app should skip to Dashboard
- Back button from Dashboard should NOT return to onboarding

---

### Task 3: Notifications for Critical Glucose Levels

**Priority**: MEDIUM
**Estimated Time**: 4-6 hours
**File to Create**: `service/NotificationService.kt`

**Requirements**:

#### NotificationService:
```kotlin
@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: PreferencesRepository
) {
    fun showCriticalGlucoseAlert(reading: Double, unit: String)
    fun cancelAllNotifications()
    private fun createNotificationChannel()
}
```

#### Alert Thresholds (mmol/L):
- Critical Low: < 3.0 → RED alert with sound
- Low: 3.0-3.9 → ORANGE warning
- High: > 11.0 → ORANGE warning
- Critical High: > 15.0 → RED alert with sound

#### Notification Channels:
- "Glucose Alerts" (high importance, sound + vibration)
- "Glucose Warnings" (default importance, vibration only)

#### UserPreferences Addition:
```kotlin
val notificationsEnabled: Boolean = true
val criticalLowThreshold: Double = 3.0
val criticalHighThreshold: Double = 15.0
```

#### Settings UI Integration:
Add to SettingsScreen:
- "Notifications" section
- Enable/disable toggle
- Threshold sliders (with mmol/L ↔ mg/dL conversion)

#### Repository Integration:
In `KulusRepository.addReading()`, after saving:
```kotlin
if (reading meets critical threshold) {
    notificationService.showCriticalGlucoseAlert(reading, unit)
}
```

**AndroidManifest.xml**:
```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

**Test**:
- Add reading with critical value → notification appears
- Tap notification → opens ReadingDetailScreen
- Disable in settings → no notifications
- Change thresholds → alerts respect new values

---

### Task 4: Profile Management (Multi-User)

**Priority**: MEDIUM
**Estimated Time**: 6-8 hours

**Files to Create**:
- `data/model/UserProfile.kt`
- `data/local/UserProfileDao.kt`
- `ui/screens/ProfileManagementScreen.kt`
- `ui/screens/ProfileSelectorScreen.kt`

**Schema**:
```kotlin
@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phoneNumber: String? = null,
    val defaultUnit: GlucoseUnit = GlucoseUnit.MMOL_L,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = false,  // Only one active at a time
    val avatarColor: String = "#00FFB8"  // Matrix theme colors
)
```

**GlucoseReading Update**:
Add field:
```kotlin
val profileId: String  // FK to UserProfile
```

**Database Migration**:
- Version 3
- Add user_profiles table
- Add profileId to glucose_readings
- Create default profile from current preferences

**UI Features**:

#### ProfileManagementScreen (in Settings):
- List all profiles
- Add new profile button
- Edit profile (name, phone, default unit, color)
- Delete profile (with confirmation, can't delete active)
- Set as active

#### ProfileSelectorScreen (on app start):
- Quick profile switcher
- Shows all profiles with avatar
- Tap to select → becomes active
- "Manage Profiles" button → ProfileManagementScreen

**Repository Changes**:
- All reading queries filter by active profileId
- addReading() auto-adds active profileId
- Sync includes profileId in remote data

**Test**:
- Create 2 profiles
- Add readings for each
- Switch profile → only see that profile's readings
- Sync → readings go to correct profile on backend

---

### Task 5: Tags System

**Priority**: LOW
**Estimated Time**: 4-6 hours

**Files to Create**:
- `data/model/Tag.kt`
- `data/local/TagDao.kt`
- `ui/screens/TagManagementScreen.kt`
- `ui/components/TagSelector.kt`

**Schema**:
```kotlin
@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: String,  // Hex color
    val icon: String?   // Material icon name
)

// Junction table for many-to-many
@Entity(
    tableName = "reading_tags",
    primaryKeys = ["readingId", "tagId"]
)
data class ReadingTag(
    val readingId: String,
    val tagId: String
)
```

**Predefined Tags** (created on first launch):
- 🍽️ Fasting (purple)
- 🍔 Post-Meal (orange)
- 💪 Exercise (green)
- 💊 Medication (blue)
- 😴 Bedtime (dark blue)
- ☀️ Morning (yellow)

**AddReadingScreen Integration**:
- Tag selector (multi-select chips)
- Shows all tags
- Selected tags saved with reading

**ReadingsListScreen Filter**:
- Filter chip row at top
- Show readings with ANY selected tag
- "All" chip to clear filter

**SettingsScreen Integration**:
- "Manage Tags" button → TagManagementScreen
- Create custom tags
- Edit/delete custom tags
- Can't delete predefined tags

**Test**:
- Add reading with tags
- Filter list by tag
- Create custom tag
- Tag appears in selector

---

## 📋 Submission Requirements

For each completed task:

### 1. Create Feature Branch
```bash
git checkout -b feature/[task-name]
```

### 2. Implement & Test
- Write code
- Test on emulator: `./gradlew installDebug`
- Verify functionality

### 3. Update CLAUDE.md
Move feature from "🚧 Next Steps" to "✅ Completed"

### 4. Commit with Details
```bash
git add -A
git commit -m "[Task #]: [Short description]

- Feature 1 implemented
- Feature 2 implemented
- Tests passing
- Updated CLAUDE.md"
```

### 5. Push & Create PR
```bash
git push origin feature/[task-name]
```

Then create Pull Request on GitHub with:
- Title: `[Task #]: [Short description]`
- Description: What was implemented, how to test
- Screenshots (if UI changes)

---

## 🚨 Important Notes

- **NO iOS Access**: You cannot reference iOS code. Use CLAUDE.md for architecture guidance.
- **Follow Existing Patterns**: Look at existing screens (ReadingsListScreen, SettingsScreen) for examples.
- **Matrix Theme**: Always use `KulusTheme` and Material3 colors.
- **Offline-First**: All features must work offline, sync when online.
- **Test Before Push**: `./gradlew assembleDebug` must succeed.

---

## 📞 Questions?

- Check `CLAUDE.md` for architecture
- Look at existing code for patterns
- Review `data/repository/KulusRepository.kt` for data operations
- Test frequently!

**Ready? Start with Task 1 (Build Fixes) immediately!**
