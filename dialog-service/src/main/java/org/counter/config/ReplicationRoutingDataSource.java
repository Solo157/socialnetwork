package org.counter.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.lang.Nullable;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Роутинг на мастер или read-replica по флагу readOnly транзакции.
 */
public class ReplicationRoutingDataSource extends AbstractRoutingDataSource {

    public ReplicationRoutingDataSource(DataSource master, DataSource readhaproxy) {
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("master", master);
        targetDataSources.put("readhaproxy", readhaproxy);
        setTargetDataSources(targetDataSources);
        setDefaultTargetDataSource(master);
        afterPropertiesSet();
    }

    @Nullable
    @Override
    protected Object determineCurrentLookupKey() {
        return TransactionSynchronizationManager.isCurrentTransactionReadOnly() ? "readhaproxy" : "master";
    }
}
