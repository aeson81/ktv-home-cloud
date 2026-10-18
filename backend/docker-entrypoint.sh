#!/bin/sh
# KTV 容器启动入口：先挂载 alist WebDAV 为 /cloud-music（网盘只读源），再启动 Java 服务。
# 挂载在容器内部完成，不依赖跨容器的 bind propagation。
#
# 密码解耦设计：rclone 不直接用 admin 账号，而是通过 alist API 创建独立的
# 只读用户 `rclone`（密码固定）。用户在 alist 网页端修改 admin 密码不会影响挂载。
set -eu

RCLONE_USER="rclone"
RCLONE_PASS="KtvRclone2026ReadOnly"

if [ -n "${KTV_ALIST_PASSWORD:-}" ]; then
  ALIST_HOST="${KTV_ALIST_HOST:-http://alist:5244}"
  ALIST_URL="${KTV_ALIST_URL:-http://alist:5244/dav}"
  MOUNT_POINT="/cloud-music"
  echo "[init] Preparing alist rclone user..."

  # 等待 alist 就绪（最多 60 秒）。
  READY=0
  for i in $(seq 1 30); do
    if wget -qO- "$ALIST_HOST/api/public/settings" >/dev/null 2>&1; then
      READY=1
      break
    fi
    sleep 2
  done

  if [ "$READY" = "1" ]; then
    # 用 admin 登录拿 token。
    TOKEN="$(wget -qO- --header="Content-Type: application/json" \
      --post-data="{\"username\":\"admin\",\"password\":\"$KTV_ALIST_PASSWORD\"}" \
      "$ALIST_HOST/api/auth/login" 2>/dev/null | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')"
    if [ -n "$TOKEN" ]; then
      # 查 rclone 用户是否存在，不存在则创建（只读权限）。
      EXISTS="$(wget -qO- --header="Authorization: $TOKEN" \
        "$ALIST_HOST/api/admin/user/list" 2>/dev/null | grep -c "\"username\":\"$RCLONE_USER\"" || true)"
      if [ "$EXISTS" = "0" ]; then
        echo "[init] Creating alist user '$RCLONE_USER'..."
        wget -qO- --header="Content-Type: application/json" \
          --header="Authorization: $TOKEN" \
          --post-data="{\"username\":\"$RCLONE_USER\",\"password\":\"$RCLONE_PASS\",\"base_path\":\"/\",\"role\":0,\"disabled\":false,\"permission\":1}" \
          "$ALIST_HOST/api/admin/user/create" >/dev/null 2>&1 || \
          echo "[init] WARNING: failed to create rclone user, falling back to admin"
      fi
    else
      echo "[init] WARNING: alist admin login failed, falling back to admin for mount"
    fi
  else
    echo "[init] WARNING: alist not ready, falling back to admin for mount"
  fi

  # 决定挂载用哪个账号：如果 rclone 用户创建成功就用它，否则回退到 admin。
  MOUNT_USER="$RCLONE_USER"
  MOUNT_PASS="$RCLONE_PASS"
  # 简单探测：如果 rclone 用户登录失败，回退到 admin。
  if ! wget -qO- --header="Content-Type: application/json" \
    --post-data="{\"username\":\"$RCLONE_USER\",\"password\":\"$RCLONE_PASS\"}" \
    "$ALIST_HOST/api/auth/login" 2>/dev/null | grep -q '"code":200'; then
    echo "[init] rclone user not usable, using admin for mount"
    MOUNT_USER="admin"
    MOUNT_PASS="$KTV_ALIST_PASSWORD"
  fi

  echo "[init] Mounting alist WebDAV $ALIST_URL -> $MOUNT_POINT (user: $MOUNT_USER)"
  mkdir -p "$MOUNT_POINT" /root/.config/rclone
  # 清理上次残留的挂载（容器重启时）。
  fusermount -uz "$MOUNT_POINT" 2>/dev/null || umount -l "$MOUNT_POINT" 2>/dev/null || true
  OBSCURED="$(rclone obscure "$MOUNT_PASS")"
  cat > /root/.config/rclone/rclone.conf <<EOF
[alist]
type = webdav
url = $ALIST_URL
vendor = other
user = $MOUNT_USER
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
