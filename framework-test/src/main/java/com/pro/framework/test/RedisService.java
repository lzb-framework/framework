package com.pro.framework.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Set;

@Component
public class RedisService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    public void run() {
        Set<String> keys = redisTemplate.keys("*");
        System.out.println(keys);
    }


}
