#!/bin/bash
set -e

ORIGINAL_MASTER=${1:-pgmaster}
CURRENT_MASTER=${2:-pgslave}

echo "🚀 Quick restore of original master..."

# 1. Останавливаем запись
docker-compose stop app

# 2. Проверяем оригинальный мастер
docker exec $ORIGINAL_MASTER psql -U postgres -c "SELECT pg_is_in_recovery();"

# 4. Быстро настраиваем остальные ноды (просто меняем конфиг)
cat > ../volumes/pgslave/postgresql.conf << EOF
listen_addresses = '*'
primary_conninfo = 'host=$ORIGINAL_MASTER port=5432 user=replicator password=pass application_name=pgslave'
EOF

cat > ../volumes/pgasyncslave/postgresql.conf << EOF
listen_addresses = '*'
primary_conninfo = 'host=$ORIGINAL_MASTER port=5432 user=replicator password=pass application_name=pgasyncslave'
EOF

# Создаем сигнальный файл для реплики
touch ../volumes/$CURRENT_MASTER/standby.signal

# 5. Перезапускаем
docker-compose restart pgslave pgasyncslave
docker-compose start app

echo "✅ Quick restore complete!"