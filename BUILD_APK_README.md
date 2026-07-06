# Build Testing APK

I could not include a prebuilt APK inside this ZIP because the build environment used to prepare this package does not have Gradle/Android SDK installed. This package adds ready build scripts that download Gradle on your PC and run the Android debug build.

## Windows

Double-click:

```text
BUILD_APK_WINDOWS.bat
```

After success, APK will be here:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Android Studio

1. Open this folder in Android Studio.
2. Wait for Gradle sync.
3. Go to **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
4. APK path: `app/build/outputs/apk/debug/app-debug.apk`

## Mac/Linux

Run:

```bash
chmod +x BUILD_APK_MAC_LINUX.sh
./BUILD_APK_MAC_LINUX.sh
```

## Notes

- Internet is needed the first time to download Gradle dependencies.
- If build fails because Android SDK 36 is missing, open Android Studio → SDK Manager → install SDK Platform 36.
- This is a debug APK for testing, not Play Store release APK/AAB.
