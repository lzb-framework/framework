package com.pro.framework.api.database;

import com.pro.framework.api.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据库操作
 */
@Getter
@AllArgsConstructor
public enum EnumDbOpt implements IEnum {
    query("查"),
    insert("增"),
    delete("删"),
    update("改"),
    ;

    String label;

}
