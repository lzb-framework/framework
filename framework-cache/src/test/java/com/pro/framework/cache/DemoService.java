package com.pro.framework.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class DemoService {

    @Autowired
    private CombinedCacheManager customCacheManager;

    @Cacheable(cacheNames = "demo")
    public MyData getCachedData(String key) {
        System.out.println("查询数据库 " + key);
        // 从一级缓存中获取数据逻辑,如果获取不到去二级缓存获取,如果获取不到,就去查询数据库
        return new MyData(System.currentTimeMillis() + ""); // Your implementation here
    }

    @CacheEvict(cacheNames = "demo")
    public void evictCache(String key) {
        // 清理redis订阅发布,通知所有分布式服务器,一级和二级的这个缓存key (需要保障线程安全)
        customCacheManager.getCache("demo").clear();
    }
}
