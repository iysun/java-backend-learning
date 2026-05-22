#!/usr/bin/env bash
# 配置 Docker 国内镜像加速（解决 docker.io 拉取超时）

set -euo pipefail

if [ "$(id -u)" -ne 0 ]; then
  echo "请使用 sudo 运行： sudo bash scripts/configure-docker-mirror.sh"
  exit 1
fi

mkdir -p /etc/docker

if [ -f /etc/docker/daemon.json ]; then
  cp /etc/docker/daemon.json /etc/docker/daemon.json.bak.$(date +%Y%m%d%H%M%S)
  echo "已备份原配置到 /etc/docker/daemon.json.bak.*"
fi

tee /etc/docker/daemon.json > /dev/null <<'EOF'
{
  "registry-mirrors": [
    "https://docker.1ms.run",
    "https://docker.m.daocloud.io"
  ]
}
EOF

echo "==> 重启 Docker 服务"
systemctl daemon-reload
systemctl restart docker

echo
echo "==> 当前镜像加速配置："
docker info | sed -n '/Registry Mirrors/,/^[^ ]/p' | head -10

echo
echo "配置完成。请重新执行："
echo "  cd /home/eason/projects/java-backend-learning"
echo "  docker compose up -d"
