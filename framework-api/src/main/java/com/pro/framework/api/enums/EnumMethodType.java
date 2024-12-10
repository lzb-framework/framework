package com.pro.framework.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnumMethodType {

    OTHER("其他"),
    QUERY("查询"),
    INSERT("添加"),
    UPDATE("更新"),
    DELETE("删除"),

    ;

    private String label;


}
