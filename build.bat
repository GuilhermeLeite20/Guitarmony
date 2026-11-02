@echo off
setlocal DisableDelayedExpansion
chcp 65001 >nul
cd /d "%~dp0"
where java >nul 2>nul
if errorlevel 1 (
  echo Instale o JDK 17 ou superior e inclua java no PATH.
  exit /b 1
)
set "COMPILER=javac -J-Dfile.encoding=UTF-8"
where javac >nul 2>nul
if errorlevel 1 set "COMPILER=java -Dfile.encoding=UTF-8 -m jdk.compiler/com.sun.tools.javac.Main"
set "ARCHIVER=jar"
where jar >nul 2>nul
if errorlevel 1 set "ARCHIVER=java -m jdk.jartool/sun.tools.jar.Main"
if not exist build mkdir build
if not exist dist mkdir dist
if exist build\classes rmdir /s /q build\classes
mkdir build\classes
(for /r src\main\java %%F in (*.java) do (
  set "SOURCE=%%F"
  setlocal EnableDelayedExpansion
  echo "!SOURCE:\=/!"
  endlocal
)) > build\main-sources.txt
%COMPILER% --release 17 -encoding UTF-8 -d build\classes @build\main-sources.txt
if errorlevel 1 exit /b 1
%ARCHIVER% --create --file dist\Guitarmony.jar --main-class com.guitarmony.GuitarmonyApp -C build\classes .
if errorlevel 1 exit /b 1
echo Build concluido: dist\Guitarmony.jar
echo Execute: java -jar dist\Guitarmony.jar
