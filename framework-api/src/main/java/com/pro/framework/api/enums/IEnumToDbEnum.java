package com.pro.framework.api.enums;

/**
 * 枚举转数据库对象
 */
public interface IEnumToDbEnum<T extends IEnumToDbDb> {
    default String getToDbCode(){
        return name();
    }
    default String getForceChangeTime() {
        return null;
    }

    String name();
}
