#!/bin/bash
# diagnose-replication.sh

echo ""
echo "1. Checking database statistics on each node:"
echo "--------------------------------------------"

for NODE in pgslave pgasyncslave; do
  echo ""
  echo "📊 $NODE:"
  docker exec $NODE psql -U postgres -c "
  SELECT
      datname,
      xact_commit,
      xact_rollback,
      numbackends,
      stats_reset
  FROM pg_stat_database
  WHERE datname NOT IN ('template0', 'template1', 'postgres')
  ORDER BY xact_commit DESC;"
done

echo ""
echo "2. Checking replication status:"
echo "-------------------------------"

# Найдем мастера
MASTER=""
for NODE in pgmaster pgslave pgasyncslave; do
    if docker exec $NODE psql -U postgres -t -c "SELECT pg_is_in_recovery();" 2>/dev/null | grep -q "f"; then
        MASTER="$NODE"
        break
    fi
done

if [ -n "$MASTER" ]; then
    echo "✅ Current master: $MASTER"
    docker exec $MASTER psql -U postgres -c "
    SELECT
        application_name,
        client_addr,
        state,
        sync_state,
        pg_wal_lsn_diff(pg_current_wal_lsn(), replay_lsn) as lag_bytes
    FROM pg_stat_replication;"
else
    echo "❌ No master found!"
fi

echo ""
echo "3. Testing with new data:"
echo "-------------------------"

if [ -n "$MASTER" ]; then
    echo "Creating test data on $MASTER..."
    docker exec $MASTER psql -U postgres -c "
    CREATE TABLE IF NOT EXISTS replication_diagnosis (
        id SERIAL PRIMARY KEY,
        source TEXT,
        created_at TIMESTAMP DEFAULT NOW()
    );
    INSERT INTO replication_diagnosis (source) VALUES ('diagnosis_test');"

    echo "Checking replicas..."
    for NODE in pgslave pgasyncslave; do
        if [ "$NODE" != "$MASTER" ]; then
            echo ""
            echo "📊 $NODE:"
            docker exec $NODE psql -U postgres -c "SELECT * FROM replication_diagnosis ORDER BY created_at DESC LIMIT 1;" 2>/dev/null || echo "Table not found"
        fi
    done
fi