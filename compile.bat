@echo off
echo ========================================================
echo Compiling Clinic Reception Management Desktop Application
echo ========================================================

if not exist bin mkdir bin

dir /s /B src\*.java > sources.txt
javac -d bin @sources.txt
del sources.txt

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Compilation finished successfully. Classes output to 'bin/'.
) else (
    echo [ERROR] Compilation failed.
)
pause

