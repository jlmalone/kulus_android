# Priority Gaps - Kulus Android Feature Parity

**Generated**: November 18, 2025
**Current Completion**: 70% (19/28 features)
**Remaining Gaps**: 9 features

---

## Gap Prioritization Framework

Features are ranked by:
1. **User Impact** - How many users need this?
2. **Safety Critical** - Does it affect health/safety?
3. **Launch Blocker** - Can we ship without it?
4. **Implementation Complexity** - How hard to build?
5. **iOS Dependency** - Do iOS users expect this?

---

## 🔴 CRITICAL Gaps (Must Assess Before Launch)

### Gap #1: Bluetooth CGM Integration

**Status**: ❌ Missing
**Impact**: HIGH if iOS users rely on Contour Next One meter
**Safety**: CRITICAL - Direct glucose readings from medical device
**Complexity**: HIGH (12-16 hours)
**Launch Blocker**: **TBD - REQUIRES DECISION**

**iOS Implementation**:
- `BluetoothService.swift` - Full BLE stack
- Contour Next One CGM support
- Service UUID: `00001808-0000-1000-8000-00805F9B34FB`
- Characteristic: `00002A18-...` (glucose measurement)
- SFLOAT16 data parsing (IEEE-11073)
- Auto-reconnect in background

**Android Requirements**:
```kotlin
// File: service/BluetoothService.kt
- BluetoothLeScanner for discovery
- BluetoothGatt for connection
- SFLOAT16 parser (match iOS algorithm exactly)
- Foreground service for background operation
- Permissions: BLUETOOTH_SCAN, BLUETOOTH_CONNECT, ACCESS_FINE_LOCATION
```

**Decision Matrix**:
| Scenario | Recommendation |
|----------|----------------|
| >50% of iOS users use Bluetooth meter | ✅ MUST HAVE - Block launch |
| 25-50% use it | 🟡 SHOULD HAVE - Ship without, add in v1.1 soon |
| <25% use it | 🟢 NICE TO HAVE - Ship without, add if requested |

**Questions for Product Owner**:
1. What % of iOS users use Contour Next One meter?
2. Do we have a physical meter available for testing?
3. Can users survive with manual entry until v1.1?
4. Is Bluetooth a differentiator vs competitors?

**If YES (Implement Now)**:
- Effort: 12-16 hours
- Timeline: +2 weeks to launch
- Assignment: Local Agent (needs iOS code comparison)
- Task: TASKS_FOR_LOCAL_AGENTS.md Task 2

**If NO (Ship Without)**:
- Alternative: Manual entry only
- Risk: Feature gap vs iOS
- Mitigation: Photo OCR partially covers use case
- Add in: Version 1.1 (post-launch)

---

### Gap #2: Onboarding Flow

**Status**: ❌ Missing
**Impact**: HIGH - Every new user sees this
**Safety**: LOW
**Complexity**: MEDIUM (6-8 hours)
**Launch Blocker**: **YES - Poor first-time experience without it**

**Why Critical**:
- First impression for all new users
- User registration/setup
- Professional app appearance
- iOS users expect it

**iOS Implementation** (4 screens):
1. `WelcomeView.swift` - Intro screen with logo
2. `PhoneNumberView.swift` - User identification
3. `SetPasswordView.swift` - Password setup with strength indicator
4. `CompletionView.swift` - Success message

**Android Implementation Needed**:
```kotlin
// Files to create:
ui/screens/onboarding/
  ├── WelcomeScreen.kt          // Intro + "Get Started" button
  ├── PhoneNumberScreen.kt      // Phone entry with validation
  ├── PasswordScreen.kt         // Password + strength indicator
  ├── CompletionScreen.kt       // Success + "Start Monitoring"
  └── OnboardingCoordinator.kt  // Navigation coordinator

// Data layer:
data/preferences/UserPreferences.kt
  ├── onboardingComplete: Boolean
  └── phoneNumber: String?
```

**User Flow**:
```
App Launch
  ↓
Check onboardingComplete?
  ├─ NO → Show Onboarding (4 screens)
  └─ YES → Show Dashboard

Onboarding:
  Welcome → Phone → Password → Completion → Dashboard
  (Can't skip, can't go back)
```

**Recommendation**: ✅ **IMPLEMENT BEFORE LAUNCH**
- Effort: 6-8 hours
- Timeline: +1 week
- Assignment: Remote LLM (no iOS access needed)
- Task: TASKS_FOR_REMOTE_LLMS.md Task 2
- Priority: **HIGH**

---

## 🟠 HIGH Priority Gaps (Ship Without, Add Soon)

### Gap #3: Local Notifications

**Status**: ❌ Missing
**Impact**: HIGH - Safety feature
**Safety**: CRITICAL - Alerts for dangerous glucose levels
**Complexity**: LOW (4-6 hours)
**Launch Blocker**: NO - but should add in v1.1

