#!/bin/sh
# KTV 容器启动入口：用 rclone 直连 115 网盘，挂载为 /cloud-music/115，再启动 Java 服务。
# 不再依赖 alist（alist 项目已被出售，改用 rclone 原生支持）。
set -eu

MOUNT_POINT="/cloud-music"
RCLONE_CONF="/root/.config/rclone/rclone.conf"

mkdir -p "$MOUNT_POINT" /root/.config/rclone
# 清理上次残留的挂载（容器重启时）。
fusermount -uz "$MOUNT_POINT" 2>/dev/null || umount -l "$MOUNT_POINT" 2>/dev/null || true

# 生成 rclone 配置。115 需要 Cookie（KTV_115_COOKIE 环境变量）。
# 格式：UID=xxx; CID=xxx; SEID=xxx（在浏览器登录 115 后按 F12 从请求头复制）。
cat > "$RCLONE_CONF" <<'CONFEOF'
# rclone 配置由 docker-entrypoint.sh 动态生成。
CONFEOF
chmod 600 "$RCLONE_CONF"

MOUNTED=0

if [ -n "${KTV_115_COOKIE:-}" ]; then
  echo "[init] Configuring rclone for 115..."
  # rclone obscure 会对特殊字符做处理，但 cookie 中的分号和等号需要原样保留，
  # 115 后端直接读取 cookie 字符串，此处不做 obscure。
  cat >> "$RCLONE_CONF" <<EOF
[115]
type = 115
cookie = ${KTV_115_COOKIE}
EOF
  echo "[init] Mounting 115 -> $MOUNT_POINT/115"
  mkdir -p "$MOUNT_POINT/115"
  fusermount -uz "$MOUNT_POINT/115" 2>/dev/null || umount -l "$MOUNT_POINT/115" 2>/dev/null || true
  if rclone mount "115:" "$MOUNT_POINT/115" \
    --allow-other \
    --allow-non-empty \
    --vfs-cache-mode writes \
    --dir-cache-time 5m \
    --poll-interval 1m \
    --daemon; then
    echo "[init] 115 mounted successfully"
    MOUNTED=1
  else
    echo "[init] WARNING: 115 mount failed (Cookie 可能过期，请更新 KTV_115_COOKIE)"
  fi
  sleep 2
else
  echo "[init] KTV_115_COOKIE not set, skipping 115 mount"
fi

if [ "$MOUNTED" = "0" ]; then
  echo "[init] No cloud source mounted, continuing with local source only"
fi

# OneDrive（rclone 原生支持，含全球/中国/德国/美国分区）。
if [ -n "${KTV_ONEDRIVE_TOKEN:-}" ]; then
  REGION="${KTV_ONEDRIVE_REGION:-global}"
  echo "[init] Configuring rclone for OneDrive (region: $REGION)..."
  # token 是 JSON，需转义后写入。rclone 的 obscure 对 token 不适用，直接明文存储（文件权限 600）。
  cat >> "$RCLONE_CONF" <<EOF
[onedrive]
type = onedrive
region = $REGION
token = ${KTV_ONEDRIVE_TOKEN}
EOF
  echo "[init] Mounting OneDrive -> $MOUNT_POINT/onedrive"
  mkdir -p "$MOUNT_POINT/onedrive"
  fusermount -uz "$MOUNT_POINT/onedrive" 2>/dev/null || umount -l "$MOUNT_POINT/onedrive" 2>/dev/null || true
  if rclone mount "onedrive:" "$MOUNT_POINT/onedrive" \
    --allow-other \
    --allow-non-empty \
    --vfs-cache-mode writes \
    --dir-cache-time 5m \
    --poll-interval 1m \
    --daemon; then
    echo "[init] OneDrive mounted successfully"
  else
    echo "[init] WARNING: OneDrive mount failed (Token 可能过期，请重新获取)"
  fi
  sleep 2
else
  echo "[init] KTV_ONEDRIVE_TOKEN not set, skipping OneDrive mount"
fi

exec java -jar /app/app.jar
