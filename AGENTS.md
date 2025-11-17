# Agent Orchestration - Kulus Android Development

**Last Updated**: November 16, 2025
**Current Phase**: Phase 3 - Build Fixes + Feature Completion
**iOS Reference**: ~/ios_code/hamumu (Hamumu Mobile v1.0 build 10)

---

## 🎯 Mission

Achieve **100% feature parity** between Kulus Android and Hamumu iOS while maintaining:
- Offline-first architecture
- Matrix neon theme
- Material3 design system
- Professional code quality

---

## 📊 Current Status

### ✅ Completed (Phase 1 & 2)
- Core architecture (Hilt, Room, Retrofit, MVVM)
- Dashboard with tabs (Today/History/Trends/Settings)
- Reading Detail Screen
- Settings Screen with preferences
- Photo OCR with ML Kit
- Charts & Analytics (Vico - NEEDS FIX)
- Data Export (CSV/JSON/Text)
- Background Sync (WorkManager)
- Enhanced Add Reading Form
- Sync UI Indicators

**Lines of Code**: 4,500+ added by remote agents

### ❌ Blocking Issues (MUST FIX FIRST)
- ⚠️ **Build Broken**: Vico Chart API incompatibility
- ⚠️ **Build Broken**: Missing ML Kit coroutines dependency
- ⚠️ **Build Broken**: WorkManager configuration syntax
- ⚠️ **Build Broken**: HorizontalDivider component

**Current Build Status**: ❌ FAILED

### 🚧 Missing Features (vs iOS)
- Onboarding Flow (Welcome, Phone, Password, Completion)
- Bluetooth Integration (Contour Next One CGM)
- Notifications (Critical glucose alerts)
- Profile Management (Multi-user)
- Tags System
- Search/Filter enhancements

---

## 👥 Agent Types & Responsibilities

### 1. Remote LLMs (GitHub Access Only)
**Tools**: Claude Code API, ChatGPT Code Interpreter, etc.
**Access**: https://github.com/jlmalone/kulus_android
**Limitations**: Cannot see iOS codebase or local filesystem

**Instructions**: `TASKS_FOR_REMOTE_LLMS.md`

**Assigned Tasks**:
- ⚡ **URGENT**: Fix build issues (Task 1A-1D)
- 🔨 Onboarding Flow (Task 2)
- 🔔 Notifications (Task 3)
- 👤 Profile Management (Task 4)
- 🏷️ Tags System (Task 5)

**Workflow**:
```bash
# Clone repo
git clone https://github.com/jlmalone/kulus_android.git

# Create feature branch
git checkout -b feature/[task-name]

# Follow task instructions
# Test: ./gradlew assembleDebug

# Commit & push
git push origin feature/[task-name]
```

---

### 2. Local Agents (Full Filesystem Access)
**Tools**: Gemini with codebase, Codex, local Claude instances
**Access**: Both `~/StudioProjects/kulus_android` AND `~/ios_code/hamumu`
**Advantage**: Can compare iOS vs Android directly

**Instructions**: `TASKS_FOR_LOCAL_AGENTS.md`

**Assigned Tasks**:
- 📊 **CRITICAL**: Feature Parity Analysis (Task 1)
- 📡 **CRITICAL**: Bluetooth Integration (Task 2)
- 🚀 Onboarding Flow Comparison (Task 3)
- 🔍 Advanced Features Analysis (Task 4)
- 📦 Data Model Parity (Task 5)

**Workflow**:
```bash
# Compare codebases
cd ~/StudioProjects/kulus_android
cat CLAUDE.md

cd ~/ios_code/hamumu
cat CLAUDE.md

# Analyze gaps
diff -u <iOS files> <Android files>

# Implement to match iOS
# Test against iOS behavior
```

---

### 3. Claude Code (Orchestrator - This Session)
**Role**: Consolidate, review, merge, delegate
**Responsibilities**:
- Pull and merge remote agent work
- Fix critical blocking issues
- Create task delegation
- Review PRs
- Update documentation
- Maintain architecture quality

**Do NOT**: Implement features directly (minimize token usage)
**DO**: Orchestrate, guide, unblock, merge

---

## 🎯 Task Delegation Matrix

