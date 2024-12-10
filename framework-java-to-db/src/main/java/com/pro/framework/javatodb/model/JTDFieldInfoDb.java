package com.pro.framework.javatodb.model;

import com.pro.framework.javatodb.constant.JTDConst;
import com.pro.framework.javatodb.enums.EnumPositionAlign;
import com.pro.framework.javatodb.util.ClientPreparedQueryBindings;
import com.pro.framework.javatodb.util.JTDException;
import com.pro.framework.javatodb.util.JTDUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author administrator
 */
@Data
//@SuperBuilder
@NoArgsConstructor
@Accessors(chain = true)
@Slf4j
@ToString
public class JTDFieldInfoDb implements ISqlSegmentInfo, Serializable {

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
     * 排序
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
    /**
     * 对应哪个实体类(的主键)
     */
    transient private String entityName;
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

    /**
     * sql配置项 转 sql
     *
     * @return sql 例如: `prerogative` varchar(1024) DEFAULT NULL COMMENT '会员卡特权说明,限制1024汉字'
     */
    public String toSql(String tableName) {
        return Stream.of(
                JTDUtil.format("`{0}`", fieldName),
                toSql_type(type, mainLength, decimalLength),
                JTDConst.EnumFieldNullType.not_null.equals(notNull) ? "NOT NULL" : "",
                toSql_default(JTDConst.EnumFieldNullType.not_null.equals(notNull), defaultValue, tableName, fieldName),
                (autoIncrement ? "AUTO_INCREMENT" : ""),
                (null != charset ? "CHARACTER SET " + charset : ""),
                JTDUtil.format("COMMENT ''{0}''", ClientPreparedQueryBindings.escapeSqlSpecialChar(label))
        ).filter(JTDUtil::isNotBlank).collect(Collectors.joining(" "));
    }

    private static String toSql_type(JTDConst.EnumFieldType type, Integer mainLength, Integer decimalLength) {
        switch (type) {
            case varchar:
            case tinyint:
            case smallint:
            case mediumint:
            case int_:
            case integer:
            case bigint:
            case timestamp:
                return type.getValue() + mainLength(mainLength, decimalLength);
            case float_:
            case double_:
            case decimal:
                return type.getValue() + mainLength(mainLength, decimalLength);

        }
        return type.getValue();
    }

    private static String mainLength(Integer mainLength, Integer decimalLength) {
        if (null == mainLength) {
            return "";
        }
        String numAppend = Stream.of(mainLength, decimalLength).filter(Objects::nonNull).map(Object::toString).collect(Collectors.joining(","));
        return JTDUtil.isBlank(numAppend) ? "" : "(" + numAppend + ")";
    }

    @SneakyThrows
    private static String toSql_default(Boolean notNull, String defaultValue, String tableName, String fieldName) {
        if (notNull && "NULL".equals(defaultValue)) {
            throw new JTDException(tableName + "." + fieldName + "字段 NOT NULL 但是 DEFAULT NULL ");
        }
        return defaultValue.length() == 0 ? "" : "DEFAULT " + defaultValue;
    }
}
