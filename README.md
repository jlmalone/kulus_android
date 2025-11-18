# Kulus Android

Glucose monitoring mobile app with Matrix-themed UI.

## Quick Start - Just Hit Play! ▶️

### First Time Setup

```bash
./setup.sh
```

This idempotent script will configure everything automatically. Safe to run multiple times.

### After Setup

1. **Open in Android Studio** - The IDE is configured to use JDK 17 automatically
2. **Hit the Play button** ▶️ - It should just work!
3. **Select your device/emulator** and go

## If Build Fails

If you see JDK-related errors when hitting play:

```bash
# Run this in Terminal:
./build.sh
```

Or manually:
```bash
./gradlew --stop
./gradlew clean assembleDebug
```

Then try hitting play again in Android Studio.

## Project Status

✅ **Ready to run!**
- Core architecture complete
- Data layer implemented
- Theme and styling configured
- Builds successfully

🚧 **UI Screens needed** - See CLAUDE.md for details

## Key Files

- `CLAUDE.md` - Full documentation for developers and AI agents
- `build.sh` - One-command build script
- `gradle.properties` - JDK 17 configuration

## API Configuration

The app connects to: **https://kulus.org**
- API Key: `kulus-unified-api-key-2025`
- Password: `kulus2025`

All configured in BuildConfig - no additional setup needed.

## Build Output

APK location: `app/build/outputs/apk/debug/app-debug.apk`

---

**Status**: November 2025 - Core complete, ready for UI development
