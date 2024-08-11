package com.stephenshen.sssharding.mybatis;

import com.stephenshen.sssharding.engine.ShardingContext;
import com.stephenshen.sssharding.engine.ShardingEngine;
import com.stephenshen.sssharding.engine.ShardingResult;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * factory bean for mapper.
 *
 * @author stephenshen
 * @date 2024/8/6 07:54:40
 */
public class ShardingMapperFactoryBean<T> extends MapperFactoryBean<T> {

    @Setter
    ShardingEngine engine;

    public ShardingMapperFactoryBean() {
    }

    public ShardingMapperFactoryBean(Class<T> mapperInterface) {
        super(mapperInterface);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getObject() throws Exception {
        T proxy = super.getObject();
        SqlSession session = getSqlSession();
        Configuration configuration = session.getConfiguration();
        Class<T> clazz = getMapperInterface();
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, (p, method, args) -> {
            String mapperId = clazz.getName() + "." + method.getName();
            MappedStatement statement = configuration.getMappedStatement(mapperId);
            BoundSql boundSql = statement.getBoundSql(args);

            Object[] params = getParams(boundSql, args);
            ShardingResult result = engine.sharding(boundSql.getSql(), params);
            ShardingContext.set(result);

            return method.invoke(proxy, args);
        });
    }

    @SneakyThrows
    private static Object[] getParams(BoundSql boundSql, Object[] args) {
        Object[] params = args;
        if (args.length == 1 && !ClassUtils.isPrimitiveOrWrapper(args[0].getClass())) {
            Object arg = args[0];
            List<String> columns = boundSql.getParameterMappings().stream().map(ParameterMapping::getProperty).toList();
            Object[] newParams = new Object[columns.size()];
            for (int i = 0; i < columns.size(); i++) {
                newParams[i] = getFieldValue(arg, columns.get(i));
            }
            params = newParams;
        }
        return params;
    }

    private static Object getFieldValue(Object arg, String column) throws NoSuchFieldException, IllegalAccessException {
        Field field = arg.getClass().getDeclaredField(column);
        field.setAccessible(true);
        return field.get(arg);
    }
}
