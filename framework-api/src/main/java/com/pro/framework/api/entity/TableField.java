package com.pro.framework.api.entity;

import com.pro.framework.api.util.StrUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段注释
 *
 * @author Administrator
 */
@Target(value = {ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface TableField {
    /***
     * 字段中文名
     */
    String value() default StrUtils.EMPTY;

    /***
     * 字段是否在数据库中存在
     */
    boolean exist() default true;
}
