#!/bin/bash
set -e

SLAVE_NAME=${1:-pgslave}
SLAVE_PORT=${2:-5432}
MASTER_HOST="pgmaster"

echo "🔄 Creating replica: $SLAVE_NAME on port $SLAVE_PORT"

# Создаем .pgpass файл для автоматической аутентификации
echo "🔐 Creating .pgpass file..."
docker exec pgmaster bash -c "
echo 'pgmaster:5432:replication:replicator:pass' > /tmp/.pgpass
chmod 600 /tmp/.pgpass
export PGPASSFILE=/tmp/.pgpass
"

# Создаем бэкап с использованием
echo "📦 Creating base backup..."
docker exec pgmaster bash -c "
export PGPASSWORD='pass'
rm -rf /backup 2>/dev/null
mkdir -p /backup
pg_basebackup -h pgmaster -D /$SLAVE_NAME -U replicator -v -P --wal-method=stream
"

# Копируем бэкап на хост
echo "📋 Copying backup to host..."
docker cp pgmaster:/pgslave ../volumes/$SLAVE_NAME

# Создаем сигнальный файл для реплики
touch ../volumes/$SLAVE_NAME/standby.signal

# Настраиваем конфиг реплики
echo "🔧 Configuring replica..."
cat > ../volumes/$SLAVE_NAME/postgresql.conf << EOF
# Реплика
listen_addresses = '*'
primary_conninfo = 'host=$MASTER_HOST port=5432 user=replicator password=pass application_name=$SLAVE_NAME'
EOF

# Запускаем реплику
echo "📦 Starting PostgreSQL Replica..."
docker compose up -d $SLAVE_NAME

# Ждем готовности реплики
echo "⏳ Waiting for master to be ready..."
until docker exec $SLAVE_NAME pg_isready -U postgres; do
    sleep 2
done

# Проверяем статус репликации
echo "📊 Checking replication status..."
docker exec $SLAVE_NAME psql -U postgres -c "SELECT * FROM pg_stat_wal_receiver;"

echo "✅ Replica $SLAVE_NAME is ready!"