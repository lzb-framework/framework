package com.pro.framework.javatodb.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/***
 *
 */
@Data
//@SuperBuilder
@EqualsAndHashCode(callSuper = false)
public class JTDFieldInfoJava extends JTDFieldInfoDb {
    /**
     * 数据库数据类型
     */
    private String propName;
    /**
     * 数据库数据类型
     */
    private Class<?> javaType;

    // /** JTDFieldInfoDb这个类里有了
    //  * 字段对应枚举类
    //  */
    // private Class<?> javaTypeEnumClass;
}
