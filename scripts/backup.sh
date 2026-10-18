#!/usr/bin/env bash
set -euo pipefail

backup_dir="${1:-./backups}"
timestamp="$(date +%Y%m%d-%H%M%S)"
database="${KTV_DB_NAME:-ktv}"
user="${KTV_DB_USER:-ktv}"
output="${backup_dir}/home-ktv-${timestamp}.dump"

mkdir -p "${backup_dir}"
docker compose exec -T db pg_dump -U "${user}" -d "${database}" --format=custom > "${output}"
echo "Backup created: ${output}"
