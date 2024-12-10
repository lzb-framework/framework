package com.pro.framework.javatodb.model;


import com.pro.framework.javatodb.annotation.JTDField;
import com.pro.framework.javatodb.constant.JTDConst;
import lombok.Data;

import java.lang.annotation.Annotation;

/**
 * 字段属性
 *
 * @author administrator
 * @date 2022-01-20
 */
@Data
public class JTDFieldTemp implements JTDField {
    /**
     * 字段中文名
     */
    String value;

    String label;

    //label description构成字段备注
    String description;

    String fieldName;

    /**
     * 数据库数据类型
     */
    JTDConst.EnumFieldType type;

    /**
     * 页面端类型
     */
    JTDConst.EnumFieldUiType uiType;

    /**
     * 长度
     */
    Integer mainLength;

    /**
     * 小数位长度
     */
    Integer decimalLength;

    /**
     * 是否不为空
     */
    JTDConst.EnumFieldNullType notNull;

    /**
     * 默认值
     */
    String defaultValue;

    /**
     * 旧字段名（修改字段名是传递，后续记得手动清理该属性）
     */
    String renameFrom;

    /**
     * 字段对应枚举类
     */
    Class javaTypeEnumClass;

    /**
     * 字段对应枚举类
     */
    Boolean javaTypeEnumClassMultiple;

    /**
     * 是否自增
     */
    Boolean autoIncrement;
    /**
     * 字符集
     */
    String charset;
    /**
     * 属性分组
     */
    String group;
    /**
     * 对应哪个实体类
     */
    String entityName;
    /**
     * 对应哪个实体类
     */
    Class entityClass;
    /**
     * 对应实体类的哪个主键
     */
    String entityClassKey;
    /**
     * 对应实体类的哪个主键说明
     */
    String entityClassLabel;
    /**
     * 当前属性对应实体类的哪个属性
     */
    String entityClassTargetProp;
    /**
     * 扩展属性
     */
    String extendProp;
    /**
     * 是否可排序
     */
    Boolean sortable;
    /**
     * 是否可排序
     */
    Integer sort = JTDConst.INT__1;
    /**
      * 是否不可编辑
      */
    Boolean disabled;

    @Override
    public String value() {
        return value;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public String description() {
        return defaultValue;
    }

    @Override
    public String fieldName() {
        return fieldName;
    }

    @Override
    public JTDConst.EnumFieldType type() {
        return type;
    }

    @Override
    public JTDConst.EnumFieldUiType uiType() {
        return uiType;
    }

    @Override
    public int mainLength() {
        return mainLength;
    }

    @Override
    public int decimalLength() {
        return decimalLength;
    }

    @Override
    public JTDConst.EnumFieldNullType notNull() {
        return notNull;
    }

    @Override
    public String defaultValue() {
        return defaultValue;
    }

    @Override
    public String renameFrom() {
        return renameFrom;
    }

    @Override
    public Class<?> javaTypeEnumClass() {
        return javaTypeEnumClass;
    }

    @Override
    public boolean javaTypeEnumClassMultiple() {
        return javaTypeEnumClassMultiple;
    }

    @Override
    public boolean autoIncrement() {
        return autoIncrement;
    }

    @Override
    public String charset() {
        return charset;
    }

    @Override
    public boolean exist() {
        return false;
    }

    @Override
    public String group() {
        return group;
    }

    public String entityName() {
        return entityName;
    }

    @Override
    public Class entityClass() {
        return entityClass;
    }

    @Override
    public String entityClassKey() {
        return entityClassKey;
    }

    @Override
    public String entityClassLabel() {
        return entityClassLabel;
    }

    @Override
    public String entityClassTargetProp() {
        return entityClassTargetProp;
    }

    @Override
    public String extendProp() {
        return extendProp;
    }

    @Override
    public boolean sortable() {
        return sortable;
    }

    @Override
    public int sort() {
        return sort;
    }

    @Override
    public boolean disabled() {
        return disabled;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return JTDField.class;
    }
}
