# Kulus Android - Setup Guide

## Quick Start

```bash
./setup.sh
```

This script is **idempotent** - it's safe to run multiple times. If setup is already complete, it will just report the current status.

## What the Setup Script Does

1. **Verifies JDK 17** - Checks that JDK 17 is installed
2. **Configures Global Gradle** - Sets up `~/.gradle/gradle.properties` with JDK 17
3. **Stops Gradle Daemons** - Clears any running daemons with old settings
4. **Clears Caches** - Removes problematic transform caches
5. **Verifies Icons** - Checks that launcher icons are present
6. **Test Build** - Runs a full build to verify everything works
7. **Creates Marker** - Leaves `.setup_complete` file with timestamp

## If Already Set Up

If you run `./setup.sh` after initial setup, you'll see:

```
✅ Setup already completed on: [timestamp]

Current configuration:
  - JDK 17: /Users/josephmalone/Library/Java/JavaVirtualMachines/jbr-17.0.14/Contents/Home
  - Global Gradle config: /Users/josephmalone/.gradle/gradle.properties
  - Launcher icons: ✓ Created
  - Gradle wrapper: ✓ Configured

To force re-setup, delete: .setup_complete
```

## Force Re-Setup

If you need to run setup again:

```bash
rm .setup_complete
./setup.sh
```

## Manual Setup (if script fails)

### 1. Configure JDK

Edit `~/.gradle/gradle.properties`:
```properties
org.gradle.java.home=/Users/josephmalone/Library/Java/JavaVirtualMachines/jbr-17.0.14/Contents/Home
```

### 2. Clear Caches

```bash
./gradlew --stop
rm -rf ~/.gradle/caches/transforms-3
```

### 3. Build

```bash
./gradlew clean assembleDebug
```

## Troubleshooting

### Android Studio Still Uses Wrong JDK

1. Open **Android Studio → Settings → Build Tools → Gradle**
2. Set **Gradle JDK** to **"jbr-17"**
3. Click **Apply** and **OK**
4. Restart Android Studio

### Build Fails with JDK Errors

Run the setup script again:
```bash
rm .setup_complete
./setup.sh
```

### Icons Missing

The setup script will detect this. Icons are auto-generated on first build.

## Files Created by Setup

- `~/.gradle/gradle.properties` - Global JDK configuration
- `.setup_complete` - Timestamp marker (project root)
- `app/build/outputs/apk/debug/app-debug.apk` - Debug APK

## For AI Agents

When prompted to set up this project:

1. **First, check if already set up:**
   ```bash
   test -f .setup_complete && cat .setup_complete
   ```

2. **If not set up, run:**
   ```bash
   ./setup.sh
   ```

3. **If already set up, just report:**
   ```
   ✅ Project already configured on [date]
   Ready to build: ./gradlew assembleDebug
   ```

The setup script is designed to be run by both humans and AI agents. It will:
- ✅ Detect existing setup and skip unnecessary steps
- ✅ Report what's already configured
- ✅ Only make changes if needed
- ✅ Leave clear markers of what was done

---

**Last Updated**: November 2025
**Status**: Production ready
