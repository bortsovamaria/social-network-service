#!/bin/bash
set -e

SLAVE_NAME=${1:-pgslave}
ASYNCSLAVE_NAME=${2:-pgasyncslave}

echo "👑 Promoting slave $SLAVE_NAME to master..."

# Промоутим слейв
docker exec $SLAVE_NAME psql -U postgres -c "SELECT pg_promote();"

# Ждем промоута
sleep 5

# Проверяем статус
docker exec $SLAVE_NAME psql -U postgres -c "SELECT pg_is_in_recovery();"

echo "✅ Slave $SLAVE_NAME promoted to master!"
echo "⚠️ Don't forget to reconfigure other slaves to follow the new master!"

# Настраиваем конфиг реплики
echo "🔧 Configuring replica..."
cat > ../volumes/pgasyncslave/postgresql.conf << EOF
# Реплика
listen_addresses = '*'
primary_conninfo = 'host=$SLAVE_NAME port=5432 user=replicator password=pass application_name=$ASYNCSLAVE_NAME'
EOF

docker exec $SLAVE_NAME bash -c "
 psql -U postgres << 'SQL'
 \c social_network
-- Перезагружаем конфиг
SELECT pg_reload_conf();

-- Проверяем настройки
SELECT name, setting FROM pg_settings
WHERE name IN ('synchronous_commit', 'synchronous_standby_names');
SQL
"