package com.stephenshen.sssharding.datasource;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.stephenshen.sssharding.engine.ShardingContext;
import com.stephenshen.sssharding.engine.ShardingResult;
import com.stephenshen.sssharding.config.ShardingProperties;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * sharding datasource.
 * @author stephenshen
 * @date 2024/8/5 07:43:49
 */
public class ShardingDataSource extends AbstractRoutingDataSource {

    public ShardingDataSource(ShardingProperties properties) {
        Map<Object, Object> dataSourceMap = new LinkedHashMap<>();
        properties.getDatasources().forEach((k, v) -> {
            try {
                dataSourceMap.put(k, DruidDataSourceFactory.createDataSource(v));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        setTargetDataSources(dataSourceMap);
        setDefaultTargetDataSource(dataSourceMap.values().iterator().next());
    }

    @Override
    protected Object determineCurrentLookupKey() {
        ShardingResult shardingResult = ShardingContext.get();
        Object key = shardingResult == null ? null : shardingResult.getTargetDatasourceName();
        System.out.println(" ===> determineCurrentLookupKey = " + key);
        return key;
    }
}
