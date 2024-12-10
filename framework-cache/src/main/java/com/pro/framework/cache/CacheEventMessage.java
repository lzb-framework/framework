package com.pro.framework.cache;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 缓存事件消息
 */
@Data
@AllArgsConstructor
public class CacheEventMessage implements Serializable {
    private EnumCacheEventType event;
    private String cacheName;
    private Object key;
}
