# Tasks for Local Agents (Gemini/Codex - Full Filesystem Access)

**Android Project**: `~/StudioProjects/kulus_android`
**iOS Reference**: `~/ios_code/hamumu`
**Context**: You have FULL access to both codebases and can compare implementations.

---

## 🎯 Your Advantage

Unlike remote LLMs, you can:
- Read iOS Hamumu implementation directly
- Compare Android vs iOS side-by-side
- Copy architectural patterns from iOS
- Verify feature parity accurately

---

## ⚠️ CRITICAL: Current State Assessment

### Build Status
❌ **BROKEN** - Must fix before new features

**Errors**:
1. Vico Chart API incompatibility (`GlucoseChart.kt`)
2. Missing ML Kit coroutines dependency
3. WorkManager configuration syntax (`KulusApplication.kt`)
4. Missing HorizontalDivider (`AddReadingScreen.kt`)

**Your Task 0** (BLOCKING): Fix all build errors
- See `TASKS_FOR_REMOTE_LLMS.md` Task 1A-1D for details
- OR analyze errors yourself and fix
- **Must complete**: `./gradlew assembleDebug` succeeds

---

## 📊 Feature Parity Analysis

### Task 1: Comprehensive Gap Analysis

**Objective**: Create detailed feature comparison matrix

**Your Advantage**: Read both codebases directly!

**Steps**:

#### 1A. Catalog ALL iOS Features
```bash
cd ~/ios_code/hamumu
cat CLAUDE.md
ls HamamuMobile/Services/*.swift
ls HamamuMobile/Views/*.swift
ls HamamuMobile/Models/*.swift
```

**Document**:
- Every service (Bluetooth, OCR, Kulus API, Camera, Auth, Data)
- Every view (Dashboard, Settings, History, Add Reading, etc.)
- Every model and data structure
- Every UI component and pattern

#### 1B. Check Android Implementation Status
```bash
cd ~/StudioProjects/kulus_android
cat CLAUDE.md
find app/src -name "*.kt" | grep -E "(Screen|Service|Model)"
```

#### 1C. Create Parity Matrix
**File**: `~/StudioProjects/kulus_android/FEATURE_PARITY_MATRIX.md`

**Format**:
```markdown
# Kulus Android ↔ Hamumu iOS Feature Parity

## Core Features

| Feature | iOS File | iOS Status | Android File | Android Status | Gap |
|---------|----------|------------|--------------|----------------|-----|
| Manual glucose entry | `AddReadingView.swift` | ✅ Complete | `AddReadingScreen.kt` | ✅ Complete | None |
| Photo OCR | `OCRService.swift` + `CameraService.swift` | ✅ Complete | `OCRService.kt` + `CameraScreen.kt` | ✅ Complete | None |
| Bluetooth CGM | `BluetoothService.swift` | ✅ Complete | N/A | ❌ Missing | **CRITICAL** |
| Dashboard Tabs | `DashboardTabView.swift` | ✅ Complete | `DashboardScreen.kt` | ✅ Complete | None |
| Charts | Embedded in Dashboard | ✅ Complete | `TrendsScreen.kt` | 🟡 Partial | Vico API issues |
| Settings | `SettingsView.swift` | ✅ Complete | `SettingsScreen.kt` | ✅ Complete | None |
| Onboarding | 9 screens | ✅ Complete | N/A | ❌ Missing | **HIGH** |
| ... | ... | ... | ... | ... | ... |
```

Continue for ALL features, including:
- Authentication
- Cloud sync
- Data export
- Notifications
- Profile management
- Tags
- Search/filter
- Data visualization
- Sharing
- Accessibility

#### 1D. Prioritize Gaps
**File**: `~/StudioProjects/kulus_android/PRIORITY_GAPS.md`

Rank missing features by:
1. **Critical**: Core functionality (e.g., Bluetooth if iOS users rely on it)
2. **High**: Common workflows (e.g., Onboarding for new users)
3. **Medium**: Nice-to-have (e.g., Multiple profiles)
4. **Low**: Advanced features (e.g., Widgets)

---

### Task 2: Bluetooth Integration (CRITICAL GAP)

**Priority**: CRITICAL if iOS users rely on Contour Next One meter
**Estimated Time**: 12-16 hours
**Complexity**: HIGH

**iOS Reference**:
```bash
cat ~/ios_code/hamumu/HamamuMobile/Services/BluetoothService.swift
cat ~/ios_code/hamumu/CLAUDE.md | grep -A 50 "Bluetooth"
```

