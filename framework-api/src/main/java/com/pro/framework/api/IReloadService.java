package com.pro.framework.api;

public interface IReloadService {
    void reload();

    default Integer getSort() {
        return 10000;
    }
}
