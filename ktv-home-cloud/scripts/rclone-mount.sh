#!/bin/sh
# cloud-mount sidecar entrypoint.
# 在容器启动时为内置 alist 生成 rclone WebDAV 配置，然后用 FUSE 把网盘挂载到 /mnt/cloud。
# alist 的 admin 密码通过环境变量 KTV_ALIST_PASSWORD 以明文传入，本脚本负责做 rclone obscure 加密。
set -eu

: "${KTV_ALIST_PASSWORD:?请设置 KTV_ALIST_PASSWORD（alist 的 admin 密码）}"
ALIST_URL="${KTV_ALIST_URL:-http://alist:5244/dav}"
MOUNT_POINT="${KTV_CLOUD_MOUNT_POINT:-/mnt/cloud}"
RCLONE_CONF="${RCLONE_CONFIG:-/config/rclone/rclone.conf}"

mkdir -p "$(dirname "$RCLONE_CONF")"
# rclone 的 WebDAV 密码必须经过 obscure 加密，不能写明文。
OBSCURED="$(rclone obscure "$KTV_ALIST_PASSWORD")"
cat > "$RCLONE_CONF" <<EOF
[alist]
type = webdav
url = $ALIST_URL
vendor = other
user = admin
pass = $OBSCURED
EOF
chmod 600 "$RCLONE_CONF"

echo "正在挂载网盘 WebDAV：$ALIST_URL -> $MOUNT_POINT"
exec rclone mount alist: "$MOUNT_POINT" \
  --allow-other \
  --vfs-cache-mode writes \
  --dir-cache-time 5m \
  --poll-interval 1m \
  --attr-timeout 1s \
  --transfers 4
