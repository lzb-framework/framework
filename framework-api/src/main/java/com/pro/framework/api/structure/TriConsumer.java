package com.pro.framework.api.structure;

public interface TriConsumer<T, U, V> {
    void accept(T t, U u, V v);
}
