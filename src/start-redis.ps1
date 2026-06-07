# Redis 快速启动脚本 (PowerShell 版本)
# 文件: start-redis.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "       Redis 快速启动脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Redis 安装路径
$RedisPath = "D:\Redis-x64-3.2.100"
$RedisServer = "$RedisPath\redis-server.exe"
$RedisCli = "$RedisPath\redis-cli.exe"

# 1. 检测 Redis 是否已在运行
Write-Host "[1/4] 检测 Redis 是否已在运行..." -ForegroundColor Yellow
try {
    $result = & $RedisCli PING 2>$null
    if ($result -eq "PONG") {
        Write-Host "[√] Redis 已在运行中，无需重复启动" -ForegroundColor Green
        Write-Host ""
        Write-Host "提示：此窗口可关闭，Redis 将在后台运行" -ForegroundColor Gray
        pause
        exit 0
    }
} catch {}

Write-Host "[!] Redis 未运行，开始启动..." -ForegroundColor Yellow

# 2. 检查 redis-server.exe 是否存在
Write-Host "[2/4] 检查 Redis 安装路径..." -ForegroundColor Yellow
if (-not (Test-Path $RedisServer)) {
    Write-Host "[×] 未找到 Redis: $RedisServer" -ForegroundColor Red
    Write-Host ""
    Write-Host "请先安装 Redis:" -ForegroundColor Yellow
    Write-Host "  winget install Redis" -ForegroundColor White
    pause
    exit 1
}
Write-Host "[√] 找到 Redis: $RedisServer" -ForegroundColor Green

# 3. 启动 Redis
Write-Host "[3/4] 启动 Redis 服务器（端口 6379）..." -ForegroundColor Yellow
Start-Process -FilePath $RedisServer -ArgumentList "--port 6379" -WindowStyle Minimized

# 4. 等待并验证
Write-Host "[4/4] 等待 Redis 启动并验证..." -ForegroundColor Yellow
Start-Sleep -Seconds 2

try {
    $result = & $RedisCli PING 2>$null
    if ($result -eq "PONG") {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "[√] Redis 启动成功！" -ForegroundColor Green
        Write-Host "    端口: 6379" -ForegroundColor White
        Write-Host "    密码: 无" -ForegroundColor White
        Write-Host "========================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "提示：此窗口可关闭，Redis 将在后台运行" -ForegroundColor Gray
        Write-Host "      如需关闭 Redis，请运行 stop-redis.ps1" -ForegroundColor Gray
        pause
        exit 0
    }
} catch {}

Write-Host ""
Write-Host "========================================" -ForegroundColor Red
Write-Host "[×] Redis 启动失败！" -ForegroundColor Red
Write-Host "========================================" -ForegroundColor Red
Write-Host ""
Write-Host "可能原因：端口 6379 已被占用" -ForegroundColor Yellow
Write-Host "排查命令：netstat -ano | findstr :6379" -ForegroundColor White
pause
exit 1