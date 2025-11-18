# Kulus Android - Quick Start for Remote Agents

## 🚀 First Time Here?

Run this command to check what needs to be done:
```bash
./verify_tasks.sh
```

This will show you which tasks are complete and which are pending.

---

## 📋 Task System (Idempotent)

**All tasks are idempotent** - they check if already completed before running.

### Key Files
1. **AGENT_TASKS.md** - Complete task list with verification commands
2. **verify_tasks.sh** - Quick status check script
3. **BUILD_INSTRUCTIONS.md** - Comprehensive build guide

### Quick Task Status Check
```bash
# See what's done
./verify_tasks.sh

# If all critical tasks complete:
# ✅ Critical tasks (P0) completed.
# Data segregation working, app buildable.

# If tasks pending:
# See AGENT_TASKS.md for what to do next
```

---

## 🔨 Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Install on emulator/device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Or combined
./gradlew installDebug
```

---

## ✅ Verification Workflow

### Before Starting Work
```bash
# 1. Check task status
./verify_tasks.sh

# 2. Read pending tasks
cat AGENT_TASKS.md | grep -A 20 "Status.*\[ \]"

# 3. Execute tasks in order
# See AGENT_TASKS.md for each task's actions
```

### After Completing Work
```bash
# 1. Verify task completion
./verify_tasks.sh

# 2. Build to ensure no errors
./gradlew clean assembleDebug

# 3. Report status
./verify_tasks.sh
```

---

## 📊 Current Status

Run `./verify_tasks.sh` to see live status.

**Critical Tasks (P0)**: ✅ COMPLETED
- Build instructions exist
- App icon updated (black circular background)
- Data segregation implemented (users don't see each other's data)
- Purple color removed (replaced with crimson)

---

## 🔧 If Verification Shows "COMPLETED"

If `verify_tasks.sh` shows a task as COMPLETED but you're asked to do it again:

1. **DO NOTHING** - Task already done
2. Report: "Task X already completed (verified by marker)"
3. Move to next pending task

This prevents duplicate work and maintains idempotency.

---

## 📁 Project Structure

```
kulus_android/
├── AGENT_TASKS.md          ← Task list with verification
├── verify_tasks.sh         ← Quick status check
├── BUILD_INSTRUCTIONS.md   ← Complete build guide
├── README_AGENTS.md        ← This file
├── CLAUDE.md               ← Full project context
├── app/
│   └── src/main/java/org/kulus/android/
│       ├── data/
│       │   └── repository/KulusRepository.kt  ← Data segregation logic
│       └── ui/
│           ├── screens/    ← ViewModels here
│           └── theme/Color.kt ← GlucoseCrimson (no purple!)
└── gradle/
```

---

## 🎯 Quick Examples

### Example 1: Check if purple removed
```bash
grep -q "GlucosePurple" app/src/main/java/org/kulus/android/ui/theme/Color.kt
# Exit code 1 = purple gone ✅
# Exit code 0 = purple still there ❌
```

### Example 2: Check if data segregation implemented
```bash
grep -q "fun getCurrentUserReadings()" app/src/main/java/org/kulus/android/data/repository/KulusRepository.kt
# Exit code 0 = implemented ✅
# Exit code 1 = not implemented ❌
```

### Example 3: Build and verify
```bash
./gradlew clean assembleDebug && echo "✅ Build successful" || echo "❌ Build failed"
```

---

## 🚨 Important Notes

### Data Segregation Pattern
**CRITICAL**: Always use `getCurrentUserReadings()` in ViewModels, never `getAllReadingsLocal()`

```kotlin
// ❌ WRONG - Shows all users' data
repository.getAllReadingsLocal()

// ✅ CORRECT - Shows only current user's data
repository.getCurrentUserReadings()
```

### Idempotency
Every task has a **completion marker**. Before executing:
1. Check the marker
2. If present, skip task
3. If absent, execute and add marker

### Color Scheme
- ❌ NO PURPLE (removed at user request)
- ✅ Use: Green (normal), Orange (caution), Red (dangerous), Crimson (critical)

---

## 📞 Getting Help

1. **Build errors**: See BUILD_INSTRUCTIONS.md → "Troubleshooting Build Issues"
2. **Task unclear**: See AGENT_TASKS.md → task's "Actions" section
3. **All tasks complete**: Run `./verify_tasks.sh` → should show "ALL TASKS COMPLETED"

---

**Quick Start Command:**
```bash
./verify_tasks.sh && cat AGENT_TASKS.md
```

This shows status + full task list with verification commands.
