#!/bin/bash
# Kulus Android Build Script
# This script ensures a clean build with the correct JDK

set -e

echo "🧹 Stopping any running Gradle daemons..."
./gradlew --stop

echo "🔨 Building debug APK..."
./gradlew clean assembleDebug

echo "✅ Build complete!"
echo "📦 APK location: app/build/outputs/apk/debug/app-debug.apk"
ls -lh app/build/outputs/apk/debug/app-debug.apk
