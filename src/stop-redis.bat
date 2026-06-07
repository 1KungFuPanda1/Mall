@echo off
chcp 65001 >nul
title Redis 关闭脚本

echo ========================================
echo        Redis 关闭脚本
echo ========================================
echo.

:: 1. 检测 Redis 是否在运行
echo [1/2] 检测 Redis 运行状态...
redis-cli PING >nul 2>&1
if %errorlevel% neq 0 (
    echo [!] Redis 未在运行，无需关闭
    pause
    exit /b 0
)

:: 2. 尝试优雅关闭
echo [√] Redis 正在运行，尝试优雅关闭...
echo [2/2] 发送 SHUTDOWN 命令...
redis-cli SHUTDOWN >nul 2>&1

:: 等待关闭
timeout /t 1 /nobreak >nul

:: 验证是否关闭成功
redis-cli PING >nul 2>&1
if %errorlevel% neq 0 (
    echo.
    echo ========================================
    echo [√] Redis 已成功关闭
    echo ========================================
    pause
    exit /b 0
)

:: 优雅关闭失败，尝试强制终止
echo [!] 优雅关闭失败，尝试强制终止进程...
taskkill /f /im redis-server.exe >nul 2>&1
if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo [√] Redis 进程已强制终止
    echo ========================================
) else (
    echo.
    echo ========================================
    echo [×] 关闭失败，请手动终止 redis-server.exe
    echo ========================================
)

pause
exit /b 0