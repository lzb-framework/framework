package com.pro.framework.api.structure;

import java.util.HashMap;
import java.util.Map;

/**
 * HashMap,扩展getValue框架便捷方法
 * @author Administrator
 * @param <K>
 * @param <V>
 */
public class FirstHashMap<K, V> extends HashMap<K, V> {

    public FirstHashMap() {
        super();
    }

    public FirstHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public FirstHashMap(Map<? extends K, ? extends V> m) {
        super(m);
    }

    /**
     * 添加getFirst方法
     *
     * @return 方便快速取值
     */
    public V getFirstValue() {
        return this.values().size() > 0 ? this.values().iterator().next() : null;
    }

//    /**
//     * 添加getFirst方法
//     *
//     * @return 方便快速取值
//     */
//    public <T, VAL> V getValue(String relationCode, FunSerializable<T, VAL> prop) {
//        SerializedLambdaData serializedLambdaData = LambdaUtil.resolveCache(prop);
//        return get(relationCode + "." + serializedLambdaData.getPropName());
//    }

    private static final FirstHashMap<?, ?> EMPTY = new FirstHashMap<>();

    public static <K, V> FirstHashMap<K, V> emptyMap() {
        //noinspection unchecked
        return (FirstHashMap<K, V>) EMPTY;
    }
}
