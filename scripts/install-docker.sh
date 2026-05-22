#!/usr/bin/env bash
# 按 Docker 官方文档安装 Engine（Debian apt 仓库）
# https://docs.docker.com/engine/install/debian/#install-using-the-repository

set -euo pipefail

if [ "$(id -u)" -ne 0 ]; then
  echo "请使用 sudo 运行此脚本，例如："
  echo "  sudo bash scripts/install-docker.sh"
  exit 1
fi

resolve_docker_codename() {
  local codename
  codename=$(. /etc/os-release && echo "${VERSION_CODENAME}")
  case "${codename}" in
    forky|sid|testing)
      echo "trixie"
      ;;
    *)
      echo "${codename}"
      ;;
  esac
}

echo "==> 1/6 清理旧 Docker apt 源（避免 forky 等无效代号导致 apt update 失败）"
rm -f /etc/apt/sources.list.d/docker.list
rm -f /etc/apt/sources.list.d/docker.sources
# 兼容旧版 one-line 格式
sed -i '\|download.docker.com/linux/debian|d' /etc/apt/sources.list 2>/dev/null || true

echo "==> 2/6 卸载可能冲突的旧 Docker 包"
apt-get remove -y docker.io docker-compose docker-doc podman-docker containerd runc 2>/dev/null || true

echo "==> 3/6 安装依赖并添加 Docker GPG key"
apt-get update
apt-get install -y ca-certificates curl
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc
chmod a+r /etc/apt/keyrings/docker.asc

echo "==> 4/6 配置 Docker apt 源"
DOCKER_CODENAME=$(resolve_docker_codename)
if [ "$(. /etc/os-release && echo "${VERSION_CODENAME}")" != "${DOCKER_CODENAME}" ]; then
  echo "    系统代号: $(. /etc/os-release && echo "${VERSION_CODENAME}")，Docker 仓库使用: ${DOCKER_CODENAME}"
fi

tee /etc/apt/sources.list.d/docker.sources > /dev/null <<EOF
Types: deb
URIs: https://download.docker.com/linux/debian
Suites: ${DOCKER_CODENAME}
Components: stable
Architectures: $(dpkg --print-architecture)
Signed-By: /etc/apt/keyrings/docker.asc
EOF

echo "==> 5/6 安装 Docker Engine"
apt-get update
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

echo "==> 6/6 启动 Docker 并验证"
systemctl enable --now docker
docker run --rm hello-world

echo
echo "安装成功："
docker --version
docker compose version

SUDO_USER_NAME="${SUDO_USER:-}"
if [ -n "${SUDO_USER_NAME}" ] && [ "${SUDO_USER_NAME}" != "root" ]; then
  echo
  echo "==> 将用户 ${SUDO_USER_NAME} 加入 docker 组（免 sudo 运行 docker）"
  usermod -aG docker "${SUDO_USER_NAME}"
  echo "已加入 docker 组。请注销并重新登录，或执行： newgrp docker"
fi
