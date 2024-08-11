package com.stephenshen.sssharding.strategy;

import java.util.List;
import java.util.Map;

/**
 * Strategy for sharding.
 * @author stephenshen
 * @date 2024/8/11 10:16:13
 */
public interface ShardingStrategy {

    List<String> getShardingColumns();

    String doSharding(List<String> availableTargetNames, String logicTableName, Map<String, Object> shardingParams);

}