**iOS Implementation**:
- `UNUserNotificationCenter` for alerts
- Thresholds: Critical Low (<3.0), Low (3.0-3.9), High (>11.0), Critical High (>15.0)
- Sound + vibration for critical levels
- Customizable thresholds in Settings

**Android Implementation Needed**:
```kotlin
// File: service/NotificationService.kt
@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun showCriticalGlucoseAlert(reading: Double, unit: String)
    fun createNotificationChannels()
    // Channels: "Glucose Alerts" (high priority), "Glucose Warnings" (default)
}

// Permissions:
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

// Settings UI:
SettingsScreen.kt - Add notification preferences section
```

**Alert Thresholds** (mmol/L):
- 🔴 Critical Low: < 3.0 → Sound + vibration
- 🟠 Low: 3.0-3.9 → Vibration only
- 🟠 High: > 11.0 → Vibration only
- 🔴 Critical High: > 15.0 → Sound + vibration

**Trigger Points**:
- When adding new reading (manual or OCR)
- When syncing reading from server
- Not for historical data (only new readings)

**Recommendation**: ✅ **ADD IN v1.1** (1-2 weeks post-launch)
- Effort: 4-6 hours
- Assignment: Remote LLM
- Task: TASKS_FOR_REMOTE_LLMS.md Task 3
- Priority: **HIGH** (safety feature)

---

## 🟡 MEDIUM Priority Gaps (Nice to Have)

### Gap #4: Multi-User Profiles

**Status**: ❌ Missing
**Impact**: MEDIUM - Family sharing use case
**Safety**: LOW
**Complexity**: MEDIUM (6-8 hours)
**Launch Blocker**: NO

**Use Case**:
- Family members sharing one device
- Each person tracks their own glucose
- Switch between profiles

**iOS Implementation**:
- Profile switcher in Dashboard
- Each profile has own readings, settings, preferences
- Avatar colors for visual distinction

**Android Implementation Needed**:
```kotlin
// Data model:
@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: String,
    val name: String,
    val phoneNumber: String?,
    val defaultUnit: GlucoseUnit,
    val isActive: Boolean,  // Only one active
    val avatarColor: String
)

// Database changes:
- Add user_profiles table
- Add profileId to glucose_readings (FK)
- Migration from v2 → v3

// UI:
ui/screens/ProfileManagementScreen.kt
ui/screens/ProfileSelectorScreen.kt
```

**Recommendation**: 🟢 **ADD IN v1.2** (1-2 months post-launch)
- Effort: 6-8 hours
- Assignment: Remote LLM
- Task: TASKS_FOR_REMOTE_LLMS.md Task 4
- Priority: **MEDIUM**

---

### Gap #5: Vico Chart Fix (Partial Implementation)

**Status**: 🟡 Partially implemented (placeholder shown)
**Impact**: MEDIUM - Analytics feature
**Safety**: LOW
**Complexity**: LOW (2-3 hours with docs)
**Launch Blocker**: NO - Statistics still work

**Current State**:
- TrendsScreen.kt shows statistics ✅
- Time range selector works ✅
- Chart shows placeholder "Chart Temporarily Disabled" ⚠️

**Issue**:
- Vico library updated to 1.13.1
- API changed from old `Chart/lineChart` to new `CartesianChartHost`
- Need to migrate to new API

**Fix Needed**:
```kotlin
// File: ui/components/GlucoseChart.kt
// Replace old API:
Chart(chart = lineChart(...))

// With new API:
CartesianChartHost(
    chart = rememberCartesianChart(
        rememberLineCartesianLayer(...)
    ),
    modelProducer = CartesianChartModelProducer.build()
)
```

**Recommendation**: 🟢 **FIX IN v1.1** (when Vico docs available)
- Effort: 2-3 hours
- Blocker: Need Vico 1.13.1 documentation/examples
- Workaround: Statistics display works fine
- Priority: **MEDIUM**

---

## 🟢 LOW Priority Gaps (Future Enhancements)

### Gap #6: Tags System

**Status**: ❌ Missing
**Impact**: LOW - Power user feature
**Complexity**: LOW (4-6 hours)
**Launch Blocker**: NO

**Use Case**:
- Categorize readings: Fasting, Post-Meal, Exercise, Medication
- Filter readings by tag
- Custom tags

**Recommendation**: 🟢 **v1.3+** (if users request)
- Effort: 4-6 hours
- Assignment: Remote LLM
- Task: TASKS_FOR_REMOTE_LLMS.md Task 5
- Priority: **LOW**

---

### Gap #7: Advanced Search/Filter

**Status**: ❌ Missing
**Impact**: LOW - Power user feature
**Complexity**: LOW (3-4 hours)
**Launch Blocker**: NO

**Current State**:
- ReadingsListScreen shows all readings
- Pull-to-refresh works
- Basic list display

