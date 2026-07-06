#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

GRADLE_VERSION="9.1.0"
GRADLE_DIR="$PWD/.gradle-local/gradle-$GRADLE_VERSION"
GRADLE_ZIP="$PWD/.gradle-local/gradle-$GRADLE_VERSION-bin.zip"
mkdir -p "$PWD/.gradle-local"

if [ ! -x "$GRADLE_DIR/bin/gradle" ]; then
  echo "Downloading Gradle $GRADLE_VERSION..."
  if command -v curl >/dev/null 2>&1; then
    curl -L "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o "$GRADLE_ZIP"
  elif command -v wget >/dev/null 2>&1; then
    wget "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -O "$GRADLE_ZIP"
  else
    echo "curl/wget missing. Install one of them or open project in Android Studio."
    exit 1
  fi
  echo "Extracting Gradle..."
  unzip -o "$GRADLE_ZIP" -d "$PWD/.gradle-local" >/dev/null
fi

echo "Building debug APK..."
"$GRADLE_DIR/bin/gradle" assembleDebug --stacktrace

echo ""
echo "APK ready: $PWD/app/build/outputs/apk/debug/app-debug.apk"
