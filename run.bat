@echo off
title ToLet Hub
cd /d "%~dp0"
where javac >nul 2>nul
if errorlevel 1 (
  echo.
  echo  Java JDK was not found. Please install JDK 17 or newer from https://adoptium.net
  echo  then run this file again.
  pause
  exit /b 1
)
if not exist out mkdir out
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d out @sources.txt
if errorlevel 1 (
  echo Compilation failed.
  pause
  exit /b 1
)
start "" javaw -cp out com.tolet.Main
