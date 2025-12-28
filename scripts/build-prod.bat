@echo off
setlocal enabledelayedexpansion
REM Build production frontend projects (Windows)
REM Usage: scripts\build-prod.bat

REM Auto switch to project root directory
cd /d "%~dp0\.."
set "PROJECT_ROOT=%CD%"

REM Set Node.js memory limit (4GB)
set NODE_OPTIONS=--max-old-space-size=4096

echo ==========================================
echo Building Frontend Projects (Production)
echo ==========================================
echo Project Root: %PROJECT_ROOT%
echo Node.js Memory Limit: 4GB
echo.

REM Check if in project root directory
if not exist "frontend" (
    echo Error: frontend directory not found
    exit /b 1
)
if not exist "admin-frontend" (
    echo Error: admin-frontend directory not found
    exit /b 1
)

REM Build production environment
echo ^>^>^> Building User Frontend (Production)
cd frontend
call npm install
if errorlevel 1 (
    echo Error: npm install failed
    cd ..
    exit /b 1
)
set VITE_BUILD_ENV=production
set VITE_API_BASE_URL=
call npm run build -- --mode production
if errorlevel 1 (
    echo Error: User frontend build failed
    set VITE_BUILD_ENV=
    set VITE_API_BASE_URL=
    cd ..
    exit /b 1
)
set VITE_BUILD_ENV=
set VITE_API_BASE_URL=
cd ..

echo ^>^>^> Building Admin Frontend (Production)
cd admin-frontend
call npm install
if errorlevel 1 (
    echo Error: npm install failed
    cd ..
    exit /b 1
)
set VITE_BUILD_ENV=production
set VITE_API_BASE_URL=
call npm run build -- --mode production
if errorlevel 1 (
    echo Error: Admin frontend build failed
    set VITE_BUILD_ENV=
    set VITE_API_BASE_URL=
    cd ..
    exit /b 1
)
set VITE_BUILD_ENV=
set VITE_API_BASE_URL=
cd ..

echo.
echo ==========================================
echo Production Build Completed!
echo ==========================================
echo.
echo Build Output Locations:
echo   User Frontend: frontend\dist-prod\
echo   Admin Frontend: admin-frontend\dist-prod\
echo.
endlocal
pause
