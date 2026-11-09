@echo off
setlocal
echo ========================================================
echo Running Automated Verification Suite (FR1 - FR30 and SOLID)
echo ========================================================

set JAVA_BIN=java
if exist "C:\Program Files\Microsoft\jdk-21.0.13.21-hotspot\bin\java.exe" (
    set "JAVA_BIN=C:\Program Files\Microsoft\jdk-21.0.13.21-hotspot\bin\java.exe"
)

"%JAVA_BIN%" -cp "lib/*;bin" com.clinic.test.TestRunner
endlocal
