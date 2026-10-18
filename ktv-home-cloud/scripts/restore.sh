#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 2 || "$2" != "--yes" ]]; then
  echo "Usage: $0 <backup.dump> --yes" >&2
  exit 2
fi

backup_file="$1"
database="${KTV_DB_NAME:-ktv}"
user="${KTV_DB_USER:-ktv}"

if [[ ! -f "${backup_file}" ]]; then
  echo "Backup not found: ${backup_file}" >&2
  exit 2
fi

docker compose stop ktv
trap 'docker compose start ktv >/dev/null' EXIT
docker compose exec -T db pg_restore \
  -U "${user}" \
  -d "${database}" \
  --clean \
  --if-exists \
  --no-owner < "${backup_file}"
docker compose start ktv
trap - EXIT
echo "Restore completed: ${backup_file}"
