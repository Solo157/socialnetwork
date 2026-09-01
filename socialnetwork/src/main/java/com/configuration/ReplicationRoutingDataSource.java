package com.configuration;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.lang.Nullable;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Сервис, отвечающий за роутинг на базы данных при создании транзакции.
 */
public class ReplicationRoutingDataSource extends AbstractRoutingDataSource {

    /**
     * В конструкторе регистрируем сорсы баз данных.
     */
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

    /**
     * По при знаку readOnly понимаем какой сорс БД использовать. readOnly считывается из аннотации @Transactional.
     */
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
