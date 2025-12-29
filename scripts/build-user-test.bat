@echo off
chcp 65001 >nul
REM 构建用户端测试环境（Windows版本）
REM 使用方法: scripts\build-user-test.bat 或从任何目录执行

REM 自动切换到项目根目录
cd /d "%~dp0\.."
set "PROJECT_ROOT=%CD%"

REM 设置 Node.js 内存限制（4GB）
set "NODE_OPTIONS=--max-old-space-size=4096"

echo ==========================================
echo 开始构建用户端（测试环境）
echo ==========================================
echo 项目根目录: %PROJECT_ROOT%
echo Node.js 内存限制: 4GB
echo.

REM 检查是否在项目根目录
if not exist "frontend" (
    echo 错误: 未找到 frontend 目录，请确认脚本位置正确
    exit /b 1
)

REM 构建用户端测试环境
echo ^>^>^> 用户端（测试环境）
cd frontend
call npm install
if errorlevel 1 (
    echo 错误: npm install 失败
    cd ..
    exit /b 1
)
set "VITE_BUILD_ENV=test"
set "VITE_API_BASE_URL=/test"
call npm run build -- --mode test
set "BUILD_ERROR=%ERRORLEVEL%"
set "VITE_BUILD_ENV="
set "VITE_API_BASE_URL="
if %BUILD_ERROR% neq 0 (
    echo 错误: 用户端构建失败
    cd ..
    exit /b 1
)
cd ..

REM 验证构建产物是否存在
if not exist "frontend\dist-test" (
    echo 错误: 未找到 frontend\dist-test 目录，构建失败
    exit /b 1
)

echo.
echo ==========================================
echo 用户端测试环境构建完成！
echo ==========================================
echo.
echo 构建产物位置：
echo   用户端: frontend\dist-test\
echo.
pause


