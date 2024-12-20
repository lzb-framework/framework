package com.pro.framework.javatodb.annotation;


import com.pro.framework.javatodb.constant.JTDConst;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段属性
 *
 * @author administrator
 * @date 2022-01-20
 */
@Target(value = {ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface JTDField {
    /**
     * 字段中文名
     */
//    @AliasFor("label")
    String value() default "";

//    @AliasFor("value")
    String label() default "";

    //代码和页面备注
    String description() default "";
    //数据库字段备注
    String descriptionDb() default "";

    /**
     * 字段名 例如 nick_name
     */
    String fieldName() default "";

    /**
     * 数据库数据类型
     */
    JTDConst.EnumFieldType type() default JTDConst.EnumFieldType.none;
    /**
     * 页面端类型
     */
    JTDConst.EnumFieldUiType uiType() default JTDConst.EnumFieldUiType.none;

    /**
     * 长度
     */
    int mainLength() default JTDConst.INT__1;

    /**
     * 小数位长度
     */
    int decimalLength() default JTDConst.INT__1;

    /**
     * 是否不为空
     */
    JTDConst.EnumFieldNullType notNull() default JTDConst.EnumFieldNullType.none;

    /**
     * 默认值
     * 默认没设置default
     * "NULL"表示设置为空
     */
    String defaultValue() default "";

    /**
     * 旧字段名（修改字段名是传递，后续记得手动清理该属性）
     */
    String renameFrom() default "";

    /**
     * 字段对应枚举类
     */
    Class javaTypeEnumClass() default Object.class;
    /**
     * 字段对应枚举类
     */
    boolean javaTypeEnumClassMultiple() default false;

    /**
     * 是否自增
     */
    boolean autoIncrement() default true;

    /**
     * 字符集
     */
    String charset() default "";


    /**
     * 排除字段
     */
    boolean exist() default true;

    /**
     * 属性分组
     */
    String group() default "";
    /**
     * 对应哪个实体类(的主键)
     */
    String entityName() default "";
    /**
     * 对应哪个实体类(的主键)
     */
    Class entityClass() default Object.class;
    /**
     * 对应实体类的哪个主键
     */
    String entityClassKey() default "code";
    /**
     * 对应实体类的哪个主键说明
     */
    String entityClassLabel() default "name";
    /**
     * 当前属性对应实体类的哪个属性
     */
    String entityClassTargetProp() default "code";
    /**
     * 扩展属性
     */
    String extendProp() default "";

    boolean sortable() default false;

    int sort() default JTDConst.INT__1;

    boolean disabled() default false;
}
