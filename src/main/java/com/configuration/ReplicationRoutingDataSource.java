package com.configuration;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.lang.Nullable;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


public class ReplicationRoutingDataSource extends AbstractRoutingDataSource {

    public ReplicationRoutingDataSource(DataSource master, DataSource slave1, DataSource slave2) {
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("master", master);
        targetDataSources.put("slave1", slave1);
        targetDataSources.put("slave2", slave2);
        setTargetDataSources(targetDataSources);
        setDefaultTargetDataSource(master);
        afterPropertiesSet();
    }

    private final AtomicInteger counter = new AtomicInteger(0);

    @Nullable
    @Override
    protected Object determineCurrentLookupKey() {
        boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();

        if (readOnly) {
            // Принимает значения 0 или 1, затем увеличивает счетчик
            int index = Math.abs(counter.getAndIncrement() % 2);
            return index == 0 ? "slave1" : "slave2";
        }

        return "master";
    }

}
