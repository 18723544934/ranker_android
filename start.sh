#!/bin/bash

echo "========================================"
echo "PerfTop 项目启动脚本"
echo "========================================"
echo ""

# Check Python
echo "[1/4] 检查环境..."
if ! command -v python &> /dev/null; then
    echo "[错误] 未找到 Python，请先安装 Python 3.11+"
    exit 1
fi

# Check Java
if ! command -v java &> /dev/null; then
    echo "[错误] 未找到 Java，请先安装 JDK 17"
    exit 1
fi

echo "[完成] 环境检查通过"
echo ""

# Start backend services
echo "[2/4] 启动后端服务..."
cd backend

if [ ! -f .env ]; then
    echo "[提示] 复制 .env.example 到 .env"
    cp .env.example .env
fi

echo "[启动] Docker Compose 服务..."
docker-compose up -d
if [ $? -ne 0 ]; then
    echo "[错误] Docker 服务启动失败"
    exit 1
fi

echo "[等待] 等待服务启动..."
sleep 30
echo "[完成] 后端服务已启动"
echo ""

echo "[3/4] 等待数据库就绪..."
sleep 15
echo "[完成] 数据库已就绪"
echo ""

echo "[4/4] 启动 Android 客户端..."
echo ""
echo "[信息] 请在 Android Studio 中打开项目并运行"
echo "[信息] 后端 API: http://localhost:8000"
echo "[信息] API 文档: http://localhost:8000/docs"
echo ""
echo "========================================"
echo "项目启动完成！"
echo "========================================"
echo ""
echo "可用命令:"
echo "  - 停止后端: docker-compose down"
echo "  - 查看日志: docker-compose logs -f"
echo "  - 重启后端: docker-compose restart"
echo ""
