package com.pro.framework.cache;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum EnumCacheEventType {
    get("根据cacheName_key查询一个元素"),
    put("根据cacheName_key更新一个元素"),
    evit("根据cacheName_key删除一个元素"),
    clear("根据cacheName删除所有元素"),
    ;
    String label;
}
