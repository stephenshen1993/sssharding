package com.stephenshen.sssharding.mybatis;

import com.stephenshen.sssharding.engine.ShardingContext;
import com.stephenshen.sssharding.engine.ShardingResult;
import com.stephenshen.sssharding.demo.model.User;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.mapper.MapperFactoryBean;

import java.lang.reflect.Proxy;

/**
 * factory bean for mapper.
 *
 * @author stephenshen
 * @date 2024/8/6 07:54:40
 */
public class ShardingMapperFactoryBean<T> extends MapperFactoryBean<T> {

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
                    System.out.println(" ===> sql statement: " + boundSql.getSql());
                    Object parameterObject = args[0];
                    System.out.println(" ===> sql parameters: " + parameterObject);

                    if (parameterObject instanceof User user) {
                        ShardingContext.set(new ShardingResult(user.getId() % 2 == 0 ? "ds0" : "ds1"));
                    } else if (parameterObject instanceof Integer id) {
                        ShardingContext.set(new ShardingResult(id % 2 == 0 ? "ds0" : "ds1"));
                    }

                    return method.invoke(proxy, args);
                }
        );
    }
}
