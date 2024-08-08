package com.stephenshen.sssharding.demo.mapper;

import com.stephenshen.sssharding.demo.model.User;
import org.apache.ibatis.annotations.*;

/**
 * mapper for user.
 * @author stephenshen
 * @date 2024/8/2 07:19:43
 */
@Mapper
public interface UserMapper {

    @Insert("insert into user(id, name, age) values(#{id}, #{name}, #{age})")
    int insert(User user);

    @Select("select * from user where id = #{id}")
    User findById(int id);

    @Update("update user set name = #{name}, age = #{age} where id = #{id}")
    int update(User user);

    @Delete("delete from user where id = #{id}")
    int delete(int id);
}
