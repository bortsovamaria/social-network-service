#!/bin/bash
# check-replication-status.sh

echo "🔍 Checking PostgreSQL replication status..."

echo ""
echo "📊 1. Node roles:"
echo "-----------------"
echo "pgmaster:"
docker exec pgmaster psql -U postgres -c "SELECT pg_is_in_recovery();"
echo ""
echo "pgslave:"
docker exec pgslave psql -U postgres -c "SELECT pg_is_in_recovery();"
echo ""
echo "pgasyncslave:"
docker exec pgasyncslave psql -U postgres -c "SELECT pg_is_in_recovery();"

echo ""
echo "📊 2. Replication slots (on current master):"
echo "--------------------------------------------"
# Определим кто сейчас мастер
MASTER=""
if docker exec pgmaster psql -U postgres -t -c "SELECT pg_is_in_recovery();" | grep -q "f"; then
    MASTER="pgmaster"
elif docker exec pgslave psql -U postgres -t -c "SELECT pg_is_in_recovery();" | grep -q "f"; then
    MASTER="pgslave"
elif docker exec pgasyncslave psql -U postgres -t -c "SELECT pg_is_in_recovery();" | grep -q "f"; then
    MASTER="pgasyncslave"
fi

if [ -n "$MASTER" ]; then
    echo "Current master: $MASTER"
    docker exec $MASTER psql -U postgres -c "SELECT slot_name, active, pg_wal_lsn_diff(pg_current_wal_lsn(), restart_lsn) as lag_bytes FROM pg_replication_slots;"
else
    echo "❌ No master found!"
fi

echo ""
echo "📊 3. Active replication connections:"
echo "-------------------------------------"
if [ -n "$MASTER" ]; then
    docker exec $MASTER psql -U postgres -c "SELECT application_name, client_addr, state, sync_state, write_lag, flush_lag, replay_lag FROM pg_stat_replication;"
else
    echo "No master to check replication"
fi

echo ""
echo "📊 4. WAL position on all nodes:"
echo "--------------------------------"
for NODE in pgmaster pgslave pgasyncslave; do
    echo "$NODE:"
    docker exec $NODE psql -U postgres -c "SELECT pg_current_wal_lsn();" 2>/dev/null || echo "Cannot get WAL position"
    echo ""
done

echo ""
echo "📊 5. Test replication with data:"
echo "---------------------------------"
if [ -n "$MASTER" ]; then
    echo "Creating test data on $MASTER..."
    docker exec $MASTER psql -U postgres -c "DROP TABLE IF EXISTS replication_test; CREATE TABLE replication_test (id SERIAL, data TEXT, created_at TIMESTAMP DEFAULT NOW()); INSERT INTO replication_test (data) VALUES ('test_$(date +%s)');"

    echo "Checking on replicas..."
    for NODE in pgmaster pgslave pgasyncslave; do
        if [ "$NODE" != "$MASTER" ]; then
            echo "$NODE:"
            docker exec $NODE psql -U postgres -c "SELECT * FROM replication_test ORDER BY created_at DESC LIMIT 1;" 2>/dev/null || echo "No data or not accessible"
            echo ""
        fi
    done
fi