package com.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

//    @Value("${spring.datasource.url:jdbc:postgresql://master:5432/postgres}")
//    private String datasourceUrl;
//
//    @Value("${spring.datasource.username:postgres}")
//    private String datasourceUsername;
//
//    @Value("${spring.datasource.password:postgres}")
//    private String datasourcePassword;

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

//    @Value("${spring.datasource.slave2.url}")
//    private String slave2Url;
//
//    @Value("${spring.datasource.slave2.username}")
//    private String slave2Username;
//
//    @Value("${spring.datasource.slave2.password}")
//    private String slave2Password;

    /**
     * Принимает сорсы баз данных и подключает их к роутингу.
     */
    @Bean
    @Primary
    public DataSource routingDataSource(@Qualifier("masterDataSource") DataSource master,
                                        @Qualifier("readhaproxyDataSource") DataSource readhaproxy) {
        ReplicationRoutingDataSource routingDataSource = new ReplicationRoutingDataSource(master, readhaproxy);

        return new LazyConnectionDataSourceProxy(routingDataSource);
    }

//    @Bean(name = "dataSource")
//    public DataSource dataSource() {
//        return DataSourceBuilder.create()
//                .url(datasourceUrl)
//                .username(datasourceUsername)
//                .password(datasourcePassword)
//                .build();
//    }

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

//    @Bean(name = "slave2DataSource")
//    public DataSource slave2DataSource() {
//        return DataSourceBuilder.create()
//                .url(slave2Url)
//                .username(slave2Username)
//                .password(slave2Password)
//                .build();
//    }

}
