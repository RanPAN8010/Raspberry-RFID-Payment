#!/bin/bash

# 2026年网络服务项目 - 一键安装脚本
echo "-------------------------------------------------------"
echo "正在启动 Raspberry RFID 支付系统部署程序..."
echo "项目目标：模拟无接触支付 [cite: 2]"
echo "-------------------------------------------------------"

# 1. 检查并创建数据持久化目录
if [ ! -d "data" ]; then
    echo "[1/3] 正在创建数据存储目录 'data'..."
    mkdir -p data
else
    echo "[1/3] 数据存储目录已存在。"
fi

# 2. 停止并移除可能存在的旧容器，确保环境干净
echo "[2/3] 正在清理旧容器（如果存在）..."
docker-compose down > /dev/null 2>&1

# 3. 使用 Docker Compose 构建镜像并以守护进程模式启动
echo "[3/3] 正在通过 Docker 构建并启动系统..."
# --build 确保它会读取最新的 Java 代码更改
docker-compose up --build -d

if [ $? -eq 0 ]; then
    echo "-------------------------------------------------------"
    echo "部署成功！"
    echo "应用现在运行在：http://localhost:8080"
    echo "数据库文件位置：./data/payment_system.db"
    echo "提示：请确保 RFID 读卡器已连接 [cite: 3]"
    echo "-------------------------------------------------------"
else
    echo "错误：部署过程中出现问题，请检查 Docker 是否已启动。"
fi