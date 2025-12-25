package com.otus.highload.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Random;

@Slf4j
@Profile("replication")
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "replication")
public class ReplicationRoutingDataSource extends AbstractRoutingDataSource {

    private final Random random = new Random();

    @Override
    protected Object determineCurrentLookupKey() {
        if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
            log.info("Read-only transaction, choosing replica");
            return random.nextBoolean() ? DbType.REPLICA : DbType.ASYNC_REPLICA;
        }
        log.info("Write transaction, using master");
        return DbType.MASTER;
    }

    enum DbType {
        MASTER, REPLICA, ASYNC_REPLICA
    }
}