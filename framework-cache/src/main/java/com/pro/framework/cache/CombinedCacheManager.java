package com.pro.framework.cache;

import com.pro.framework.api.message.IBaseMessageService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 多缓存管理
 */
@Getter
@Component
@AllArgsConstructor
public class CombinedCacheManager implements CacheManager {

    private final List<CacheManager> cacheManagers;
    private final IBaseMessageService messageService;
    private final Map<String, Cache> cacheMap;

    /**
     * 获取缓存项(类似一张表)
     */
    @Override
    public Cache getCache(String name) {
        return cacheMap.computeIfAbsent(name, key -> new CombinedCache(cacheManagers, key, messageService, false));
    }

    @Override
    public Collection<String> getCacheNames() {
        return cacheMap.keySet();
    }


}
