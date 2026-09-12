# AliMedia – Sri Lankan Elephants & Tuskers App

Android (Jetpack Compose) client for the AliMedia platform.  
Syncs with Firebase Realtime Database + Cloudinary for photos.

## Prerequisites

- **Android Studio** (Ladybug / Meerkat or newer recommended)
- JDK 17+
- Internet (for Firebase + Cloudinary)

## Quick Start (Android Studio)

1. Clone / download this repository.
2. Open the project folder in Android Studio.
3. Let Gradle sync (it will download dependencies).
4. (Optional) Create a `.env` file in the **project root** if you need extra secrets:
   ```
   # GEMINI_API_KEY=your_key_here
   ```
5. Run the app on an emulator or device (Debug configuration).

> The app already contains a working `debug.keystore`.  
> Release builds require a proper upload keystore + GitHub Secrets (see below).

## Build APK locally

```bash
# Debug APK
./gradlew assembleDebug
# → app/build/outputs/apk/debug/app-debug.apk

# Release APK (needs keystore env vars)
export KEYSTORE_PATH=/path/to/my-upload-key.jks
export STORE_PASSWORD=...
export KEY_PASSWORD=...
./gradlew assembleRelease
```

## GitHub Actions (CI)

The workflow `.github/workflows/android-build.yml` builds a debug APK on every push to `main`.

To build a **signed release** APK:

1. Create a GitHub repository and push this project.
2. In repo Settings → Secrets and variables → Actions, add:
   - `KEYSTORE_BASE64` – base64-encoded `.jks` file (`base64 -w0 my-upload-key.jks`)
   - `STORE_PASSWORD`
   - `KEY_PASSWORD`
3. Go to Actions → Android Build → Run workflow (workflow_dispatch).

The release artifact will appear under the workflow run.

## Project structure

```
app/
  src/main/java/com/example/
    MainActivity.kt          # Entry + navigation
    data/
      model/                 # Elephant, Post, Story, Notice, UserProfile…
      remote/                # Firebase Auth, RTDB (REST), Cloudinary
      repository/            # AliMediaRepository (single source of truth)
      locale/                # English + Sinhala strings
    ui/
      screens/               # Feed, Elephants, Create, Notices, Profile…
      components/            # Cards, BottomBar, Story tray…
      theme/                 # Colors, Typography, Theme
```

## Firebase

- Project: `aliapp-e5196`
- Realtime Database rules are in `database.rules.json`
- The Android app uses **anonymous Auth** so write rules (`auth != null`) are satisfied.
- Package name / applicationId: `com.aistudio.alimedia.tskr`

## Notes / Fixes applied

- Secrets Gradle plugin now finds `.env.example` at project root.
- Debug / release signing is conditional so local builds never fail because of missing keystore.
- Unused Room + Moshi dependencies & KSP processors removed (they were not used).
- Added proper `.gitignore` for Android / Gradle / secrets.

If you still see Gradle errors after opening in Android Studio, try:

```bash
./gradlew --stop
./gradlew clean
```

Then File → Invalidate Caches / Restart in Android Studio.
