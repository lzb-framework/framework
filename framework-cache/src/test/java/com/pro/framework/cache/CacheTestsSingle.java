package com.pro.framework.cache;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class CacheTestsSingle {
    @Autowired
    private DemoService demoService;
    @Autowired
    private CaffeineCacheManager cacheManagerLocal;
    @Autowired
    private RedisCacheManager cacheManagerCenter;

//    @Test
    public void testCaffeineCache() {
        String key = System.currentTimeMillis() + "";
        MyData demo = demoService.getCachedData(key);
        System.out.println("demoService " + demo);
        System.out.println("cacheManager1 " + cacheManagerLocal.getCache("demo").get(key, MyData.class));
        System.out.println("cacheManager2 " + cacheManagerCenter.getCache("demo").get(key, MyData.class));
        System.out.println("demoSffervice " + demo);
    }
}
