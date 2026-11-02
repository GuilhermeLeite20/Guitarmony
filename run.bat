@echo off
setlocal DisableDelayedExpansion
cd /d "%~dp0"
if not exist dist\Guitarmony.jar (
  call build.bat
  if errorlevel 1 exit /b 1
)
java -jar dist\Guitarmony.jar %*
exit /b %errorlevel%
