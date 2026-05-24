@echo off
REM Script untuk menjalankan Music Sheet Builder dengan Docker Compose

echo ========================================
echo Starting Music Sheet Builder
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

REM Check if docker-compose is available
docker-compose version >nul 2>&1
if errorlevel 1 (
    echo ERROR: docker-compose is not available!
    echo Please install Docker Compose and try again.
    pause
    exit /b 1
)

REM Start services
echo Starting services...
docker-compose up -d

if errorlevel 1 (
    echo.
    echo ERROR: Failed to start services!
    pause
    exit /b 1
)

echo.
echo ========================================
echo Application started successfully!
echo ========================================
echo.
echo Application URL: http://localhost:8080
echo.
echo Useful commands:
echo   View logs:    docker-compose logs -f
echo   Stop app:     docker-compose down
echo   Restart app:  docker-compose restart
echo.
echo Opening browser in 3 seconds...
timeout /t 3 /nobreak >nul
start http://localhost:8080
