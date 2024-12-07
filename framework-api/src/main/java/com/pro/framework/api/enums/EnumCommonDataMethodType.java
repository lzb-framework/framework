package com.pro.framework.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnumCommonDataMethodType {

    insert("新增", EnumMethodType.INSERT),
    update("更新", EnumMethodType.UPDATE),
    delete("删除", EnumMethodType.DELETE),
    insertOrUpdate("新增获更新", EnumMethodType.QUERY),
    selectById("根据id查询一个", EnumMethodType.QUERY),
    selectOne("查询第一个", EnumMethodType.QUERY),
    selectLists("查询多个列表", EnumMethodType.QUERY),
    selectList("查询列表", EnumMethodType.QUERY),
    selectCountSum("查询总计", EnumMethodType.QUERY),
    selectPage("查询分页列表", EnumMethodType.QUERY),
    ;

    private final String label;
    private final EnumMethodType type;


}
