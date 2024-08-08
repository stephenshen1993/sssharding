package com.stephenshen.sssharding.mybatis;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.springframework.stereotype.Component;

import java.sql.Connection;

/**
 * intercept sql.
 *
 * @author stephenshen
 * @date 2024/8/6 07:35:33
 */
@Component
@Intercepts(@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}))
public class SqlStatementInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler handler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = handler.getBoundSql();
        System.out.println(" ===> sql statement: " + boundSql.getSql());
        Object parameterObject = boundSql.getParameterObject();
        System.out.println(" ===> sql parameters: " + parameterObject);

        // todo 修改sql，user -> user1
        return invocation.proceed();
    }
}
