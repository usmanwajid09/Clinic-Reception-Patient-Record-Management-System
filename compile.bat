@echo off
setlocal
echo ========================================================
echo Compiling Clinic Reception Management Desktop Application
echo ========================================================

if not exist bin mkdir bin

set JAVAC_BIN=javac
if exist "C:\Program Files\Microsoft\jdk-21.0.13.21-hotspot\bin\javac.exe" (
    set "JAVAC_BIN=C:\Program Files\Microsoft\jdk-21.0.13.21-hotspot\bin\javac.exe"
)

powershell -NoProfile -Command "(Get-ChildItem -Path src -Recurse -Filter *.java).FullName.Replace('\', '/') | ForEach-Object { '\"' + $_ + '\"' } | Set-Content sources.txt -Encoding ascii"

"%JAVAC_BIN%" -encoding UTF-8 -cp "lib/*;bin" -d bin @sources.txt
set COMPILE_ERR=%ERRORLEVEL%
if exist sources.txt del sources.txt

if %COMPILE_ERR% EQU 0 (
    if not exist bin\com\clinic\ui\icons mkdir bin\com\clinic\ui\icons
    copy /Y src\com\clinic\ui\icons\*.png bin\com\clinic\ui\icons\ >nul 2>nul
    echo [SUCCESS] Compilation finished successfully. Classes output to 'bin/'.
) else (
    echo [ERROR] Compilation failed with error code %COMPILE_ERR%.
)
endlocal
