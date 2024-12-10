package com.pro.framework.javatodb.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnumUIArea {
    base("公共"),
    search("表格搜索表单"),
//    tableColumn("表格每一列"),
    table("表格"),
    form("提交表单"),
    ;
    final String label;
}
