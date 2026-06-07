@echo off
chcp 65001 >nul
title Redis 快速启动脚本

echo ========================================
echo        Redis 快速启动脚本
echo ========================================
echo.

:: 1. 检测 Redis 是否已在运行
echo [1/4] 检测 Redis 是否已在运行...
redis-cli PING >nul 2>&1
if %errorlevel% equ 0 (
    echo [√] Redis 已在运行中，无需重复启动
    goto :success
)

echo [!] Redis 未运行，开始查找 Redis 安装路径...

:: 2. 查找 redis-server.exe
set REDIS_SERVER=

:: 优先级1: D:\Redis-x64-3.2.100\ (用户实际安装路径)
if exist "D:\Redis-x64-3.2.100\redis-server.exe" (
    set REDIS_SERVER=D:\Redis-x64-3.2.100\redis-server.exe
    goto :found
)

:: 优先级2: C:\Program Files\Redis\
if exist "C:\Program Files\Redis\redis-server.exe" (
    set REDIS_SERVER=C:\Program Files\Redis\redis-server.exe
    goto :found
)

:: 优先级2: D:\Program Files\Redis\
if exist "D:\Program Files\Redis\redis-server.exe" (
    set REDIS_SERVER=D:\Program Files\Redis\redis-server.exe
    goto :found
)

:: 优先级3: C:\Redis\
if exist "C:\Redis\redis-server.exe" (
    set REDIS_SERVER=C:\Redis\redis-server.exe
    goto :found
)

:: 优先级4: D:\Redis\
if exist "D:\Redis\redis-server.exe" (
    set REDIS_SERVER=D:\Redis\redis-server.exe
    goto :found
)

:: 优先级5: 系统PATH中查找
for /f "tokens=*" %%i in ('where redis-server.exe 2^>nul') do (
    set REDIS_SERVER=%%i
    goto :found
)

:: 未找到 Redis
echo.
echo [×] 未找到 Redis 安装！请先安装 Redis：
echo.
echo   方式一（推荐）：使用 winget 安装
echo     winget install Redis
echo.
echo   方式二：手动下载安装
echo     访问 https://github.com/tporadowski/redis/releases
echo     下载 Redis-x64-*.msi 并安装
echo.
pause
exit /b 1

:found
echo [√] 找到 Redis: %REDIS_SERVER%

:: 3. 启动 Redis
echo.
echo [2/4] 启动 Redis 服务器（端口 6379）...
start "Redis-6379" /min "%REDIS_SERVER%" --port 6379

:: 4. 等待启动并验证
echo [3/4] 等待 Redis 启动...
timeout /t 2 /nobreak >nul

echo [4/4] 验证 Redis 连接...
redis-cli PING >nul 2>&1
if %errorlevel% equ 0 (
    goto :success
) else (
    goto :fail
)

:success
echo.
echo ========================================
echo [√] Redis 启动成功！
echo     端口: 6379
echo     密码: 无
echo ========================================
echo.
echo 提示：此窗口可关闭，Redis 将在后台运行
echo       如需关闭 Redis，请运行 stop-redis.bat
echo.
pause
exit /b 0

:fail
echo.
echo ========================================
echo [×] Redis 启动失败！
echo ========================================
echo.
echo 可能原因：
echo   1. 端口 6379 已被其他程序占用
echo   2. Redis 配置文件有误
echo.
echo 排查命令：
echo   netstat -ano ^| findstr :6379
echo.
pause
exit /b 1