@echo off
REM Script untuk menghentikan Music Sheet Builder

echo ========================================
echo Stopping Music Sheet Builder
echo ========================================
echo.

REM Check if Docker is running
docker info >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker is not running!
    pause
    exit /b 1
)

REM Stop services
echo Stopping services...
docker-compose down

if errorlevel 1 (
    echo.
    echo ERROR: Failed to stop services!
    pause
    exit /b 1
)

echo.
echo ========================================
echo Application stopped successfully!
echo ========================================
echo.
pause