| Task | Type | Assigned To | Priority | Status | File |
|------|------|-------------|----------|--------|------|
| Fix Build | Technical | Remote LLMs | 🔴 CRITICAL | ⏳ Pending | TASKS_FOR_REMOTE_LLMS.md Task 1 |
| Gap Analysis | Strategic | Local Agents | 🔴 CRITICAL | ⏳ Pending | TASKS_FOR_LOCAL_AGENTS.md Task 1 |
| Bluetooth | Complex | Local Agents | 🟠 HIGH | ⏳ Pending | TASKS_FOR_LOCAL_AGENTS.md Task 2 |
| Onboarding | UI/UX | Both | 🟠 HIGH | ⏳ Pending | Both instruction files Task 2/3 |
| Notifications | Feature | Remote LLMs | 🟡 MEDIUM | ⏳ Pending | TASKS_FOR_REMOTE_LLMS.md Task 3 |
| Profiles | Feature | Remote LLMs | 🟡 MEDIUM | ⏳ Pending | TASKS_FOR_REMOTE_LLMS.md Task 4 |
| Tags | Feature | Remote LLMs | 🟢 LOW | ⏳ Pending | TASKS_FOR_REMOTE_LLMS.md Task 5 |
| Data Model | Foundation | Local Agents | 🟡 MEDIUM | ⏳ Pending | TASKS_FOR_LOCAL_AGENTS.md Task 5 |
| Advanced Features | Research | Local Agents | 🟢 LOW | ⏳ Pending | TASKS_FOR_LOCAL_AGENTS.md Task 4 |

---

## 📋 Execution Plan

### Immediate (This Week)
1. **Remote LLMs**: Fix build (1-2 days)
2. **Local Agents**: Create parity matrix (1 day)
3. **Both**: Start onboarding flow (2-3 days)

### Short Term (Next 2 Weeks)
4. **Local Agents**: Bluetooth integration (if critical)
5. **Remote LLMs**: Notifications
6. **Both**: Profile management

### Long Term (Month 2)
7. Tags system
8. Advanced features
9. UI/UX polish
10. Performance optimization

---

## 🔄 Workflow

### For Remote LLMs

```bash
# 1. Pick a task from TASKS_FOR_REMOTE_LLMS.md
# 2. Create branch
git checkout -b feature/fix-build-issues

# 3. Implement
# Follow task instructions exactly

# 4. Test
./gradlew assembleDebug  # Must pass

# 5. Update CLAUDE.md
# Move feature to "✅ Completed"

# 6. Commit
git add -A
git commit -m "Fix build issues

- Fixed Vico Chart API
- Added ML Kit dependency
- Fixed WorkManager config
- Replaced HorizontalDivider

Build now succeeds"

# 7. Push
git push origin feature/fix-build-issues

# 8. Create PR on GitHub
# Title: "[CRITICAL] Fix build issues"
# Assign to: jlmalone for review
```

### For Local Agents

```bash
# 1. Pick task from TASKS_FOR_LOCAL_AGENTS.md
# 2. Study iOS implementation
cat ~/ios_code/hamumu/HamamuMobile/Services/BluetoothService.swift

# 3. Create branch
cd ~/StudioProjects/kulus_android
git checkout -b feature/bluetooth-integration

# 4. Implement Android equivalent
# Match iOS functionality
# Adapt to Android patterns

# 5. Test against iOS
# Same meter → same readings
# Same workflow → same experience

# 6. Document parity
# Add notes on iOS vs Android differences

# 7. Commit & Push
git add -A
git commit -m "Implement Bluetooth integration

iOS Reference: BluetoothService.swift
Android: service/BluetoothService.kt

- Device scanning ✅
- GATT connection ✅
- Data parsing (SFLOAT16) ✅
- Auto-reconnect ✅
- Tested with real Contour Next One meter"

git push origin feature/bluetooth-integration
```

### For Claude Code (Orchestrator)

```bash
# Daily workflow:

# 1. Pull all remote work
git fetch --all
git branch -r | grep -v HEAD

# 2. Review & merge PRs
# Check build: ./gradlew assembleDebug
# Check tests
# Check code quality

# 3. Merge if good
git merge origin/feature/[branch-name]

# 4. Push consolidated main
git push origin main

# 5. Delete merged remote branches
git push origin --delete feature/[branch-name]

# 6. Update AGENTS.md status
# Mark tasks as complete
# Assign new tasks
```

---

## 📊 Success Metrics

### Build Health
- ✅ `./gradlew assembleDebug` passes
- ✅ No compiler errors
- ✅ No runtime crashes
- ✅ APK size < 30MB

### Feature Parity
- ✅ All iOS screens have Android equivalent
- ✅ All iOS services have Android equivalent
- ✅ All workflows work identically
- ✅ Data models match

