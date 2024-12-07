package com.pro.framework.api.enums;

import com.pro.framework.api.model.IModel;

import java.time.LocalDateTime;

/**
 * 枚举转数据库对象
 */
public interface IEnumToDbDb extends IModel {
    void setEnumToDbCode(String code);
    String getEnumToDbCode();

    LocalDateTime getUpdateTime();
    void setUpdateTime(LocalDateTime updateTime);
}
