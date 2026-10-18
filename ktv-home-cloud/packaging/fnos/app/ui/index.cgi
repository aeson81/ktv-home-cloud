#!/bin/bash
# 飞牛桌面入口：输出导航页（链接用 location.hostname 动态拼出飞牛 IP）。

echo "Content-Type: text/html; charset=utf-8"
echo "Cache-Control: no-store"
echo ""
cat "/var/apps/ktv-home-cloud/target/www/index.html"
