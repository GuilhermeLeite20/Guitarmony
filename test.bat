@echo off
setlocal DisableDelayedExpansion
chcp 65001 >nul
cd /d "%~dp0"
call build.bat
if errorlevel 1 exit /b 1
set "COMPILER=javac -J-Dfile.encoding=UTF-8"
where javac >nul 2>nul
if errorlevel 1 set "COMPILER=java -Dfile.encoding=UTF-8 -m jdk.compiler/com.sun.tools.javac.Main"
if exist build\test-classes rmdir /s /q build\test-classes
mkdir build\test-classes
(for /r src\test\java %%F in (*.java) do (
  set "SOURCE=%%F"
  setlocal EnableDelayedExpansion
  echo "!SOURCE:\=/!"
  endlocal
)) > build\test-sources.txt
%COMPILER% --release 17 -encoding UTF-8 -cp build\classes -d build\test-classes @build\test-sources.txt
if errorlevel 1 exit /b 1
java -Djava.awt.headless=true -cp "build\classes;build\test-classes" com.guitarmony.TheoryChecks
if errorlevel 1 exit /b 1
java -Djava.awt.headless=true -cp "build\classes;build\test-classes" com.guitarmony.UiSmokeTest
exit /b %errorlevel%
