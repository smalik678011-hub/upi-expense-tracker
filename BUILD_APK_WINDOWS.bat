@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

set GRADLE_VERSION=9.1.0
set GRADLE_DIR=%CD%\.gradle-local\gradle-%GRADLE_VERSION%
set GRADLE_ZIP=%CD%\.gradle-local\gradle-%GRADLE_VERSION%-bin.zip

if not exist ".gradle-local" mkdir ".gradle-local"

REM Try Android Studio bundled Java first.
if exist "C:\Program Files\Android\Android Studio\jbr" set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
if exist "C:\Program Files\Android\Android Studio\jre" set JAVA_HOME=C:\Program Files\Android\Android Studio\jre

if not exist "%GRADLE_DIR%\bin\gradle.bat" (
  echo Downloading Gradle %GRADLE_VERSION%...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%GRADLE_ZIP%'"
  if errorlevel 1 (
    echo Gradle download failed. Check internet connection.
    pause
    exit /b 1
  )
  echo Extracting Gradle...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%GRADLE_ZIP%' -DestinationPath '%CD%\.gradle-local' -Force"
)

echo Building debug APK...
"%GRADLE_DIR%\bin\gradle.bat" assembleDebug --stacktrace
if errorlevel 1 (
  echo Build failed. Open this project in Android Studio and let it sync, then try again.
  pause
  exit /b 1
)

echo.
echo APK ready:
echo %CD%\app\build\outputs\apk\debug\app-debug.apk
pause
