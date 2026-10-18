#!/bin/sh
# KTV 容器启动入口：先挂载 alist WebDAV 为 /cloud-music（网盘只读源），再启动 Java 服务。
# 挂载在容器内部完成，不依赖跨容器的 bind propagation。
set -eu

if [ -n "${KTV_ALIST_PASSWORD:-}" ]; then
  ALIST_URL="${KTV_ALIST_URL:-http://alist:5244/dav}"
  MOUNT_POINT="/cloud-music"
  echo "[init] Mounting alist WebDAV $ALIST_URL -> $MOUNT_POINT"
  mkdir -p "$MOUNT_POINT" /root/.config/rclone
  # 清理上次残留的挂载（容器重启时）。
  fusermount -uz "$MOUNT_POINT" 2>/dev/null || umount -l "$MOUNT_POINT" 2>/dev/null || true
  OBSCURED="$(rclone obscure "$KTV_ALIST_PASSWORD")"
  cat > /root/.config/rclone/rclone.conf <<EOF
[alist]
type = webdav
url = $ALIST_URL
vendor = other
user = admin
pass = $OBSCURED
EOF
  chmod 600 /root/.config/rclone/rclone.conf
  # 后台挂载；即使失败也不阻塞主服务（降级为无网盘源）。
  if ! rclone mount alist: "$MOUNT_POINT" \
    --allow-other \
    --allow-non-empty \
    --vfs-cache-mode writes \
    --dir-cache-time 5m \
    --poll-interval 1m \
    --daemon; then
    echo "[init] WARNING: rclone mount failed, continuing without cloud source"
  fi
  sleep 2
else
  echo "[init] KTV_ALIST_PASSWORD not set, skipping cloud mount"
fi

exec java -jar /app/app.jar
