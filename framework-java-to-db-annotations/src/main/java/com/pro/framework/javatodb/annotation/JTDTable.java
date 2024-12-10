package com.pro.framework.javatodb.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface JTDTable {
    /**
     * 表中文名
     */
//    @AliasFor("label")
    String value() default "";

//    @AliasFor("value")
    String label() default "";

    //label + description构成字段备注
    String description() default "";

    /**
     * 表名
     */
    String tableName() default "";

    /**
     * 主键信息
     */
    String[] keyFieldNames() default {"id"};

    /**
     * 排除字段
     */
    String[] excludeFieldNames() default {};

    /**
     * 索引信息
     * 例如 UNIQUE KEY `uk_variable_valName` (`variable`,`val_name`) USING BTREE
     */
    String[] sequences() default {""};

    /**
     * 模块(便于生成到指定目录下)
     */
    String module() default "";

    /**
     * 表唯一id(便于生成一些菜单id之类)
     */
    int entityId() default 0;
}
