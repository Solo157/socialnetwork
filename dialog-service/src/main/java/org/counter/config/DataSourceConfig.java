package org.counter.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;

/**
 * Подключение к PostgreSQL: master (запись) и read-replica через haproxy (чтение).
 * Первичный DataSource — роутинговый, выбирает цель по флагу readOnly транзакции.
 */
@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.master.url}")
    private String masterUrl;

    @Value("${spring.datasource.master.username}")
    private String masterUsername;

    @Value("${spring.datasource.master.password}")
    private String masterPassword;

    @Value("${spring.datasource.readhaproxy.url}")
    private String readhaproxyUrl;

    @Value("${spring.datasource.readhaproxy.username}")
    private String readhaproxyUsername;

    @Value("${spring.datasource.readhaproxy.password}")
    private String readhaproxyPassword;

    @Bean
    @Primary
    public DataSource routingDataSource(@Qualifier("masterDataSource") DataSource master,
                                        @Qualifier("readhaproxyDataSource") DataSource readhaproxy) {
        ReplicationRoutingDataSource routing = new ReplicationRoutingDataSource(master, readhaproxy);
        return new LazyConnectionDataSourceProxy(routing);
    }

    @Bean(name = "masterDataSource")
    public DataSource masterDataSource() {
        return DataSourceBuilder.create()
                .url(masterUrl)
                .username(masterUsername)
                .password(masterPassword)
                .build();
    }

    @Bean(name = "readhaproxyDataSource")
    public DataSource readhaproxyDataSource() {
        return DataSourceBuilder.create()
                .url(readhaproxyUrl)
                .username(readhaproxyUsername)
                .password(readhaproxyPassword)
                .build();
    }
}
