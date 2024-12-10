package com.pro.framework.api.enums;

import com.pro.framework.api.FrameworkConst;

/**
 * 字典接口
 * 可用来界面展示
 */
public interface IEnum {
    String name();

    default String getCode() {
        return name();
    }

    default String getLabel() {
        return name();
    }

    default String getColor() {
        return FrameworkConst.Str.EMPTY;
    }

}
