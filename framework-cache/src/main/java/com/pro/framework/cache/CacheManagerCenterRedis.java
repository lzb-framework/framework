package com.pro.framework.cache;

import com.pro.framework.api.cache.ICacheManagerCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 远程缓存基本方法
 * 需轻量使用
 */
@Component
public class CacheManagerCenterRedis implements ICacheManagerCenter {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Object get(String cacheName, String key) {
        return redisTemplate.opsForValue().get(generateCacheKey(cacheName, key));
    }

    @Override
    public void put(String cacheName, String key, Object value) {
        redisTemplate.opsForValue().set(generateCacheKey(cacheName, key), value);
    }

    @Override
    public void put(String cacheName, String key, Object value, long timeNum, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(generateCacheKey(cacheName, key), value, timeNum, timeUnit);
    }

    @Override
    public void evict(String cacheName, String key) {
        redisTemplate.delete(generateCacheKey(cacheName, key));
    }

    @Override
    public void clearCache(String cacheName) {
        Set<String> keys = redisTemplate.keys(generateCacheKey(cacheName, "*"));
        if (keys != null) {
            redisTemplate.delete(keys);
        }
    }

    @Override
    public boolean isCacheExists(String cacheName, String key) {
        return redisTemplate.hasKey(generateCacheKey(cacheName, key));
    }

    @Override
    public Collection<String> getCacheKeys(String cacheName) {
        return redisTemplate.keys(generateCacheKey(cacheName, "*"));
    }

    private String generateCacheKey(String cacheName, String key) {
        return cacheName + ":" + key;
    }
}
