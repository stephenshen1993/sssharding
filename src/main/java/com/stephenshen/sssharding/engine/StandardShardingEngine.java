package com.stephenshen.sssharding.engine;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.stephenshen.sssharding.config.ShardingProperties;
import com.stephenshen.sssharding.demo.model.User;
import com.stephenshen.sssharding.strategy.HashShardingStrategy;
import com.stephenshen.sssharding.strategy.ShardingStrategy;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Standard sharding engine.
 * @author stephenshen
 * @date 2024/8/9 08:04:17
 */
public class StandardShardingEngine implements ShardingEngine {

    private final MultiValueMap<String, String> actualDatabaseNames = new LinkedMultiValueMap<>();
    private final MultiValueMap<String, String> actualTableNames = new LinkedMultiValueMap<>();
    private final Map<String, ShardingStrategy> databaseStrategies = new HashMap<>();
    private final Map<String, ShardingStrategy> tableStrategies = new HashMap<>();

    public StandardShardingEngine(ShardingProperties properties) {
        properties.getTables().forEach((tableName, tableProperties) -> {
            tableProperties.getActualDataNodes().forEach(actualDataNode -> {
                String[] split = actualDataNode.split("\\.");
                String actualDatabaseName = split[0], actualTableName = split[1];
                actualDatabaseNames.add(tableName, actualDatabaseName);
                actualTableNames.add(tableName, actualTableName);
            });
            databaseStrategies.put(tableName, new HashShardingStrategy(tableProperties.getDatabaseStrategy()));
            tableStrategies.put(tableName, new HashShardingStrategy(tableProperties.getTableStrategy()));
        });
    }

    @Override
    public ShardingResult sharding(String sql, Object[] args) {

        SQLStatement sqlStatement = SQLUtils.parseSingleMysqlStatement(sql);
        if (sqlStatement instanceof SQLInsertStatement sqlInsertStatement) {
            String logicTableName = sqlInsertStatement.getTableName().getSimpleName();
            Map<String, Object> shhardingColumnsMap = new HashMap<>();
            List<SQLExpr> columns = sqlInsertStatement.getColumns();

            for (int i = 0; i < columns.size(); i++) {
                SQLExpr column = columns.get(i);
                SQLIdentifierExpr columnExpr = (SQLIdentifierExpr) column;
                String columnName = columnExpr.getSimpleName();
                shhardingColumnsMap.put(columnName, args[i]);
            }

            ShardingStrategy databaseStrategy = databaseStrategies.get(logicTableName);
            String targetDatabase = databaseStrategy.doSharding(actualDatabaseNames.get(logicTableName), logicTableName, shhardingColumnsMap);
            ShardingStrategy tableStrategy = tableStrategies.get(logicTableName);
            String targetTable = tableStrategy.doSharding(actualTableNames.get(logicTableName), logicTableName, shhardingColumnsMap);
            System.out.println(" ===>>>");
            System.out.println(" ===>>> target db.table = " + targetDatabase + "." + targetTable);
            System.out.println(" ===>>>");
        } else {

        }

        Object parameterObject = args[0];
        System.out.println(" ===> sql statement: " + sql);
        int id = 0;
        if (parameterObject instanceof User user) {
            id = user.getId();
        } else if (parameterObject instanceof Integer uid) {
            id = uid;
        }
        return new ShardingResult(id % 2 == 0 ? "ds0" : "ds1", sql);
    }
}