**Missing**:
- Search by date range
- Filter by glucose level
- Filter by name/comment
- Sort options

**Recommendation**: 🟢 **v1.3+** (if users request)
- Effort: 3-4 hours
- Priority: **LOW**

---

## Recommended Implementation Timeline

### Week 1 (Current) - CRITICAL
- [x] ✅ Fix build issues (COMPLETED - Task 0)
- [x] ✅ Feature parity analysis (COMPLETED - Task 1)
- [ ] 🔴 **DECISION**: Is Bluetooth critical?

### Week 2 - HIGH PRIORITY
- [ ] 🔴 Onboarding Flow (6-8 hours) - **RECOMMENDED**
  - WelcomeScreen, PhoneScreen, PasswordScreen, CompletionScreen
  - Assign to: Remote LLM
  - Blocks: First-time user experience

### Week 3 - BLUETOOTH (If Critical)
- [ ] 🔴 Bluetooth Integration (12-16 hours) - **IF NEEDED**
  - BluetoothService.kt
  - Device pairing UI
  - SFLOAT16 parser
  - Assign to: Local Agent (needs iOS comparison)
  - Blocks: Hardware users

### Week 4 - POLISH
- [ ] 🟠 Notifications (4-6 hours)
  - NotificationService.kt
  - Alert channels
  - Settings integration
  - Assign to: Remote LLM

### v1.1 (1-2 months post-launch)
- [ ] 🟡 Vico Chart Fix (2-3 hours)
- [ ] 🟠 Bluetooth (if not in v1.0)

### v1.2 (2-3 months post-launch)
- [ ] 🟡 Multi-User Profiles (6-8 hours)

### v1.3+ (Future)
- [ ] 🟢 Tags System (4-6 hours)
- [ ] 🟢 Advanced Search (3-4 hours)

---

## Launch Decision Matrix

### Option A: Minimum Viable (Current State)
**Includes**: Core + Data + OCR + Export + Stats
**Missing**: Onboarding, Bluetooth, Notifications, Profiles, Tags
**Completion**: 70%
**Timeline**: Ready now
**Risk**: ⚠️ Poor first-time UX, no hardware support

---

### Option B: Recommended (Onboarding Added)
**Includes**: Option A + Onboarding Flow
**Missing**: Bluetooth, Notifications, Profiles, Tags
**Completion**: 75%
**Timeline**: +1 week
**Risk**: 🟡 No hardware support (if needed)

**Recommended If**:
- Bluetooth usage is <25% of iOS users
- Can delay notifications to v1.1
- Want to ship soon with good UX

---

### Option C: Full Featured (If Bluetooth Critical)
**Includes**: Option B + Bluetooth + Notifications
**Missing**: Profiles, Tags
**Completion**: 85%
**Timeline**: +3 weeks
**Risk**: 🟢 Launch delay

**Recommended If**:
- Bluetooth usage is >50% of iOS users
- Hardware integration is core value prop
- Safety (notifications) is critical

---

### Option D: 100% Parity (Everything)
**Includes**: All features
**Missing**: Nothing
**Completion**: 100%
**Timeline**: +5-6 weeks
**Risk**: 🟡 Significant launch delay

**Recommended If**:
- Perfect parity required
- No time pressure
- Want feature-complete launch

---

## Immediate Next Steps

### 1. Critical Decision (This Week)
- [ ] **Determine Bluetooth criticality**
  - Survey iOS users: What % use Contour Next One?
  - Get physical meter for testing (if implementing)
  - Decide: Block launch or ship without?

### 2. Assign Tasks (This Week)
- [ ] **Onboarding Flow** → Remote LLM (RECOMMENDED)
  - Task: TASKS_FOR_REMOTE_LLMS.md Task 2
  - Timeline: 6-8 hours
  - Branch: `feature/onboarding-flow`

- [ ] **Bluetooth** → Local Agent (IF CRITICAL)
  - Task: TASKS_FOR_LOCAL_AGENTS.md Task 2
  - Timeline: 12-16 hours
  - Branch: `feature/bluetooth-integration`
  - Requires: Physical Contour Next One meter

### 3. Track Progress (Weekly)
- Update AGENTS.md with status
- Update FEATURE_PARITY_MATRIX.md completion %
- Review PRs and merge approved work

---

## Summary

**Current Status**: 70% feature parity
**Blocking Issue**: Build fixed ✅
**Critical Decision**: Bluetooth yes/no?
**Recommended Next**: Onboarding Flow (1 week)
**Estimated v1.0 Launch**: 2-3 weeks (with onboarding, without Bluetooth)

---

**Generated by**: Claude Code (Local Agent)
**Task**: TASKS_FOR_LOCAL_AGENTS.md Task 1D
**Status**: Phase 1 Analysis Complete
**Next**: Implement onboarding OR assess Bluetooth criticality
