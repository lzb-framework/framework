package com.pro.framework.api.enums;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 枚举转数据库对象
 */
public interface IEnumToDbDbCode extends IEnumToDbDb {
    String getCode();

    void setCode(String code);

    default void setEnumToDbCode(String code) {
        setCode(code);
    }

    @JsonIgnore
    default String getEnumToDbCode() {
        return getCode();
    }
}
