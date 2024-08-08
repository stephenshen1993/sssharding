package com.stephenshen.sssharding;

import com.stephenshen.sssharding.demo.User;
import com.stephenshen.sssharding.demo.UserMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(ShardingAutoConfiguration.class)
@MapperScan(value = "com.stephenshen.sssharding.demo", factoryBean = ShardingMapperFactoryBean.class)
public class SsShardingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SsShardingApplication.class, args);
    }

    @Autowired
    UserMapper userMapper;

    @Bean
    ApplicationRunner applicationRunner() {
        return x -> {
            for (int id = 1; id <= 10; id++) {
                test(id);
            }
        };
    }

    private void test(int id) {
        System.out.println(" ===> 1.test insert ...");
        int inserted = userMapper.insert(new User(id, "stephenshen", 20));
        System.out.println(" ===> inserted = " + inserted);

        System.out.println(" ===> 2.test find ...");
        User user = userMapper.findById(id);
        System.out.println(" ===> find = " + user);

        System.out.println(" ===> 3.test update ...");
        user.setName("SS");
        int updated = userMapper.update(user);
        System.out.println(" ===> updated = " + updated);

        System.out.println(" ===> 4.test new find ...");
        User user2 = userMapper.findById(id);
        System.out.println(" ===> find = " + user2);

//            System.out.println(" ===> 5.test delete ...");
//            int deleted = userMapper.delete(id);
//            System.out.println(" ===> deleted = " + deleted);
    }
}
