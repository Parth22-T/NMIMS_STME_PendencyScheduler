@echo off
set "PATH=C:\Program Files\Git\cmd;C:\Program Files\Git\bin;%PATH%"
echo ======================================================================
echo   NMIMS STME Dynamic Academic Pendency Scheduler - GitHub Push Helper
echo ======================================================================
echo.

where git >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Git is not installed or not in your PATH.
    echo Please install Git from https://git-scm.com/download/win or use GitHub Desktop.
    echo.
    pause
    exit /b 1
)

set /p GITHUB_USER="Enter your GitHub username: "
if "%GITHUB_USER%"=="" (
    echo [ERROR] GitHub username cannot be empty.
    pause
    exit /b 1
)

set /p REPO_NAME="Enter your GitHub repository name [default: NMIMS_STME_PendencyScheduler]: "
if "%REPO_NAME%"=="" set REPO_NAME=NMIMS_STME_PendencyScheduler

echo.
echo Initializing Git repository...
git init
git branch -M main

echo Adding files...
git add .

echo Committing...
git commit -m "feat: complete dynamic academic pendency scheduler with real-time multi-device sync"

echo Setting remote URL to https://github.com/%GITHUB_USER%/%REPO_NAME%.git ...
git remote remove origin 2>nul
git remote add origin https://github.com/%GITHUB_USER%/%REPO_NAME%.git

echo.
echo Pushing to GitHub...
git push -u origin main

if %errorlevel% equ 0 (
    echo.
    echo ======================================================================
    echo   SUCCESS! Project successfully published to:
    echo   https://github.com/%GITHUB_USER%/%REPO_NAME%
    echo ======================================================================
) else (
    echo.
    echo [NOTE] If the push failed, please make sure you created the empty
    echo repository on GitHub first at https://github.com/new with name: %REPO_NAME%
)

echo.
pause
