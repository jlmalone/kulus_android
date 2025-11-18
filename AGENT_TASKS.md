# Kulus Android - Remote Agent Task List

This file contains tasks for remote agents to execute. Each task is idempotent - it checks if already completed before running.

**Usage for AI Agents:**
1. Read this file when prompted to work on the project
2. Check task status markers (`[ ]` = pending, `[x]` = completed)
3. Execute pending tasks in order
4. Update status markers when complete
5. If all tasks complete, report "All tasks completed" and do nothing

---

## Task Status

### Build & Configuration Tasks

#### ✅ Task 1: Set up build environment
**Status**: `[x]` COMPLETED (2025-11-17)
**Completion Marker**: `BUILD_INSTRUCTIONS.md` exists at root
**Verification**:
```bash
test -f BUILD_INSTRUCTIONS.md && echo "COMPLETED" || echo "PENDING"
```
**Actions if pending**:
- Create comprehensive build instructions
- Document standalone build process
- Include troubleshooting guide

---

#### ✅ Task 2: Fix app icon with black circular background
**Status**: `[x]` COMPLETED (2025-11-17)
**Completion Marker**: All mipmap icons exist and have black backgrounds
**Verification**:
```bash
# Check if icons exist
for d in mdpi hdpi xhdpi xxhdpi xxxhdpi; do
  test -f app/src/main/res/mipmap-$d/ic_launcher.png || exit 1
done && echo "COMPLETED" || echo "PENDING"
```
**Actions if pending**:
- Add black circular background to Hamumu butterfly icon
- Generate all 5 density variants (48, 72, 96, 144, 192)
- Update both ic_launcher.png and ic_launcher_round.png

---

#### ✅ Task 3: Implement client-side data segregation
**Status**: `[x]` COMPLETED (2025-11-17)
**Completion Marker**: `KulusRepository.kt` contains `getCurrentUserReadings()` method
**Verification**:
```bash
grep -q "fun getCurrentUserReadings()" app/src/main/java/org/kulus/android/data/repository/KulusRepository.kt && echo "COMPLETED" || echo "PENDING"
```
**Actions if pending**:
- Add `getCurrentUserReadings()` method to KulusRepository
- Update ReadingsViewModel to use filtered readings
- Update TrendsViewModel to use filtered readings
- Update SettingsViewModel export to use filtered readings
- Add import: `import kotlinx.coroutines.flow.flatMapLatest`

---

#### ✅ Task 4: Remove purple color from theme
**Status**: `[x]` COMPLETED (2025-11-17)
**Completion Marker**: `Color.kt` has GlucosePurple replaced with GlucoseCrimson
**Verification**:
```bash
grep -q "GlucoseCrimson" app/src/main/java/org/kulus/android/ui/theme/Color.kt && \
! grep -q "GlucosePurple" app/src/main/java/org/kulus/android/ui/theme/Color.kt && \
echo "COMPLETED" || echo "PENDING"
```
**Actions if pending**:
- Replace `GlucosePurple = Color(0xFFAF52DE)` with `GlucoseCrimson = Color(0xFFDC143C)` in Color.kt
- Find all references to `GlucosePurple` and replace with `GlucoseCrimson`
- Rebuild to verify no compilation errors

---

### Code Quality Tasks

#### [ ] Task 5: Add OptIn annotation for experimental coroutines API
**Status**: `[ ]` PENDING
**Completion Marker**: KulusRepository has @OptIn annotation
**Verification**:
```bash
grep -q "@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)" app/src/main/java/org/kulus/android/data/repository/KulusRepository.kt && echo "COMPLETED" || echo "PENDING"
```
**Actions**:
Add annotation to KulusRepository class:
```kotlin
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@Singleton
class KulusRepository @Inject constructor(
```

---

#### [ ] Task 6: Verify all ViewModels use filtered data
**Status**: `[ ]` PENDING
**Completion Marker**: No ViewModel uses `getAllReadingsLocal()` directly
**Verification**:
```bash
! grep -r "getAllReadingsLocal()" app/src/main/java/org/kulus/android/ui/screens/*ViewModel.kt && echo "COMPLETED" || echo "PENDING"
```
**Actions**:
- Search for any remaining `getAllReadingsLocal()` usage in ViewModels
- Replace with `getCurrentUserReadings()`
- Verify data segregation works

