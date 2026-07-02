package com.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;

import java.util.*;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.master.url}")
    private String masterUrl;

    @Value("${spring.datasource.master.username}")
    private String masterUsername;

    @Value("${spring.datasource.master.password}")
    private String masterPassword;

    @Value("${spring.datasource.slave1.url}")
    private String slave1Url;

    @Value("${spring.datasource.slave1.username}")
    private String slave1Username;

    @Value("${spring.datasource.slave1.password}")
    private String slave1Password;

    @Value("${spring.datasource.slave2.url}")
    private String slave2Url;

    @Value("${spring.datasource.slave2.username}")
    private String slave2Username;

    @Value("${spring.datasource.slave2.password}")
    private String slave2Password;

    /**
     * Принимает сорсы баз данных и подключает их к роутингу.
     */
    @Bean
    @Primary
    public DataSource routingDataSource(@Qualifier("masterDataSource") DataSource master,
                                        @Qualifier("slave1DataSource") DataSource slave1,
                                        @Qualifier("slave2DataSource") DataSource slave2) {
        ReplicationRoutingDataSource routingDataSource = new ReplicationRoutingDataSource(master, slave1, slave2);

        return new LazyConnectionDataSourceProxy(routingDataSource);
    }

    @Bean(name = "masterDataSource")
    public DataSource masterDataSource() {
        return DataSourceBuilder.create()
                .url(masterUrl)
                .username(masterUsername)
                .password(masterPassword)
                .build();
    }

    @Bean(name = "slave1DataSource")
    public DataSource slave1DataSource() {
        return DataSourceBuilder.create()
                .url(slave1Url)
                .username(slave1Username)
                .password(slave1Password)
                .build();
    }

    @Bean(name = "slave2DataSource")
    public DataSource slave2DataSource() {
        return DataSourceBuilder.create()
                .url(slave2Url)
                .username(slave2Username)
                .password(slave2Password)
                .build();
    }

}