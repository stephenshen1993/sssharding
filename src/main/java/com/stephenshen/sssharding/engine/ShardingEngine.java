package com.stephenshen.sssharding.engine;

/**
 * Core sharding engine,
 * @author stephenshen
 * @date 2024/8/9 07:59:16
 */
public interface ShardingEngine {

    ShardingResult sharding(String sql, Object[] args);
}