---

### Testing Tasks

#### [ ] Task 7: Add unit tests for data segregation
**Status**: `[ ]` PENDING
**Completion Marker**: KulusRepositoryTest exists with segregation tests
**Verification**:
```bash
test -f app/src/test/java/org/kulus/android/data/repository/KulusRepositoryTest.kt && echo "COMPLETED" || echo "PENDING"
```
**Actions**:
- Create KulusRepositoryTest.kt
- Test `getCurrentUserReadings()` filters by userName
- Test multiple users don't see each other's data
- Test user switching scenarios

---

#### [ ] Task 8: Add instrumented test for onboarding flow
**Status**: `[ ]` PENDING
**Completion Marker**: OnboardingFlowTest exists
**Verification**:
```bash
test -f app/src/androidTest/java/org/kulus/android/OnboardingFlowTest.kt && echo "COMPLETED" || echo "PENDING"
```
**Actions**:
- Create OnboardingFlowTest.kt
- Test complete onboarding flow (5 screens)
- Test name validation (min 3 chars, confirmation)
- Test preferences saved correctly
- Test main app blocked until onboarding complete

---

### Documentation Tasks

#### [ ] Task 9: Update CLAUDE.md with latest status
**Status**: `[ ]` PENDING
**Completion Marker**: CLAUDE.md reflects "Data Segregation Fix - COMPLETED"
**Verification**:
```bash
grep -q "Data Segregation.*COMPLETED" CLAUDE.md && echo "COMPLETED" || echo "PENDING"
```
**Actions**:
- Update implementation status section
- Document data segregation implementation
- Add notes about getCurrentUserReadings() pattern
- Update "Last Updated" timestamp

---

#### [ ] Task 10: Create API documentation
**Status**: `[ ]` PENDING
**Completion Marker**: API.md exists at root
**Verification**:
```bash
test -f API.md && echo "COMPLETED" || echo "PENDING"
```
**Actions**:
- Document all KulusApiService endpoints
- Include request/response examples
- Document authentication flow
- Document error handling
- Include curl examples for testing

---

## How to Use This File (For AI Agents)

### On Start
```bash
# Check overall status
grep -c "Status.*\[x\]" AGENT_TASKS.md
grep -c "Status.*\[ \]" AGENT_TASKS.md
```

### Execute Task
```bash
# For each pending task:
# 1. Run verification command
# 2. If PENDING, execute actions
# 3. Verify completion
# 4. Update status from [ ] to [x]
# 5. Add completion date
```

### Example Execution Pattern
```bash
# Task 5 example
if grep -q "@OptIn.*ExperimentalCoroutinesApi" app/src/main/java/org/kulus/android/data/repository/KulusRepository.kt; then
  echo "Task 5: Already completed, skipping"
else
  echo "Task 5: Executing..."
  # Add annotation
  # Rebuild
  # Verify
  # Update AGENT_TASKS.md status to [x]
fi
```

---

## Priority Levels

**P0 (Critical)**: Tasks 1-4 - Must be completed for basic functionality
**P1 (High)**: Tasks 5-6 - Code quality and correctness
**P2 (Medium)**: Tasks 7-8 - Testing coverage
**P3 (Low)**: Tasks 9-10 - Documentation polish

---

## Completion Report Template

When all tasks complete, report:

```
AGENT_TASKS.md Status Report
============================

Total Tasks: 10
Completed: 10
Pending: 0

All tasks completed successfully.

Last completed: Task 10 (API documentation) on YYYY-MM-DD

Build status: ✅ PASSING
Test status: ✅ ALL PASSING
Documentation: ✅ UP TO DATE

Project ready for production.
```

---

**Last Updated**: 2025-11-17
**Current Status**: 4/10 tasks completed (P0 tasks complete, data segregation working)
