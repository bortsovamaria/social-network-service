#!/bin/bash
set -e

echo "⚡ Setting up synchronous replication..."

docker exec pgmaster bash -c "
 psql -U postgres << 'SQL'
 \c social_network
-- Включаем синхронную репликацию для первого слейва
ALTER SYSTEM SET synchronous_commit = 'on';
ALTER SYSTEM SET synchronous_standby_names = 'FIRST 1 (pgslave)';

-- Перезагружаем конфиг
SELECT pg_reload_conf();

-- Проверяем настройки
SELECT name, setting FROM pg_settings
WHERE name IN ('synchronous_commit', 'synchronous_standby_names');
SQL
"

echo "✅ Synchronous replication configured"