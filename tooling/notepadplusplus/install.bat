@echo off
rem Installs the Janitor syntax highlighting for Notepad++ for the current user.
setlocal
set "TARGET=%APPDATA%\Notepad++\userDefineLangs"
if not exist "%TARGET%" mkdir "%TARGET%"
copy /Y "%~dp0Janitor.udl.xml" "%TARGET%\" >nul
if errorlevel 1 (
    echo Could not copy Janitor.udl.xml to "%TARGET%".
    pause
    exit /b 1
)
echo Installed Janitor.udl.xml to "%TARGET%".
echo Restart Notepad++ to use it.
pause
