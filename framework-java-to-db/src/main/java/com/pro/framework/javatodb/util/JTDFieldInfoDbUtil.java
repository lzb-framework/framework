package com.pro.framework.javatodb.util;

import com.pro.framework.enums.EnumUtil;
import com.pro.framework.javatodb.constant.JTDConst;
import com.pro.framework.javatodb.model.JTDFieldInfoDb;
import com.pro.framework.javatodb.model.JTDFieldInfoJava;
import com.pro.framework.javatodb.model.JTDTableInfo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class JTDFieldInfoDbUtil {
    public static final Set<Class<?>> AUTO_INCREASE_TYPE = new HashSet<>(Arrays.asList(Long.class, Integer.class));

    /**
     * java配置项 转 sql配置项
     */
    @SneakyThrows
    public static JTDFieldInfoDb init(JTDTableInfo jtdTable, JTDFieldInfoJava java, Map<String, String> fieldPatternNotNullDefaultValueMap) {
        Set<String> idFields = new HashSet<>(jtdTable.getKeyFieldNames());
        if (idFields.isEmpty()) {
            idFields = Collections.singleton("id");
        }
        //数据库类型根据 java类型默认映射;长度;默认值;添加label(枚举类型)
        JTDFieldInfoDb build = new JTDFieldInfoDb();
        String fieldName = JTDUtil.or(java.getFieldName(), JTDUtil.toUnderlineCase(java.getPropName()));
        build
                .setFieldName(fieldName)
                .setLabel(JTDUtil.or(java.getLabel(), build.getFieldName()))
                .setDefaultValue(JTDUtil.or(java.getDefaultValue(), build.getDefaultValue()))
        ;
        Class<?> javaType = java.getJavaType();
        switch (javaType.getName()) {
            case "java.lang.Long":
                boolean isKeyProp = jtdTable.getKeyFieldNames().contains(fieldName);
//                boolean isKeyProp = jtdTable.getKeyFieldNames().contains(fieldName) || jtdTable.getSequences().stream().filter(s -> JTDConstInner.EnumSequenceType.UNIQUE_KEY.equals(s.getType())).flatMap(s -> s.getFieldNames().stream()).anyMatch(name -> name.equals(fieldName));
                build.setType(JTDConst.EnumFieldType.bigint)
//                        .setMainLength(20)
                        .setNotNull(isKeyProp ? JTDConst.EnumFieldNullType.not_null : JTDConst.EnumFieldNullType.can_null)
                ;
                break;
            case "java.lang.Integer":
                build.setType(JTDConst.EnumFieldType.int_)
//                        .setMainLength(10)
                        .setDefaultValue(JTDUtil.or(JTDUtil.shortNum(build.getDefaultValue()), buildDefaultValueByFieldName(fieldName, fieldPatternNotNullDefaultValueMap), "'0'"))
                ;
                break;
            case "java.lang.String":
            case "java.lang.Character":
                build.setType(JTDConst.EnumFieldType.varchar)
                        .setMainLength(200)
                ;
                build.setType(JTDUtil.or(java.getType(), build.getType()));
                switch (build.getType()) {
                    case longtext:
                    case mediumtext:
                    case tinytext:
                    case text:
                        build.setDefaultValue(null);
                        build.setNotNull(JTDConst.EnumFieldNullType.can_null);
                        break;
                    case char_:
                    case varchar:
                        build.setDefaultValue(JTDUtil.or(JTDUtil.shortNum(build.getDefaultValue()), buildDefaultValueByFieldName(fieldName, fieldPatternNotNullDefaultValueMap), "''"));
                        break;
                }
                break;
            case "java.math.BigDecimal":
            case "java.lang.Float":
            case "java.lang.Double":
                build.setType(JTDConst.EnumFieldType.decimal)
                        .setMainLength(20)
                        .setDecimalLength(6)
                        //数字型默认都去掉小数位后面的小数去匹配
                        .setDefaultValue(JTDUtil.or(JTDUtil.shortNum(build.getDefaultValue()), "'0'"))
                ;
                break;
            case "java.util.Date":
            case "java.time.LocalDateTime":
                build.setType(JTDConst.EnumFieldType.datetime);
                build.setNotNull(JTDConst.EnumFieldNullType.can_null);
                break;
            case "java.time.LocalDate":
                build.setType(JTDConst.EnumFieldType.date);
                break;
            case "java.time.LocalTime":
                build.setType(JTDConst.EnumFieldType.time);
                break;
            case "java.lang.Boolean":
                build.setType(JTDConst.EnumFieldType.tinyint);
                build.setMainLength(1);
                build.setDefaultValue(JTDUtil.or(JTDUtil.shortNum(build.getDefaultValue()), buildDefaultValueByFieldName(fieldName, fieldPatternNotNullDefaultValueMap), "'0'"));
                break;
            default:
                if (javaType.isEnum()) {
//                    if (IEnum.class.isAssignableFrom(javaType) && Integer.class.isAssignableFrom(javaType.getDeclaredMethod("getValue").getReturnType())) {
//                        build.setType(JTDConst.EnumFieldType.tinyint);
//                    } else {
                    build.setType(JTDConst.EnumFieldType.varchar);
                    build.setMainLength(32);
//                    }
                    //noinspection unchecked,rawtypes
                    Map<Serializable, String> nameLabelMap = EnumUtil.getNameLabelMap((Class) javaType);
                    String IEnumStr = nameLabelMap
                            .entrySet().stream().map(e -> JTDUtil.isNotBlank(e.getValue()) ? (e.getKey() + "-" + e.getValue()) : e.getKey().toString()).collect(Collectors.joining(" "));
                    build.setLabel(build.getLabel() + " " + JTDUtil.sub(IEnumStr, 400));
                    //若枚举没指定默认值，取第一个做默认值
                    build.setDefaultValue(JTDUtil.or(build.getDefaultValue(), "'" + nameLabelMap.keySet().iterator().next().toString()) + "'");
                } else {
//                    throw new JTDException("未知的字段类型" + javaType);
                    log.warn("未知的字段类型|{}|{}|{}", jtdTable.getTableName(), fieldName, javaType);
                    return null;
                }
        }


        JTDConst.EnumFieldType type = JTDUtil.or(java.getType(), build.getType());
//        if("state".equals(fieldName)){
//            int i = 0;
//        }
        build
                .setGroup(JTDUtil.or(java.getGroup(), build.getGroup()))
                .setEntityClass(Object.class.equals(java.getEntityClass()) ? build.getEntityClass() : java.getEntityClass())
                .setEntityClassKey(JTDUtil.or(java.getEntityClassKey(), build.getEntityClassKey()))
                .setEntityClassLabel(JTDUtil.or(java.getEntityClassLabel(), build.getEntityClassLabel()))
                .setEntityClassTargetProp(JTDUtil.or(java.getEntityClassTargetProp(), build.getEntityClassTargetProp()))
                .setExtendProp(JTDUtil.or(java.getExtendProp(), build.getExtendProp()))
                .setSortable(JTDUtil.or(java.getSortable(), build.getSortable()))
                .setSort(orInt(java.getSort(), build.getSort()))
                .setDisabled(JTDUtil.or(java.getDisabled(), build.getDisabled()))
                .setSimpleLabel(JTDUtil.or(java.getSimpleLabel(), java.getPropName()))
                .setJavaTypeEnumClass(java.getJavaTypeEnumClass())
                .setJavaTypeEnumClassMultiple(java.getJavaTypeEnumClassMultiple())
                .setType(type)
                .setUiType(getUiType(fieldName, JTDUtil.or(java.getUiType(), build.getUiType()), type, javaType, build.getEntityClass()))
                .setMainLength(orInt(java.getMainLength(), build.getMainLength()))
                .setDecimalLength(orInt(java.getDecimalLength(), build.getDecimalLength()))
                .setNotNull(JTDUtil.or(java.getNotNull(), build.getNotNull(), JTDConst.EnumFieldNullType.not_null))
                .setRenameFrom(JTDUtil.or(java.getRenameFrom(), build.getRenameFrom()))
                .setCharset(JTDUtil.or(java.getCharset(), build.getCharset()))
                .setAutoIncrement(idFields.contains(fieldName) && AUTO_INCREASE_TYPE.contains(javaType) ? java.getAutoIncrement() : false)
                .setDefaultValue(JTDUtil.or(appendDefault(java.getDefaultValue()), appendDefault(build.getDefaultValue()), buildDefaultValueByFieldName(fieldName, fieldPatternNotNullDefaultValueMap)).trim())
        ;
        switch (type) {
            case longtext:
            case mediumtext:
            case tinytext:
            case text:
                build.setMainLength(null);
                break;
            case char_:
            case varchar:
                break;
            case tinyint:
            case smallint:
            case mediumint:
            case int_:
            case integer:
            case bigint:
            case float_:
            case double_:
            case decimal:
                //这些类型默认是NULL
                if (JTDConst.EnumFieldNullType.can_null.equals(build.getNotNull()) && "".equals(build.getDefaultValue())) {
                    build.setDefaultValue("NULL");
                }
                break;
            case date:
            case time:
            case year:
            case datetime:
            case timestamp:
                //这些类型默认是NULL
                if (JTDConst.EnumFieldNullType.can_null.equals(build.getNotNull()) && "".equals(build.getDefaultValue())) {
                    build.setDefaultValue("NULL");
                }
                break;
        }
//        if (jtdTable.getTableName().equals("user_recharge") && fieldName.equals("update_time")) {
//            int i = 0;
//        }
        return build;
    }


    //    @SafeVarargs
//    @SneakyThrows
    public static Integer orInt(Integer... ss) {
        if (ss == null) {
            return null;
        }
        return Arrays.stream(ss).filter(Objects::nonNull).filter(s -> JTDConst.INT__1 != s).findFirst().orElse(ss[ss.length - 1]);
    }


    private static JTDConst.EnumFieldUiType getUiType(String fieldName, JTDConst.EnumFieldUiType uiType, JTDConst.EnumFieldType type, Class<?> javaType, Class<?> entityClass) {
        if (uiType != null && !JTDConst.EnumFieldUiType.none.equals(uiType)) {
            return uiType;
        }
//        || (null != entityClass && !Object.class.equals(entityClass))
        if (javaType.isEnum()) {
            return JTDConst.EnumFieldUiType.select;
        }
        switch (type) {
            case longtext:
            case mediumtext:
            case tinytext:
            case text:
                return JTDConst.EnumFieldUiType.richText;
            case char_:
            case varchar:
                return getEnumFieldUiType(fieldName, javaType, entityClass, JTDConst.EnumFieldUiType.text);
            case tinyint:
                return JTDConst.EnumFieldUiType.bool;
            case bigint:
                return getEnumFieldUiType(fieldName, javaType, entityClass, JTDConst.EnumFieldUiType.number);
            case smallint:
            case mediumint:
            case int_:
            case integer:
            case float_:
            case double_:
            case decimal:
                return JTDConst.EnumFieldUiType.number;
            case date:
                return JTDConst.EnumFieldUiType.date;
            case time:
                return JTDConst.EnumFieldUiType.time;
            // case year:
            case datetime:
                return JTDConst.EnumFieldUiType.datetime;
            case timestamp:
                return JTDConst.EnumFieldUiType.datetime;
        }
        return JTDConst.EnumFieldUiType.none;
    }

    private static JTDConst.EnumFieldUiType getEnumFieldUiType(String fieldName, Class<?> javaType, Class<?> entityClass, JTDConst.EnumFieldUiType defaultType) {
        if (null == entityClass || Object.class.equals(entityClass)) {
            // 非实体类的关联Id
            if (null != fieldName && (fieldName.endsWith("_id") || fieldName.equals("id") || (javaType == Long.class && fieldName.contains("id")))) {
                return JTDConst.EnumFieldUiType.hide;
            } else {
                return defaultType;
            }
        } else {
            // 有关联实体
            return JTDConst.EnumFieldUiType.select;
        }
    }

    //关键字
    private static final Set<String> keys = new HashSet<>(Arrays.asList("CURRENT_TIMESTAMP", "current_timestamp", "NULL", "NOT NULL", "null", "not null"));

    public static String appendDefault(String str) {
        if (JTDUtil.isBlank(str)) {
            return "";
        }
        if (!keys.contains(str) && !str.startsWith("'")) {
            str = "'" + str + "'";
        }
        return str;
        // return "DEFAULT " + str;
    }


    private static String buildDefaultValueByFieldName(String fieldName, Map<String, String> fieldPatternNotNullDefaultValueMap) {
        for (String fieldNamePattern : fieldPatternNotNullDefaultValueMap.keySet()) {
            if (fieldName.matches(fieldNamePattern)) {
                return fieldPatternNotNullDefaultValueMap.get(fieldNamePattern);
            }
        }
        return "";
    }

    /**
     * sql 转 sql配置项
     *
     * @param fieldConfigSql 例如: `prerogative` varchar(1024) DEFAULT NULL COMMENT '会员卡特权说明,限制1024汉字'
     * @param renameFrom     prerogativeOld
     * @param field          field
     */
    public static JTDFieldInfoDb init(String fieldConfigSql, String renameFrom, Field field) {
        String[] infos = fieldConfigSql.split(" ");
        JTDAssertUtil.notNull(infos.length >= 4, "@JTDFieldSql#fieldConfigSql(),至少要包含类似如下几部分信息: `prerogative` varchar" +
                "(1024) DEFAULT NULL COMMENT '会员卡特权说明,限制1024汉字'");

        int currentIndex = 0;
        String fieldName = infos[currentIndex].replaceAll("`", "");
        if (null != field && !JTDUtil.toUnderlineCase(field.getName()).equals(fieldName)) {
            log.info("实体类对应字段名{} 与 @JTDFieldSql内字段名{} 不匹配", JTDUtil.toUnderlineCase(field.getName()), fieldConfigSql);
        }

        ++currentIndex;
        String typeName = infos[currentIndex].split("\\(")[0];
        JTDConst.EnumFieldType type = JTDConst.EnumFieldType.MAP.get(typeName);
        JTDAssertUtil.notNull(type, "暂未扩展的sql字段类型：" + typeName);
        String[] lengths = infos[currentIndex].contains("(") ? infos[currentIndex].split("\\(")[1].split("\\)")[0].split(",") : null;
        Integer mainLength = null != lengths ? Integer.valueOf(lengths[0]) : null;
        Integer decimalLength = null != lengths && lengths.length > 1 ? Integer.valueOf(lengths[1]) : null;
        ++currentIndex;

        // COLLATE utf8mb4_general_ci
        String charset = null;
        if ("COLLATE".equals(infos[currentIndex])) {
            charset = infos[currentIndex + 1];
            ++currentIndex;
            ++currentIndex;
        }
        boolean notNull = "NOT NULL".equals(infos[currentIndex] + " " + infos[currentIndex + 1]);
        if (notNull) {
            ++currentIndex;
            ++currentIndex;
        }
        String defaultValue = "";
        if (fieldConfigSql.contains("DEFAULT")) {
            //&& !"NULL".equals(infos[currentIndex + 1])
            defaultValue = "DEFAULT".equals(infos[currentIndex]) ? infos[currentIndex + 1] : "";
            if (defaultValue.startsWith("'") && infos[currentIndex + 2].contains("'")) {
                // 例如 DEFAULT '1970-01-01 00:00:00'
                defaultValue = infos[currentIndex + 1] + " " + infos[currentIndex + 2];
                ++currentIndex;
            }
            ++currentIndex;
            ++currentIndex;
            if ("".equals(defaultValue)) {
                defaultValue = "''";// 注解里 "''"对应空字符的情况  注解里 "" 对应没配置的情况
            }
        }
        // else {
        //     defaultValue = "DEFAULT NULL";
        // }
//        if ("high_price".equals(fieldName)) {
//            int i = 0;
//        }

        Boolean autoIncrement = fieldConfigSql.contains(" AUTO_INCREMENT");
        String label = null;
        if (fieldConfigSql.contains("CHARACTER SET ")) {
            charset = fieldConfigSql.split("CHARACTER SET ")[1];
            charset = charset.substring(0, charset.indexOf(" "));
        }
        if (fieldConfigSql.contains("COMMENT '")) {
            String commentLast = fieldConfigSql.split("COMMENT '")[1];
            label = commentLast.substring(0, commentLast.lastIndexOf("'"));
            label = label.replaceAll("''", "'");
        }

        //noinspection SwitchStatementWithTooFewBranches
        switch (type) {
            case decimal:
                if (!defaultValue.isEmpty()) {
                    //删掉尾数为0的字符,结尾如果是小数点，则去掉
                    defaultValue = defaultValue.replaceAll("'", "");
                    if (JTDUtil.isNum(defaultValue)) {
                        defaultValue = "'" + JTDUtil.shortNum(defaultValue) + "'";
                    }
                }
                break;
            default:
        }
        // defaultValue = defaultValue.length() == 0 ? "" : "DEFAULT " + defaultValue;
        JTDFieldInfoDb fieldInfo = new JTDFieldInfoDb();
        fieldInfo.setFieldName(JTDUtil.toUnderlineCase(fieldName));
        fieldInfo.setLabel(label);
        fieldInfo.setType(type);
        fieldInfo.setMainLength(mainLength);
        fieldInfo.setDecimalLength(decimalLength);
        fieldInfo.setNotNull(notNull ? JTDConst.EnumFieldNullType.not_null : JTDConst.EnumFieldNullType.can_null);
        fieldInfo.setDefaultValue(defaultValue);
        fieldInfo.setAutoIncrement(autoIncrement);
        fieldInfo.setCharset(charset);
        fieldInfo.setRenameFrom(renameFrom);
        // sql 转 sql配置项
        return fieldInfo;
    }
}
