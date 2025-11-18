#!/bin/bash
# Kulus Android Setup Script
# This script is IDEMPOTENT - safe to run multiple times
# It will detect if setup is already complete and skip unnecessary steps

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SETUP_MARKER="$PROJECT_ROOT/.setup_complete"
GLOBAL_GRADLE_PROPS="$HOME/.gradle/gradle.properties"
JDK_17_PATH="/Users/josephmalone/Library/Java/JavaVirtualMachines/jbr-17.0.14/Contents/Home"

echo "🔧 Kulus Android Setup"
echo "======================"
echo ""

# Check if already set up
if [ -f "$SETUP_MARKER" ]; then
    echo "✅ Setup already completed on: $(cat $SETUP_MARKER)"
    echo ""
    echo "Current configuration:"
    echo "  - JDK 17: $JDK_17_PATH"
    echo "  - Global Gradle config: $GLOBAL_GRADLE_PROPS"
    echo "  - Launcher icons: ✓ Created"
    echo "  - Gradle wrapper: ✓ Configured"
    echo ""
    echo "To force re-setup, delete: $SETUP_MARKER"
    echo "To build: ./gradlew assembleDebug or ./build.sh"
    exit 0
fi

echo "⚙️  Running first-time setup..."
echo ""

# Step 1: Verify JDK 17 exists
echo "1️⃣  Checking JDK 17..."
if [ ! -d "$JDK_17_PATH" ]; then
    echo "❌ ERROR: JDK 17 not found at $JDK_17_PATH"
    echo "   Please install JDK 17 first"
    exit 1
fi
echo "   ✓ JDK 17 found"

# Step 2: Set up global Gradle properties
echo ""
echo "2️⃣  Configuring global Gradle settings..."
if [ ! -f "$GLOBAL_GRADLE_PROPS" ]; then
    echo "   Creating $GLOBAL_GRADLE_PROPS"
    mkdir -p "$HOME/.gradle"
    cat > "$GLOBAL_GRADLE_PROPS" << 'EOF'
# Global Gradle Configuration
# This file affects ALL Gradle projects on this machine

# Enable Gradle Daemon for faster builds
org.gradle.daemon=true
org.gradle.configureondemand=true

# Memory settings
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=512m

# Force JDK 17 for all Android projects (overrides Android Studio settings)
org.gradle.java.home=/Users/josephmalone/Library/Java/JavaVirtualMachines/jbr-17.0.14/Contents/Home
EOF
    echo "   ✓ Created global gradle.properties"
else
    # Check if JDK setting exists
    if grep -q "org.gradle.java.home.*jbr-17" "$GLOBAL_GRADLE_PROPS"; then
        echo "   ✓ Global gradle.properties already configured"
    else
        echo "   ⚠️  Global gradle.properties exists but missing JDK 17 config"
        echo "   Adding JDK 17 setting..."
        echo "" >> "$GLOBAL_GRADLE_PROPS"
        echo "# Force JDK 17 for all Android projects" >> "$GLOBAL_GRADLE_PROPS"
        echo "org.gradle.java.home=$JDK_17_PATH" >> "$GLOBAL_GRADLE_PROPS"
        echo "   ✓ Updated global gradle.properties"
    fi
fi

# Step 3: Stop any running Gradle daemons
echo ""
echo "3️⃣  Stopping Gradle daemons..."
./gradlew --stop || echo "   (No daemons were running)"

# Step 4: Clear problematic caches
echo ""
echo "4️⃣  Clearing Gradle caches..."
if [ -d "$HOME/.gradle/caches/transforms-3" ]; then
    rm -rf "$HOME/.gradle/caches/transforms-3"
    echo "   ✓ Cleared transform cache"
else
    echo "   ✓ Transform cache already clean"
fi

# Step 5: Verify launcher icons exist
echo ""
echo "5️⃣  Verifying launcher icons..."
ICON_COUNT=$(find app/src/main/res/mipmap-* -name "ic_launcher*.xml" 2>/dev/null | wc -l)
if [ "$ICON_COUNT" -gt 0 ]; then
    echo "   ✓ Found $ICON_COUNT launcher icon files"
else
    echo "   ⚠️  Launcher icons missing (should be auto-generated on build)"
fi

# Step 6: Test build
echo ""
echo "6️⃣  Running test build..."
./gradlew assembleDebug

# Step 7: Create setup marker
echo ""
echo "7️⃣  Marking setup as complete..."
date > "$SETUP_MARKER"
echo "   ✓ Created marker file"

# Final summary
echo ""
echo "=========================================="
echo "✅ Setup Complete!"
echo "=========================================="
echo ""
echo "What was configured:"
echo "  ✓ Global Gradle properties with JDK 17"
echo "  ✓ Stopped old Gradle daemons"
echo "  ✓ Cleared problematic caches"
echo "  ✓ Verified launcher icons"
echo "  ✓ Test build successful"
echo ""
echo "Next steps:"
echo "  1. Open Android Studio"
echo "  2. Hit the Play button ▶️"
echo "  3. Select your device/emulator"
echo ""
echo "Build commands:"
echo "  ./build.sh           - Quick clean build"
echo "  ./gradlew assembleDebug - Standard build"
echo ""
echo "APK location:"
echo "  app/build/outputs/apk/debug/app-debug.apk"
echo ""
