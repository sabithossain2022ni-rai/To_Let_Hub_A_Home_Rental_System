@echo off
cd /d "%~dp0"
if exist "jre\bin\javaw.exe" (
  start "" "jre\bin\javaw.exe" -cp "app" com.tolet.Main
) else (
  echo Bundled Java not found. Install a JDK from https://adoptium.net and use run.bat instead.
  pause
)
