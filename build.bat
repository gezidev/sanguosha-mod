@echo off
rem ============================================================
rem  Build the decompiled SanguoshaForge mod (Forge 1.20.1)
rem  Requires: JDK 17 on PATH, Python 3 on PATH
rem ============================================================
cd /d "%~dp0"
python build.py
if errorlevel 1 (
  echo.
  echo Build failed. See messages above.
  pause
  exit /b 1
)
echo.
echo Build OK. Jar is in build\libs\SanguoshaForge-rebuilt.jar
pause
