package com.stephenshen.sssharding.engine;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.stephenshen.sssharding.config.ShardingProperties;
import com.stephenshen.sssharding.demo.model.User;
import com.stephenshen.sssharding.strategy.HashShardingStrategy;
import com.stephenshen.sssharding.strategy.ShardingStrategy;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        String logicTableName;
        Map<String, Object> shardingColumnsMap;

        if (sqlStatement instanceof SQLInsertStatement sqlInsertStatement) {
            // insert
            logicTableName = sqlInsertStatement.getTableName().getSimpleName();
            shardingColumnsMap = new HashMap<>();
            List<SQLExpr> columns = sqlInsertStatement.getColumns();

            for (int i = 0; i < columns.size(); i++) {
                SQLExpr column = columns.get(i);
                SQLIdentifierExpr columnExpr = (SQLIdentifierExpr) column;
                String columnName = columnExpr.getSimpleName();
                shardingColumnsMap.put(columnName, args[i]);
            }
        } else {
            // select/update/delete
            MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
            visitor.setParameters(List.of(args));
            sqlStatement.accept(visitor);

            LinkedHashSet<SQLName> sqlNames = new LinkedHashSet<>(visitor.getOriginalTables());
            if (sqlNames.size() > 1) {
                throw new RuntimeException("not support multi tables sharding: " + sqlNames);
            }
            logicTableName = sqlNames.iterator().next().getSimpleName();
            System.out.println(" ===>>> visitor.getOriginalTables = " + logicTableName);
            shardingColumnsMap = visitor.getConditions().stream()
                    .collect(Collectors.toMap(c -> c.getColumn().getName(), c -> c.getValues().get(0)));
            System.out.println(" ===>>> visitor.getConditions = " + logicTableName);

        }

        ShardingStrategy databaseStrategy = databaseStrategies.get(logicTableName);
        String targetDatabase = databaseStrategy.doSharding(actualDatabaseNames.get(logicTableName), logicTableName, shardingColumnsMap);
        ShardingStrategy tableStrategy = tableStrategies.get(logicTableName);
        String targetTable = tableStrategy.doSharding(actualTableNames.get(logicTableName), logicTableName, shardingColumnsMap);
        System.out.println(" ===>>>");
        System.out.println(" ===>>> target db.table = " + targetDatabase + "." + targetTable);
        System.out.println(" ===>>>");
        return new ShardingResult(targetDatabase, sql.replace(logicTableName, targetTable));
    }
}
