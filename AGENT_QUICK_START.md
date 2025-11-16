# Agent Quick Start Guide

## 🎯 Your Mission
Develop Kulus Android to match iOS Hamumu feature parity.

## 📍 Essential Context

**iOS Reference (Read-Only)**: `~/ios_code/hamumu`
**Android Workspace**: `~/StudioProjects/kulus_android`
**Backend**: https://kulus.org (Firebase)

## 📚 Required Reading (First 30 Minutes)
1. This file (5 min)
2. `REMOTE_AGENT_INSTRUCTIONS.md` (full guide - 20 min)
3. `CLAUDE.md` (architecture - 10 min)
4. iOS: `~/ios_code/hamumu/CLAUDE.md` (15 min)

## ✅ Quick Parity Check

### Android HAS ✅
- Manual glucose entry
- Readings list
- Cloud sync (upload/download)
- Room database
- Matrix theme
- Token auth

### Android NEEDS ❌
**Critical (Week 1)**:
- Reading Detail Screen
- Settings Screen
- Dashboard with Tabs
- Background Sync (WorkManager)

**High Priority (Week 2-3)**:
- Photo OCR
- Charts/Analytics
- Onboarding Flow
- Sync UI Indicators

**Medium (Week 4+)**:
- Profile Management
- Tags System
- Notifications
- Data Export

## 🚀 First Task: Reading Detail Screen

**File**: `app/src/main/java/org/kulus/android/ui/screens/ReadingDetailScreen.kt`

**What it does**: Display full glucose reading details when user taps a reading

**iOS Reference**: `~/ios_code/hamumu/HamamuMobile/Views/DashboardView.swift`

**Requirements**:
- Large glucose value display
- Color-coded level (Green/Orange/Red/Purple)
- Name, timestamp, comment, source, sync status
- Edit/Delete buttons
- Navigation from ReadingsListScreen

**Estimated Time**: 4-6 hours

**Dependencies**:
- Existing: KulusRepository, GlucoseReading model
- New: ReadingDetailViewModel

## 🛠️ Build & Test

```bash
# Build
./build.sh

# Or manual
./gradlew clean assembleDebug

# Install on device
./gradlew installDebug

# Check logs
adb logcat | grep Kulus
```

## 📁 Key Files Reference

### Data Layer (Already Complete ✅)
- `data/api/KulusApiService.kt` - API calls
- `data/local/KulusDatabase.kt` - Room DB
- `data/repository/KulusRepository.kt` - Data logic
- `data/model/GlucoseReading.kt` - Data model

### UI Layer (Your Workspace 🚧)
- `ui/screens/` - Full-screen composables
- `ui/components/` - Reusable components
- `ui/theme/` - Matrix theme (complete)
- `MainActivity.kt` - Entry point

### iOS Reference Files
- `Services/KulusRemoteService.swift` - API integration example
- `Views/DashboardView.swift` - Main UI example
- `Views/SettingsView.swift` - Settings example
- `Models/GlucoseData.swift` - Data model example

## 🎨 Theme Usage

```kotlin
@Composable
fun MyScreen() {
    KulusTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Text(
                "Reading",
                color = MaterialTheme.colorScheme.primary // MatrixNeon
            )
        }
    }
}

// Glucose level colors
val levelColor = when (reading.color) {
    "Green" -> GlucoseGreen
    "Orange" -> GlucoseOrange
    "Red" -> GlucoseRed
    "Purple" -> GlucosePurple
    else -> MaterialTheme.colorScheme.onSurface
}
```

## 🏗️ Architecture Pattern

```kotlin
// 1. Screen Composable
@Composable
fun MyScreen(viewModel: MyViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        is Loading -> LoadingIndicator()
        is Success -> Content(uiState.data)
        is Error -> ErrorMessage(uiState.error)
    }
}

// 2. ViewModel
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: KulusRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            repository.getData()
                .onSuccess { data -> _uiState.value = Success(data) }
                .onFailure { error -> _uiState.value = Error(error.message) }
        }
    }
}

// 3. Repository (already exists - use it!)
repository.getAllReadingsLocal() // Flow<List<GlucoseReading>>
repository.addReading(...) // suspend function
repository.syncReadingsFromServer() // suspend function
```

## 📋 Daily Workflow

### Morning
1. `git pull origin main`
2. Review yesterday's work
3. Pick next feature from roadmap
4. Read iOS implementation

### Development
1. Create feature branch: `git checkout -b feature/reading-detail`
2. Implement feature
3. Test frequently: `./gradlew installDebug`
4. Commit often: `git commit -m "Add reading detail screen"`

### Evening
1. Final test on real device
2. Update documentation
3. `git push origin feature/reading-detail`
4. Update `PROGRESS.md`

## 📊 Progress Tracking

Create/Update `PROGRESS.md`:

```markdown
# Week 1

## Day 1 - [Date]
- ✅ Reading Detail Screen - Implemented
- 🚧 Settings Screen - In progress
- ⏳ Dashboard Tabs - Not started

## Blockers
- None

## Notes
- Reading detail screen matches iOS behavior
- Need to verify color-coded levels on real device
```

## ⚠️ Common Pitfalls

1. **Don't reinvent**: Use existing KulusRepository, don't create new API calls
2. **Theme consistency**: Always use KulusTheme and Material3 colors
3. **Navigation**: Use Navigation Compose, not manual nav
4. **State management**: StateFlow + collectAsState, not LiveData
5. **Testing**: Test on real device, not just emulator
6. **Offline-first**: All features must work offline

## 🆘 Getting Unstuck

1. Check `REMOTE_AGENT_INSTRUCTIONS.md` (full guide)
2. Check `CLAUDE.md` (architecture)
3. Look at iOS implementation
4. Look at existing Android code for patterns
5. Check existing screens like `ReadingsListScreen.kt`
6. Document blocker in `BLOCKERS.md`

## 📞 Key Resources

- **Jetpack Compose**: https://developer.android.com/jetpack/compose/documentation
- **Material3**: https://m3.material.io/
- **Hilt DI**: https://dagger.dev/hilt/
- **Room**: https://developer.android.com/training/data-storage/room
- **Navigation**: https://developer.android.com/jetpack/compose/navigation

## 🎯 Week 1 Goals

By end of Week 1, you should have:
- [ ] Reading Detail Screen ✅
- [ ] Settings Screen ✅
- [ ] Dashboard with Tabs ✅
- [ ] Background Sync ✅
- [ ] All commits pushed
- [ ] Documentation updated

## 🚀 Let's Go!

**Start with**: Reading Detail Screen (Day 1-2)
**Reference**: `REMOTE_AGENT_INSTRUCTIONS.md` page 33
**iOS File**: `~/ios_code/hamumu/HamamuMobile/Views/DashboardView.swift`

---

**Ready? Run `./build.sh` to verify your environment, then start coding!** 💻
