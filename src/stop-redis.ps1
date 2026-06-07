# Redis 关闭脚本 (PowerShell 版本)
# 文件: stop-redis.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "       Redis 关闭脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Redis 安装路径
$RedisPath = "D:\Redis-x64-3.2.100"
$RedisCli = "$RedisPath\redis-cli.exe"

# 1. 检测 Redis 是否在运行
Write-Host "[1/2] 检测 Redis 运行状态..." -ForegroundColor Yellow
try {
    $result = & $RedisCli PING 2>$null
    if ($result -ne "PONG") {
        Write-Host "[!] Redis 未在运行，无需关闭" -ForegroundColor Yellow
        pause
        exit 0
    }
} catch {
    Write-Host "[!] Redis 未在运行，无需关闭" -ForegroundColor Yellow
    pause
    exit 0
}

# 2. 优雅关闭
Write-Host "[√] Redis 正在运行，尝试优雅关闭..." -ForegroundColor Green
Write-Host "[2/2] 发送 SHUTDOWN 命令..." -ForegroundColor Yellow

try {
    & $RedisCli SHUTDOWN 2>$null
    Start-Sleep -Seconds 1
    
    # 验证是否关闭
    try {
        $result = & $RedisCli PING 2>$null
        if ($result -eq "PONG") {
            # 优雅关闭失败，强制终止
            Write-Host "[!] 优雅关闭失败，尝试强制终止..." -ForegroundColor Yellow
            Stop-Process -Name "redis-server" -Force -ErrorAction SilentlyContinue
        }
    } catch {}
    
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "[√] Redis 已成功关闭" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
} catch {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "[×] 关闭失败，请手动终止 redis-server.exe" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
}

pause
exit 0