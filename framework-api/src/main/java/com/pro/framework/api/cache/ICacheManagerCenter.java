package com.pro.framework.api.cache;

import com.pro.framework.api.FrameworkConst;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * 远程缓存基本方法
 * 需轻量使用
 */
public interface ICacheManagerCenter {
    String SPLIT = FrameworkConst.Str.COLON;
    Object get(String cacheName, String key);

    void put(String cacheName, String key, Object value);

    void put(String cacheName, String key, Object value, long expirationTime, TimeUnit days);

    void evict(String cacheName, String key);

    void clearCache(String cacheName);

    boolean isCacheExists(String cacheName, String key);

    Collection<String> getCacheKeys(String cacheName);
}
