package com.pro.framework.cache;

import lombok.AllArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import java.util.List;

/**
 * 监听,执行,缓存事件
 */
@AllArgsConstructor
public class CacheMessageListener extends MessageListenerAdapter {
    private List<CacheManager> cacheManagers;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        CacheEventMessage msg = (CacheEventMessage) extractMessage(message);
        String cacheName = msg.getCacheName();
        Object key = msg.getKey();
        switch (msg.getEvent()) {
            case get:
                break;
            case put:
                for (CacheManager cacheManager : cacheManagers) {
                    cacheManager.getCache(cacheName).put(cacheName, key);
                }
                break;
            case evit:
                for (CacheManager cacheManager : cacheManagers) {
                    cacheManager.getCache(cacheName).evict(key);
                }
                break;
            case clear:
                for (CacheManager cacheManager : cacheManagers) {
                    cacheManager.getCache(cacheName).clear();
                }
                break;
        }
    }
}