**Your Objective**: Achieve 100% functional parity with iOS Bluetooth integration

**Android Implementation**:

#### File: `app/src/main/java/org/kulus/android/service/BluetoothService.kt`

**Must Match iOS Features**:
1. **Device Scanning**:
   - Scan for Contour Next One devices
   - Filter by service UUID: `00001808-0000-1000-8000-00805F9B34FB`
   - Display RSSI signal strength
   - Show device name

2. **Connection Management**:
   - Connect to selected device
   - Maintain connection state (disconnected, scanning, connecting, connected, failed)
   - Handle disconnections gracefully
   - Auto-reconnect in background

3. **GATT Communication**:
   - Discover services and characteristics
   - Subscribe to glucose measurement characteristic: `00002A18-0000-1000-8000-00805F9B34FB`
   - Enable notifications
   - Handle characteristic value updates

4. **Data Parsing** (CRITICAL - Must match iOS exactly):
   ```kotlin
   // iOS: BluetoothService.swift:118-131
   fun parseSFLOAT16(data: ByteArray): Double {
       // Extract mantissa (12 bits)
       // Extract exponent (4 bits)
       // Handle special values (NaN, NRes, +Infinity, -Infinity)
       // Calculate: mantissa * 10^exponent
       // Convert to mmol/L or mg/dL
   }
   ```

5. **Device Info**:
   - Read manufacturer name
   - Read device model
   - Sync time with device

**Android BLE Specifics**:
- Request permissions: `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`, `ACCESS_FINE_LOCATION`
- Use `BluetoothLeScanner` for discovery
- Use `BluetoothGatt` for connection
- Handle Android 12+ permission model
- Foreground service for background scanning

