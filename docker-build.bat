@echo off
REM Script untuk build Docker image Music Sheet Builder

echo ========================================
echo Building Music Sheet Builder Docker Image
echo ========================================
echo.

REM Check if Docker is running
docker info >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker is not running!
    echo Please start Docker Desktop and try again.
    pause
    exit /b 1
)

REM Build image
echo Building image...
docker build -t music-sheet-builder:latest .

if errorlevel 1 (
    echo.
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo Build completed successfully!
echo ========================================
echo.
echo Image: music-sheet-builder:latest
echo.
echo To run the application:
echo   docker-compose up -d
echo.
echo Or:
echo   docker run -d -p 8080:8080 --name music-sheet-builder music-sheet-builder:latest
echo.
pause
