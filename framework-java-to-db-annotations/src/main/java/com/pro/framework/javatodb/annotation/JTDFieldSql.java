package com.pro.framework.javatodb.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段属性完整SQL
 *
 * @author administrator
 * @date 2022-01-20
 */
@Target(value = {ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface JTDFieldSql {

//    @AliasFor("label")
    String value() default "";
    /**
     * 字段中文名
     */
//    @AliasFor("value")
    String label() default "";

    /**
     * 完整列配置字段
     * `prerogative` varchar(1024) DEFAULT NULL COMMENT '会员卡特权说明,限制1024汉字'
     */
    String fieldConfigSql() default "";

    /**
     * 旧字段名（修改字段名是传递，后续记得手动清理该属性）
     */
    String renameFrom() default "";
}
