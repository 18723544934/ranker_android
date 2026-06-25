@echo off
echo ========================================
echo PerfTop 项目启动脚本
echo ========================================
echo.

echo [1/4] 检查环境...
where python >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 未找到 Python，请先安装 Python 3.11+
    pause
    exit /b 1
)

where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 未找到 Java，请先安装 JDK 17
    pause
    exit /b 1
)

echo [完成] 环境检查通过
echo.

echo [2/4] 启动后端服务...
cd backend
if not exist .env (
    echo [提示] 复制 .env.example 到 .env
    copy .env.example .env
)

echo [启动] Docker Compose 服务...
docker-compose up -d
if %ERRORLEVEL% NEQ 0 (
    echo [错误] Docker 服务启动失败
    pause
    exit /b 1
)

echo [等待] 等待服务启动...
timeout /t 30 /nobreak >nul
echo [完成] 后端服务已启动
echo.

echo [3/4] 等待数据库就绪...
timeout /t 15 /nobreak >nul
echo [完成] 数据库已就绪
echo.

echo [4/4] 启动 Android 客户端...
echo.
echo [信息] 请在 Android Studio 中打开项目并运行
echo [信息] 后端 API: http://localhost:8000
echo [信息] API 文档: http://localhost:8000/docs
echo.
echo ========================================
echo 项目启动完成！
echo ========================================
echo.
echo 可用命令:
echo   - 停止后端: docker-compose down
echo   - 查看日志: docker-compose logs -f
echo   - 重启后端: docker-compose restart
echo.
pause
