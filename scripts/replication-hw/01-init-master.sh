#!/bin/bash
set -e

echo "🚀 Starting HighLoad Replication Environment..."

# Запускаем мастер
echo "📦 Starting PostgreSQL Master..."
docker compose up -d pgmaster

# Ждем готовности мастера
echo "⏳ Waiting for master to be ready..."
until docker exec pgmaster pg_isready -U postgres; do
    sleep 2
done
echo "✅ Master is ready!"

echo "🔧 Initializing master configuration..."

# Получаем путь к данным
DATA_DIR="/var/lib/postgresql/data"

# Создаем базовую конфигурацию
docker exec pgmaster bash -c "cat > $DATA_DIR/postgresql.conf << 'EOF'
# Основные настройки

listen_addresses = '*'
dynamic_shared_memory_type = posix	# the default is usually the first option
max_connections = 100			# (change requires restart)
shared_buffers = 128MB			# min 128kB
log_timezone = 'UTC'
datestyle = 'iso, mdy'
timezone = 'UTC'
lc_messages = 'en_US.utf8'			# locale for system error message
					# strings
lc_monetary = 'en_US.utf8'			# locale for monetary formatting
lc_numeric = 'en_US.utf8'			# locale for number formatting
lc_time = 'en_US.utf8'				# locale for time formatting

# default configuration for text search
default_text_search_config = 'pg_catalog.english'

# WAL и репликация
wal_keep_size = 2GB
max_wal_size = 4GB
min_wal_size = 1GB
wal_level = replica
max_wal_senders = 4
ssl = off

EOF"


echo "✅ Master configuration created"
sleep 10
exit;