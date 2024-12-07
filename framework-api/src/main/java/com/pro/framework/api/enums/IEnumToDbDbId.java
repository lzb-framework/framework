package com.pro.framework.api.enums;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 枚举转数据库对象
 */
public interface IEnumToDbDbId extends IEnumToDbDb {
    Long getId();

    void setId(Long id);

    default void setEnumToDbCode(String code) {
        setId(Long.valueOf(code.substring(1)));
    }

    @JsonIgnore
    default String getEnumToDbCode() {
        return "_" + getId();
    }
}
