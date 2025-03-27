package com.pro.framework.cache;

import com.pro.framework.api.message.EnumTopicFramework;
import com.pro.framework.api.message.IBaseMessageService;
import lombok.SneakyThrows;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.AbstractValueAdaptingCache;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * 定制缓存项
 */
public class CombinedCache extends AbstractValueAdaptingCache implements Cache {
    /**
     * 多级缓存
     */
    private final List<CacheManager> cacheManagers;
    /**
     * 服务通讯
     */
    private final IBaseMessageService messageService;

    private final String name;

    public CombinedCache(
            List<CacheManager> cacheManagers,
            String name,
            IBaseMessageService messageService,
            boolean allowNullValues
    ) {
        super(allowNullValues);
        assert cacheManagers.size() > 0;
        this.cacheManagers = cacheManagers;
        this.messageService = messageService;
        this.name = name;
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return cacheManagers.get(0).getCache(name).getNativeCache();
    }

    @Override
    @SneakyThrows
    public <T> T get(Object key, Callable<T> valueLoader) {
        Object val = lookup(key);
        if (val != null) {
            return (T) val;
        }
        T valNew = valueLoader.call();
        put(key, valNew);
        return valNew;
    }

    /**
     * 去缓存查数据
     */
    @Override
    public Object lookup(Object key) {
        // 从一级,二级获取数据
        for (CacheManager cacheManager : cacheManagers) {
            ValueWrapper wrapper = cacheManager.getCache(name).get(key);
            if (wrapper != null) {
                Object value = wrapper.get();
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    /**
     * 新增/修改数据
     */
    @Override
    public void put(Object key, Object value) {
        System.out.println("put " + key + " " + value.hashCode());
        // 将数据放入一级缓存，并通知所有分布式服务器更新二级缓存
        for (CacheManager cacheManager : cacheManagers) {
            cacheManager.getCache(name).put(key, value);
        }
        //广播消息给所有服务器
        broadcast(EnumCacheEventType.put, name, key);
    }

    /**
     * 删除数据
     */
    @Override
    public void evict(Object key) {
        for (CacheManager cacheManager : cacheManagers) {
            cacheManager.getCache(name).evict(key);
        }
        //广播消息给所有服务器
        broadcast(EnumCacheEventType.evit, name, key);
    }

    /**
     * 清空项(表)
     */
    @Override
    public void clear() {
        for (CacheManager cacheManager : cacheManagers) {
            cacheManager.getCache(name).clear();
        }
        //广播消息给所有服务器
        broadcast(EnumCacheEventType.clear, name, null);
    }

    /**
     * 广播消息给所有服务器
     */
    private void broadcast(EnumCacheEventType event, String name, Object key) {
        messageService.sendMessageToServers(EnumTopicFramework.framework_cache.name(), new CacheEventMessage(event, name, key));
    }
}
