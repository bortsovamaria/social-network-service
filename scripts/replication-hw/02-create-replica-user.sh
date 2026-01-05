#!/bin/bash
set -e

echo "👤 Creating replication user..."

docker exec pgmaster bash -c "
 psql -U postgres << 'SQL'
-- Создаем пользователя для репликации
create role replicator with login replication password 'pass';
SQL
"

echo "✅ Replication user created"

# Проверяем, что пользователь создан
echo "🔍 Checking if replicator user exists..."
docker exec pgmaster psql -U postgres -c "\du replicator"