**Permissions** (`AndroidManifest.xml`):
```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN"
    android:usesPermissionFlags="neverForLocation" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

**Testing**:
- Real Contour Next One device required
- Test pairing flow
- Test glucose reading reception
- Test disconnection/reconnection
- Verify data matches iOS readings (same meter, same time)

**Deliverable**:
- BluetoothService.kt (matches iOS functionality)
- DeviceConnectionScreen.kt (UI for pairing)
- Updated AddReadingScreen with "Connect Meter" button
- GlucoseReading source field supports "bluetooth"

---

### Task 3: Onboarding Flow Comparison

**Priority**: HIGH
**Estimated Time**: 8-10 hours

**iOS Reference**:
```bash
ls ~/ios_code/hamumu/HamamuMobile/Views/*View.swift | grep -i onboarding
cat ~/ios_code/hamumu/HamamuMobile/Views/WelcomeView.swift
cat ~/ios_code/hamumu/HamamuMobile/Views/PhoneNumberView.swift
cat ~/ios_code/hamumu/HamamuMobile/Views/SetPasswordView.swift
cat ~/ios_code/hamumu/HamamuMobile/Views/CompletionView.swift
```

**Your Task**: Clone iOS onboarding experience in Android

**Match iOS Screens**:
1. **WelcomeView** → `WelcomeScreen.kt`
2. **PhoneNumberView** → `PhoneNumberScreen.kt`
3. **SetPasswordView** → `PasswordScreen.kt`
4. **DeviceTypeView** → `DeviceSelectionScreen.kt` (optional)
5. **DeviceConnectionView** → Part of Bluetooth (Task 2)
6. **DeviceTestingView** → Part of Bluetooth (Task 2)
7. **SMSNotificationView** → `NotificationPermissionScreen.kt`
8. **CompletionView** → `CompletionScreen.kt`

**Key iOS Patterns to Copy**:
- Progress indicator (step X of Y)
- Back navigation (except from Welcome)
- Form validation (phone format, password strength)
- Password strength indicator (same logic as iOS)
- Matrix theme colors (match iOS visual style)
- Same button text and messaging
- Same success animations (if any)

**Test**: Complete onboarding → should feel identical to iOS flow

---

### Task 4: Advanced Features Analysis

**Your Advantage**: See exactly what iOS has that Android doesn't

**Investigate**:

#### 4A. iOS-Only Features
```bash
cd ~/ios_code/hamumu
grep -r "func \|class \|struct " HamamuMobile/ | grep -v "View\|Model" | less
```

Find features like:
- Haptic feedback?
- Widgets?
- Apple Health integration?
- iCloud sync?
- Shortcuts/Siri?
- Face ID/Touch ID?
- Apple Watch?

**Document**:
- Which are iOS-specific (can't port)
- Which should have Android equivalents (Google Fit, etc.)
- Which are optional nice-to-haves

#### 4B. iOS UI Patterns
```bash
cat ~/ios_code/hamumu/HamamuMobile/Views/DashboardView.swift
```

**Analyze**:
- Layout patterns (grid vs list)
- Animation patterns
- Navigation patterns
- Color usage
- Typography
- Iconography

**Replicate in Android**: Material3 + Matrix theme equivalents

---

### Task 5: Data Model Parity

**Priority**: MEDIUM
**Estimated Time**: 4-6 hours

**iOS Models**:
```bash
cat ~/ios_code/hamumu/HamamuMobile/Models/GlucoseData.swift
```

**Compare**:
- Fields in iOS GlucoseReading vs Android GlucoseReading
- Enums (GlucoseSource, GlucoseUnit, etc.)
- Computed properties
- Extensions

**Ensure Android Has**:
- All same fields
- Same enums
- Same validation logic
- Same computed values (level classification, color coding)

**File**: Update `data/model/GlucoseReading.kt` if gaps found

---

## 📋 Submission Requirements

### For Each Task:

1. **Before Starting**:
   ```bash
   git checkout -b feature/[task-name]
   ```

2. **Compare Implementations**:
   - Read iOS code in `~/ios_code/hamumu`
   - Read Android code in `~/StudioProjects/kulus_android`
   - Note differences

3. **Implement to Match iOS**:
   - Copy architecture patterns
   - Match functionality
   - Adapt UI to Material3 (not SwiftUI)
   - Keep Matrix theme

4. **Test Against iOS**:
   - Same inputs → same outputs
   - Same workflows → same results
   - Visual style → Android equivalent

5. **Document**:
   ```markdown
   ## iOS vs Android Implementation

   ### iOS (Reference)
   - File: `HamamuMobile/Services/BluetoothService.swift`
   - Key features: [list]
   - Architecture: [describe]

   ### Android (Implementation)
   - File: `service/BluetoothService.kt`
   - Matches iOS: ✅ [what matches]
   - Android-specific: [what's different and why]
   - Known limitations: [if any]
   ```

6. **Update CLAUDE.md**:
   - Move feature to "✅ Completed"
   - Add "iOS Parity: ✅ Verified [date]"

7. **Commit & Push**:
   ```bash
   git add -A
   git commit -m "[Task #]: [Description]

   iOS Reference: HamamuMobile/[file]
   Android Implementation: [file]

   - Feature 1 matches iOS
   - Feature 2 matches iOS
   - Tests passing
   - Build succeeds"

   git push origin feature/[task-name]
   ```

---

## 🚀 Execution Priority

### Week 1: Critical
1. **Fix Build** (Task 0) - BLOCKING
2. **Gap Analysis** (Task 1) - Strategic
3. **Bluetooth** (Task 2) - Only if iOS users rely on it

### Week 2: High Value
4. **Onboarding** (Task 3) - User experience
5. **Data Model Parity** (Task 5) - Foundation

### Week 3: Polish
6. **Advanced Features** (Task 4) - Nice-to-have
7. **UI/UX Refinement** - Match iOS polish

---

## 🎯 Success Criteria

**Feature parity achieved when**:
- ✅ Android app can do EVERYTHING iOS app can do
- ✅ Same data models and structures
- ✅ Same workflows and user journeys
- ✅ Same or better performance
- ✅ Material3 Android equivalent of iOS design
- ✅ Build succeeds: `./gradlew assembleDebug`
- ✅ All tests pass
- ✅ FEATURE_PARITY_MATRIX.md shows 100%

---

## 📞 Your Advantage

You can run:
```bash
# Compare implementations side-by-side
diff -u \
  <(cat ~/ios_code/hamumu/HamamuMobile/Services/KulusRemoteService.swift) \
  <(cat ~/StudioProjects/kulus_android/app/src/main/java/org/kulus/android/data/repository/KulusRepository.kt)

# Find iOS features not in Android
comm -23 \
  <(find ~/ios_code/hamumu -name "*.swift" | xargs basename -s .swift | sort) \
  <(find ~/StudioProjects/kulus_android -name "*.kt" | xargs basename -s .kt | sort)
```

Use your filesystem access to be thorough!

**Start with Task 0 (fix build) immediately!**
