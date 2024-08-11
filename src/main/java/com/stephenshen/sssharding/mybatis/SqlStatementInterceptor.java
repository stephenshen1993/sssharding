package com.stephenshen.sssharding.mybatis;

import com.stephenshen.sssharding.engine.ShardingContext;
import com.stephenshen.sssharding.engine.ShardingResult;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.springframework.objenesis.instantiator.util.UnsafeUtils;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.sql.Connection;

/**
 * intercept sql.
 *
 * @author stephenshen
 * @date 2024/8/6 07:35:33
 */
@Intercepts(@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}))
public class SqlStatementInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        ShardingResult shardingResult = ShardingContext.get();
        if (shardingResult != null) {
            StatementHandler handler = (StatementHandler) invocation.getTarget();
            BoundSql boundSql = handler.getBoundSql();
            String sql = boundSql.getSql();
            System.out.println(" ===> SqlStatementInterceptor: " + sql);
            String targetSqlStatement = shardingResult.getTargetSqlStatement();
            if (!sql.equals(targetSqlStatement)) {
                replaceSql(boundSql, targetSqlStatement);
            }
        }
        return invocation.proceed();
    }

    private static void replaceSql(BoundSql boundSql, String sql) throws NoSuchFieldException {
        Field field = boundSql.getClass().getDeclaredField("sql");
        Unsafe unsafe = UnsafeUtils.getUnsafe();
        long fieldOffset = unsafe.objectFieldOffset(field);
        unsafe.putObject(boundSql, fieldOffset, sql);
    }
}
