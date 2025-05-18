package com.bbangle.bbangle.config;

import com.bbangle.bbangle.common.querylistener.CustomQueryLoggingListener;
import com.zaxxer.hikari.HikariDataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Profile("!test")
public class DataSourceProxyConfig {

    @Bean
    public DataSource dataSource(DataSourceProperties properties) {
        HikariDataSource hikari = properties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();

        return ProxyDataSourceBuilder
                .create(hikari)
                .name("DB-Logger")
                .listener(new CustomQueryLoggingListener())
                .countQuery()
                .multiline()
                .build();
    }
}