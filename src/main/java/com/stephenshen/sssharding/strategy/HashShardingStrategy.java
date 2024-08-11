package com.stephenshen.sssharding.strategy;

import groovy.lang.Closure;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * hash sharding strategy.
 * @author stephenshen
 * @date 2024/8/11 10:18:02
 */
public class HashShardingStrategy implements ShardingStrategy {

    private final String shardingColumn;
    private final String algorithmExpression;

    public HashShardingStrategy(Properties properties) {
        this.shardingColumn = properties.getProperty("shardingColumn");
        this.algorithmExpression = properties.getProperty("algorithmExpression");
    }

//    @Override
//    public List<String> getShardingColumns() {
//        return List.of(this.shardingColumn);
//    }

    @Override
    public String doSharding(List<String> availableTargetNames, String logicTableName, Map<String, Object> shardingParams) {
        String expression = InlineExpressionParser.handlePlaceHolder(this.algorithmExpression);
        InlineExpressionParser parser = new InlineExpressionParser(expression);
        Closure closure = parser.evaluateClosure();
        closure.setProperty(this.shardingColumn, shardingParams.get(this.shardingColumn));
        return closure.call().toString();
    }
}
