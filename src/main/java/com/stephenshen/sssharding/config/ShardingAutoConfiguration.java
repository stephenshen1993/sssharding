package com.stephenshen.sssharding.config;

import com.stephenshen.sssharding.datasource.ShardingDataSource;
import com.stephenshen.sssharding.engine.ShardingEngine;
import com.stephenshen.sssharding.engine.StandardShardingEngine;
import com.stephenshen.sssharding.mybatis.SqlStatementInterceptor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * sharding auto configuration.
 * @author stephenshen
 * @date 2024/8/5 07:59:27
 */
@Configuration
@EnableConfigurationProperties(ShardingProperties.class)
public class ShardingAutoConfiguration {

    @Bean
    public ShardingDataSource shardingDataSource(ShardingProperties properties) {
        return new ShardingDataSource(properties);
    }

    @Bean
    public ShardingEngine shardingEngine(ShardingProperties properties) {
        return new StandardShardingEngine(properties);
    }

    @Bean
    public SqlStatementInterceptor sqlStatementInterceptor() {
        return new SqlStatementInterceptor();
    }
}
