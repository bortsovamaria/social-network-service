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

# Настраиваем репликацию на мастере
echo "🔧 Configuring master for replication..."
./01-init-master.sh
./02-create-replica-user.sh
./03-configure-master.sh

# Перезапускаем мастер
docker compose restart pgmaster
# Ждем готовности мастера
echo "⏳ Waiting for master to be ready..."
until docker exec pgmaster pg_isready -U postgres; do
    sleep 2
done
echo "✅ Master is ready!"

# Создаем синхронную реплику
echo "🔄 Creating synchronous replica..."
./04-backup-and-create-slave.sh pgslave 5434

# Создаем асинхронную реплику
echo "🔄 Creating asynchronous replica..."
./04-backup-and-create-slave.sh pgasyncslave 5435

# Настраиваем синхронную репликацию
echo "⚡ Setting up synchronous replication..."
./05-setup-sync-replication.sh

# Генерация данных
echo "⚡ Setting up synchronous replication..."
./06-generate.sh

# Запускаем остальные сервисы
echo "🚀 Starting all services..."
docker-compose up -d

echo "✅ All services started!"
echo ""
echo "📊 Services:"
echo "  PostgreSQL Master:     localhost:5433"
echo "  PostgreSQL Sync Slave: localhost:5434"
echo "  PostgreSQL Async Slave: localhost:5435"
echo "  Spring Boot App:       http://localhost:8080"
echo "  Prometheus:            http://localhost:9090"
echo "  Grafana:               http://localhost:3000 (admin/admin)"