### Code Quality
- ✅ Follows existing architecture
- ✅ Hilt DI used throughout
- ✅ Coroutines for async ops
- ✅ Flow for reactive data
- ✅ Material3 + Matrix theme
- ✅ Offline-first maintained

### Documentation
- ✅ CLAUDE.md updated
- ✅ FEATURE_PARITY_MATRIX.md complete
- ✅ README.md reflects current state
- ✅ All tasks have completion notes

---

## 🚨 Critical Rules

### For ALL Agents

1. **NEVER Break the Build**
   - Always test before pushing
   - `./gradlew assembleDebug` must succeed

2. **Follow Existing Patterns**
   - Use Hilt for DI
   - Use Room for database
   - Use Retrofit for network
   - Use Navigation Compose

3. **Maintain Offline-First**
   - All features work without network
   - Background sync when online
   - No blocking network calls on UI thread

4. **Use Matrix Theme**
   - Always wrap in `KulusTheme {}`
   - Use `MaterialTheme.colorScheme.*` colors
   - Glucose colors: GlucoseGreen/Orange/Red/Purple

5. **Update Documentation**
   - CLAUDE.md for architecture changes
   - AGENTS.md task status
   - Code comments for complex logic

6. **Create Clean PRs**
   - One feature per PR
   - Clear commit messages
   - Test instructions included
   - Screenshots for UI changes

---

## 📞 Communication

### For Remote LLMs
- **Questions**: Check TASKS_FOR_REMOTE_LLMS.md first
- **Blockers**: Create GitHub issue
- **Architecture**: Read CLAUDE.md
- **Examples**: Look at existing screens

### For Local Agents
- **Questions**: Check TASKS_FOR_LOCAL_AGENTS.md
- **iOS Reference**: Read iOS code directly
- **Comparison**: Use `diff` and `comm` commands
- **Verification**: Test against iOS behavior

### For Claude Code (Orchestrator)
- **Daily**: Review PRs and merge
- **Weekly**: Update task assignments
- **Blockers**: Unblock agents quickly
- **Quality**: Maintain standards

---

## 🎯 Current Sprint (Week of Nov 16, 2025)

### Assigned Tasks

**Remote LLMs**:
- [ ] Fix build issues (CRITICAL - 1-2 days)
- [ ] Start onboarding flow (HIGH - 3-4 days)

**Local Agents**:
- [ ] Create feature parity matrix (CRITICAL - 1 day)
- [ ] Assess if Bluetooth is critical (URGENT - 1 day)
- [ ] Start Bluetooth if needed (HIGH - 5-7 days)

**Claude Code**:
- [x] Consolidate remote work
- [x] Delete old instruction files
- [x] Create new task delegation
- [ ] Review incoming PRs daily
- [ ] Merge approved work
- [ ] Update status weekly

---

## 📈 Progress Tracking

Update this section weekly:

### Week of Nov 16, 2025
- Merged: Phase 2 features (4,500+ lines)
- Build Status: ❌ BROKEN (4 issues)
- Active PRs: 0
- Completed Features: 14
- Remaining Features: 6
- Completion: ~70%

### Week of Nov 23, 2025
- TBD

### Week of Nov 30, 2025
- TBD

---

## ✅ Definition of Done

Project is COMPLETE when:

1. **Build**:
   - ✅ `./gradlew assembleDebug` succeeds
   - ✅ `./gradlew test` passes
   - ✅ No warnings
   - ✅ APK installs and runs

2. **Feature Parity**:
   - ✅ FEATURE_PARITY_MATRIX.md shows 100%
   - ✅ All iOS features implemented or documented as N/A
   - ✅ Same workflows
   - ✅ Same data models

3. **Quality**:
   - ✅ No crashes
   - ✅ No ANRs
   - ✅ Smooth scrolling
   - ✅ Fast startup
   - ✅ Offline works

4. **Documentation**:
   - ✅ CLAUDE.md complete
   - ✅ README.md updated
   - ✅ All code commented
   - ✅ Architecture documented

5. **User Experience**:
   - ✅ Matches iOS quality
   - ✅ Material3 polish
   - ✅ Matrix theme perfect
   - ✅ Intuitive navigation

---

**Status**: 🚧 ACTIVE DEVELOPMENT
**Next Review**: Nov 23, 2025
**Target Completion**: Dec 15, 2025

---

**Let's build! 🚀**
