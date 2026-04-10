package com.sky.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfiguration {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private String port;

    @Value("${spring.redis.password:}") // 默认为空
    private String password;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        // 修正：确保这里是 redis:// 前缀
        String address = "redis://" + host + ":" + port;
        config.useSingleServer()
                .setAddress(address)
                .setDatabase(0); // 建议和项目用的库一致

        if (password != null && !password.isEmpty()) {
            config.useSingleServer().setPassword(password);
        }

        return Redisson.create(config);
    }
}