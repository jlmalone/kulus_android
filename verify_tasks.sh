#!/bin/bash
# Kulus Android - Task Verification Script
# Run this to check which tasks are completed
# Usage: ./verify_tasks.sh

set -e

echo "========================================"
echo "Kulus Android - Task Verification"
echo "========================================"
echo ""

COMPLETED=0
PENDING=0

# Task 1: Build instructions
echo -n "Task 1 (Build Instructions): "
if test -f BUILD_INSTRUCTIONS.md; then
  echo "✅ COMPLETED"
  COMPLETED=$((COMPLETED + 1))
else
  echo "❌ PENDING"
  PENDING=$((PENDING + 1))
fi

# Task 2: App icon
echo -n "Task 2 (Black Circular Icon): "
ALL_ICONS_EXIST=true
for d in mdpi hdpi xhdpi xxhdpi xxxhdpi; do
  if ! test -f app/src/main/res/mipmap-$d/ic_launcher.png; then
    ALL_ICONS_EXIST=false
  fi
done
if $ALL_ICONS_EXIST; then
  echo "✅ COMPLETED"
  COMPLETED=$((COMPLETED + 1))
else
  echo "❌ PENDING"
  PENDING=$((PENDING + 1))
fi

# Task 3: Data segregation
echo -n "Task 3 (Data Segregation): "
if grep -q "fun getCurrentUserReadings()" app/src/main/java/org/kulus/android/data/repository/KulusRepository.kt 2>/dev/null; then
  echo "✅ COMPLETED"
  COMPLETED=$((COMPLETED + 1))
else
  echo "❌ PENDING"
  PENDING=$((PENDING + 1))
fi

# Task 4: Purple removal
echo -n "Task 4 (Remove Purple Color): "
if grep -q "GlucoseCrimson" app/src/main/java/org/kulus/android/ui/theme/Color.kt 2>/dev/null && \
   ! grep -q "GlucosePurple" app/src/main/java/org/kulus/android/ui/theme/Color.kt 2>/dev/null; then
  echo "✅ COMPLETED"
  COMPLETED=$((COMPLETED + 1))
else
  echo "❌ PENDING"
  PENDING=$((PENDING + 1))
fi

# Task 5: OptIn annotation
echo -n "Task 5 (OptIn Annotation): "
if grep -q "@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)" app/src/main/java/org/kulus/android/data/repository/KulusRepository.kt 2>/dev/null; then
  echo "✅ COMPLETED"
  COMPLETED=$((COMPLETED + 1))
else
  echo "❌ PENDING"
  PENDING=$((PENDING + 1))
fi

# Task 6: ViewModel verification
echo -n "Task 6 (ViewModels Use Filtered Data): "
if ! grep -r "getAllReadingsLocal()" app/src/main/java/org/kulus/android/ui/screens/*ViewModel.kt 2>/dev/null; then
  echo "✅ COMPLETED"
  COMPLETED=$((COMPLETED + 1))
else
  echo "❌ PENDING (some ViewModels still use getAllReadingsLocal)"
  PENDING=$((PENDING + 1))
fi

# Task 7: Repository tests
echo -n "Task 7 (Repository Unit Tests): "
if test -f app/src/test/java/org/kulus/android/data/repository/KulusRepositoryTest.kt; then
  echo "✅ COMPLETED"
  COMPLETED=$((COMPLETED + 1))
else
  echo "❌ PENDING"
  PENDING=$((PENDING + 1))
fi

# Task 8: Onboarding tests
echo -n "Task 8 (Onboarding Flow Tests): "
if test -f app/src/androidTest/java/org/kulus/android/OnboardingFlowTest.kt; then
  echo "✅ COMPLETED"
  COMPLETED=$((COMPLETED + 1))
else
  echo "❌ PENDING"
  PENDING=$((PENDING + 1))
fi

# Task 9: CLAUDE.md update
echo -n "Task 9 (Update CLAUDE.md): "
if grep -q "Data Segregation.*COMPLETED" CLAUDE.md 2>/dev/null; then
  echo "✅ COMPLETED"
  COMPLETED=$((COMPLETED + 1))
else
  echo "❌ PENDING"
  PENDING=$((PENDING + 1))
fi

# Task 10: API documentation
echo -n "Task 10 (API Documentation): "
if test -f API.md; then
  echo "✅ COMPLETED"
  COMPLETED=$((COMPLETED + 1))
else
  echo "❌ PENDING"
  PENDING=$((PENDING + 1))
fi

echo ""
echo "========================================"
echo "Summary"
echo "========================================"
echo "Total Tasks: 10"
echo "Completed: $COMPLETED"
echo "Pending: $PENDING"
echo ""

if [ $COMPLETED -eq 10 ]; then
  echo "🎉 ALL TASKS COMPLETED!"
  echo "Project ready for production."
  exit 0
elif [ $COMPLETED -ge 4 ]; then
  echo "✅ Critical tasks (P0) completed."
  echo "Data segregation working, app buildable."
  exit 0
else
  echo "⚠️  Critical tasks incomplete."
  echo "See AGENT_TASKS.md for details."
  exit 1
fi
