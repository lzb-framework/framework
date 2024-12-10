package com.pro.framework.javatodb.model;

import com.pro.framework.javatodb.constant.JTDConst;
import com.pro.framework.javatodb.enums.EnumPositionAlign;
import lombok.Data;

import java.io.Serializable;

@Data
public class JTDFieldInfoDbVo implements Serializable {

    /**
     * 字段名
     */
    private String fieldName;

    /**
     * 字段中文名 + 枚举值说明
     */
    private String label;
    /**
     * 字段中文名
     */
    private String simpleLabel;

    /**
     * 数据库数据类型
     */
    private JTDConst.EnumFieldType type;

    /**
     * 页面端类型
     */
    private JTDConst.EnumFieldUiType uiType;

    /**
     * 长度
     */
    private Integer mainLength;

    /**
     * 小数位长度
     */
    private Integer decimalLength;

    /**
     * 是否不为空
     */
    private JTDConst.EnumFieldNullType notNull;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 是否自增
     */
    private Boolean autoIncrement;
    /**
     * 编码
     */
    private String charset;

    /**
     * 从某个旧字段重命名过来
     */
    private String renameFrom;

    /**
     * 字段对应枚举类(java特有的属性冗余一下)
     */
    private Class<?> javaTypeEnumClass;
    /**
     * 字段对应枚举类多选
     */
    private Boolean javaTypeEnumClassMultiple;

    /**
     * 属性分组
     */
    private String group;
    /**
     * 对应哪个实体名
     */
    private String entityName;
    /**
     * 对应哪个实体类(的主键)
     */
    private Class<?> entityClass;
    /**
     * 对应实体类的哪个主键
     */
    private String entityClassKey;
    /**
     * 对应实体类的哪个主键说明
     */
    private String entityClassLabel;
    /**
     * 对应实体类的哪个主键说明
     */
    private String entityClassTargetProp;
    /**
     * 扩展属性
     */
    private String extendProp;
    /**
     * 对应实体类的哪个主键说明
     */
    private Boolean sortable;
    /**
     * 对应实体类的哪个主键说明
     */
    private Integer sort;
    /**
     * 是否不可编辑
     */
    private Boolean disabled;
    /**
     * 描述
     */
    private String description;
//    /**
//     * 对应哪个实体类(的主键)
//     */
//    transient private String entityName;
    /**
     * 是否可以清理
     */
    transient private Boolean clearable;
    /**
     * 宽度
     */
    transient private Integer width;
    /**
     * 居中
     */
    transient private EnumPositionAlign align;
}
