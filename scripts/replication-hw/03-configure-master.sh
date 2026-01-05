#!/bin/bash
set -e

echo "🔐 Configuring master authentication..."

docker exec pgmaster bash -c "cat > /var/lib/postgresql/data/pg_hba.conf << 'EOF'
# local is for Unix domain socket connections only
local   all             all                                     trust
# IPv4 local connections:
host    all             all             127.0.0.1/32            trust
# IPv6 local connections:
host    all             all             ::1/128                 trust
# Allow replication connections from localhost, by a user with the
# replication privilege.
local   replication     all                                     trust
host    replication     all             127.0.0.1/32            trust
host    replication     all             ::1/128                 trust

# Разрешаем ВСЕ подключения (для демо-среды)
host    all             all             0.0.0.0/0               md5
host    all             all             ::/0                    md5
# Репликация со всех хостов
host    replication     all             0.0.0.0/0               md5
host    replication     all             ::/0                    md5

EOF"

echo "✅ Master authentication configured"