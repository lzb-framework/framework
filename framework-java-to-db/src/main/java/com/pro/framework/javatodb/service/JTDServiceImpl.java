package com.pro.framework.javatodb.service;

import com.pro.framework.api.entity.IEntityProperties;
import com.pro.framework.api.util.AssertUtil;
import com.pro.framework.api.util.JSONUtils;
import com.pro.framework.enums.EnumUtil;
import com.pro.framework.javatodb.annotation.JTDField;
import com.pro.framework.javatodb.annotation.JTDFieldSql;
import com.pro.framework.javatodb.annotation.JTDTable;
import com.pro.framework.javatodb.config.JTDConfig;
import com.pro.framework.javatodb.config.JTDProperties;
import com.pro.framework.javatodb.constant.JTDConst;
import com.pro.framework.javatodb.constant.JTDConstInner;
import com.pro.framework.javatodb.model.*;
import com.pro.framework.javatodb.util.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * {@link JTDConfig}
 * 从早到晚
 * Construct >> @Autowired(依赖注入) >> @postConstruct >> InitializingBean.afterPropertiesSet > init-method(XML模式下常用)
 * Construct >> @Autowired(依赖注入) >> @postConstruct >> InitializingBean.afterPropertiesSet > CommandLineRunner >ApplicationRunner > ApplicationListener
 *
 * @author administrator
 */
@Slf4j
public class JTDServiceImpl implements IJTDService {

    private final JTDProperties jtdProperties;
    private final JTDAdaptor adaptor;
    private final IEntityProperties entityProperties;
    private static final JTDTable jtdTableExample = ExampleClazz.class.getAnnotation(JTDTable.class);
    private static final JTDField jtdFieldExample = ExampleClazz.class.getDeclaredFields()[0].getAnnotation(JTDField.class);

    // @Autowired
    public JTDServiceImpl(JTDProperties jtdProperties, JTDAdaptor adaptor, IEntityProperties entityProperties) {
        this.adaptor = adaptor;
        this.jtdProperties = jtdProperties;
        this.entityProperties = entityProperties;
    }

    // implements InitializingBean
    // @Override
    // @PostConstruct
    @Override
    public void executeSql() {
        executeSql(jtdProperties, adaptor);
    }

    public static void executeSql(JTDProperties jtdProperties, JTDAdaptor adaptor) {
        JTDConst.EnumSqlRunType currRunType = jtdProperties.getRunType();
        Map<String, String> fieldPatternNotNullDefaultValueMap = jtdProperties.getFieldPatternNotNullDefaultValueMap();
        if (JTDConst.EnumSqlRunType.none.equals(currRunType)) {
            return;
        }
        log.info("初始化表结构开始");
        long start = System.currentTimeMillis();
        if (JTDConst.EnumSqlRunType.createModifyDeleteAll.equals(jtdProperties.getRunType())) {
            log.warn("初始化表结构,包含删除字段的sql语句(只在本地执行,不在生产环境上");
        }
        try {
            adaptor.executeQuery("select 1 from dual;", o -> {
                try {
                    return o.getString(1);
                } catch (SQLException e) {
                }
                return null;
            });
        } catch (Exception e) {
            String ERROR = "Unknown database ";
            String message = e.getMessage();
            if (message.startsWith(ERROR)) {
//                    throw new RuntimeException(e);
                String substring = message.substring(ERROR.length());
                String dbName = substring.substring(1, substring.length() - 1);
                boolean execute = adaptor.createDatabase(dbName);
                AssertUtil.isTrue(execute,"create database " + dbName+" error");
            }
        }

        Integer size = executeSql(jtdProperties.getBasePackages(), adaptor, currRunType, fieldPatternNotNullDefaultValueMap);

        long end = System.currentTimeMillis();
        log.warn("初始化表结构完毕，涉及 {} 张表,耗时{}ms", size, (end - start));
    }

    /**
     * 分析表和字段信息,对比生成DDL SQL,并执行
     */
    public static Integer executeSql(String[] basePackages, JTDAdaptor adaptor, JTDConst.EnumSqlRunType currRunType, Map<String, String> fieldPatternNotNullDefaultValueMap) {

        //1.从代码中解析最新表结构(hashCode有变化的)
        List<JTDTableInfo> tableInfosInCode = analyzeNewTableInfoFromCodes(basePackages, fieldPatternNotNullDefaultValueMap);
        // tableInfosInCode.forEach(t -> {
        //     t.setModule(null);
        //     t.setEntityId(null);
        // });
        tableInfosInCode.stream().flatMap(t -> t.getFields().stream()).forEach(f -> {
            //无需对比(数据库肯定没有的属性),不重要的属性,忽略
            f.setSimpleLabel(null);
            f.setJavaTypeEnumClass(null);
            f.setJavaTypeEnumClassMultiple(null);
            f.setUiType(null);
            f.setGroup(null);
            f.setEntityClass(null);
            f.setEntityClassKey(null);
            f.setEntityClassLabel(null);
            f.setEntityClassTargetProp(null);
            f.setExtendProp(null);
            f.setSortable(null);
            f.setSort(null);
            f.setDisabled(null);
        });

        //2.从数据库中,查询旧表 (与新表有关系的旧表)
        Map<String, JTDTableInfo> oldTableInfoInNewMap = tableInfosInCode.stream().map(JTDTableInfo::getTableName)
                .map(tableName -> {
                    String createTableSql = getCreateTableSql(adaptor, tableName);
                    if (JTDUtil.isNotBlank(createTableSql)) {
                        return JTDTableInfo.init(createTableSql);
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toMap(JTDTableInfo::getTableName, o -> o));

        //3. 对比表和字段信息,有变化的字段/片段,生成DDL SQL语句
        List<List<List<JTDSqlInfo>>> sqlInfosss = tableInfosInCode.stream().map(newTableInfo -> {
            JTDTableInfo oldTableInfo = oldTableInfoInNewMap.get(newTableInfo.getTableName());
            // 库里不存在表,建表
            if (oldTableInfo == null) {
                return Collections.singletonList(Collections.singletonList(new JTDSqlInfo(JTDConst.EnumSqlRunType.createTable, newTableInfo.toCreateTableSql())));
            }
            // 库里存在表,对比更新表信息,字段信息
            else {
                // EnumTableMetaType 表的ddl信息分块类型
                return Arrays.stream(JTDConstInner.EnumTableMetaType.values()).map(JTDConstInner.EnumTableMetaType::getMetaService)
                        .filter(Objects::nonNull)
                        // 判断 数据库 和 实体类信息 里的表信息是否不一致
                        .map(metaService -> Objects.equals(metaService.getVersionSequence(oldTableInfo), metaService.getVersionSequence(newTableInfo))
                                //不一致就 生成新增字段/修改字段,表元素...的DDL语句
                                ? null : calDiffSql(newTableInfo, oldTableInfo, metaService))
                        .filter(Objects::nonNull).collect(Collectors.toList());
            }
        }).collect(Collectors.toList());

//        log.info("sqlInfosss= {}", JSONUtils.toString(sqlInfosss));

        //整理和过滤不执行的sql
        List<List<List<JTDSqlInfo>>> sqlInfosssExecute = sqlInfosss.stream()
                .map(sqlInfoss -> sqlInfoss.stream()
                        .map(sqlInfos -> sqlInfos.stream()
                                //过滤sql执行类型
                                .filter(sqlInfo -> filterRunnerType(currRunType, sqlInfo))
                                .collect(Collectors.toList()))
                        .filter(JTDUtil::notEmpty).collect(Collectors.toList()))
                .filter(JTDUtil::notEmpty).collect(Collectors.toList());

        List<JTDSqlInfo> sqlInfos = sqlInfosssExecute.stream().flatMap(ll -> ll.stream().flatMap(Collection::stream)).collect(Collectors.toList());
        for (JTDSqlInfo sqlInfo : sqlInfos) {
            String sql = sqlInfo.getContent();
            try {
                // -- 执行sql
                log.warn("执行: " + sql);
                adaptor.execute(sql);
            } catch (Exception e) {
                String message = e.getMessage();
//                if ("Data truncation: Invalid use of NULL value".equals(message)) {
//                    message += "   " + getNotnullSql(sql);
//                }
                sqlInfo.setErrorInfo(message);
            }
        }
        String errorLogs = sqlInfos.stream().filter(s -> null != s.getErrorInfo()).map(sqlInfo -> "     -- error: " + sqlInfo.getErrorInfo() + "\n   " + sqlInfo.getContent() + ";").collect(Collectors.joining("\n"));
        if (!errorLogs.isEmpty()) {
            log.error("\n-- 执行异常:\n\n{}\n\n", errorLogs);
        }

        return sqlInfosssExecute.size();
    }

    //               String alterTableSql = "ALTER TABLE `user_bank_card` CHANGE COLUMN `bank_account` `bank_account` varchar(200) NOT NULL DEFAULT '' COMMENT '卡号';";
//    public static String getNotnullSql(String alterTableSql) {
//
//        // 使用正则表达式提取表名和字段名
//        Pattern pattern = Pattern.compile("ALTER TABLE `([^`]+)` CHANGE COLUMN `([^`]+)` `([^`]+)`");
//        Matcher matcher = pattern.matcher(alterTableSql);
//
//        String tableName = null;
//        String columnName = null;
//
//        if (matcher.find()) {
//            tableName = matcher.group(1);
//            columnName = matcher.group(3);
//        }
//
//        // 生成 UPDATE 语句
//        if (tableName != null && columnName != null) {
//            String updateSql = String.format("UPDATE %s SET %s = '' WHERE %s IS NULL;", tableName, columnName, columnName);
//            return updateSql;
//        } else {
//            System.out.println("Failed to extract table and column names from the ALTER TABLE statement.");
//        }
//        return "";
//    }

    private static boolean filterRunnerType(JTDConst.EnumSqlRunType currRunType, JTDSqlInfo sqlInfo) {
        return currRunType.equals(sqlInfo.getRunType()) || currRunType.getContainsMore().contains(sqlInfo.getRunType());
    }

    private static List<JTDSqlInfo> calDiffSql(JTDTableInfo newTableInfo, JTDTableInfo oldTableInfo, JTDTableMetaService metaService) {
//        log.info("新旧信息有区别 {} {} \n old={} \nnew={} \n", oldTableInfo.getTableName(), metaService.getClass().getSimpleName(), oldTableInfo, newTableInfo);
        return metaService.calculateAlterTableSqls(oldTableInfo, newTableInfo);
    }

    private static String getCreateTableSql(JTDAdaptor adaptor, String tableName) {
        String s = null;
        try {
            s = adaptor.executeQuery("show create table " + tableName, (JTDServiceImpl::getRsString1)).stream().findFirst().orElse("");
        } catch (Throwable e) {
            if (null == e.getMessage() || !e.getMessage().endsWith("' doesn't exist")) {
                log.error("getCreateTableSql error {} ", tableName, e);
            }
        }
        return s;
    }


    /**
     * 读取java中的表信息
     */
    @Override
    public JTDTableInfoVo readTableInfo(Class<?> clazz) {
        if (entityProperties != null) {
            clazz = entityProperties.getEntityClassReplaceMap().getOrDefault(clazz.getSimpleName(), clazz);
        }
        JTDTableInfo jtdTableInfo = buildTableFullInfoFromClass(clazz, jtdProperties.getFieldPatternNotNullDefaultValueMap());

        JTDTableInfoVo tableInfoVo = new JTDTableInfoVo();
        if (jtdTableInfo == null) {
            log.info("readTableInfo null {}", clazz);
//            tableInfoVo.setFields(Collections.emptyList());
//            return tableInfoVo;
            return null;
        }
        JTDUtil.copyProperties(jtdTableInfo, tableInfoVo);
        tableInfoVo.setFields(jtdTableInfo.getFields().stream().map(f -> {
            JTDFieldInfoDbVo field = new JTDFieldInfoDbVo();
            JTDUtil.copyProperties(f, field);
            return field;
        }).collect(Collectors.toList()));
        return tableInfoVo;
    }
//    /**
//     * 读取java中的表信息
//     */
//    public List<JTDTableInfo> readTableInfos() {
//        return Arrays.stream(jtdProperties.getBasePackages()).flatMap(pack -> ClassScanner.scanClasses(pack).stream())
//                .map(clazz -> buildTableInfoFromClass(clazz, jtdProperties.getFieldPatternNotNullDefaultValueMap()))
//                .filter(Objects::nonNull).collect(Collectors.toList());
//    }

    /**
     * 从代码中解析最新表结构(hashCode有变化的)
     */
    private static List<JTDTableInfo> analyzeNewTableInfoFromCodes(String[] basePackages, Map<String, String> fieldPatternNotNullDefaultValueMap) {
        return Arrays.stream(basePackages).flatMap(pack -> ClassScanner.scanClasses(pack).stream())
                .map(clazz -> buildTableFullInfoFromClass(clazz, fieldPatternNotNullDefaultValueMap))
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static JTDTableInfo buildTableFullInfoFromClass(Class<?> entityClass, Map<String, String> fieldPatternNotNullDefaultValueMap) {
        JTDTableInfo jtdTableInfo = buildTableInfoFromClass(entityClass);
        if (jtdTableInfo != null) {
            List<JTDFieldInfoDb> fields = buildTableFields(entityClass, fieldPatternNotNullDefaultValueMap, jtdTableInfo, jtdTableInfo.getTableName());
            jtdTableInfo.setFields(fields);
        }
        return jtdTableInfo;
    }

    public static JTDTableInfo buildTableInfoFromClass(Class<?> entityClass) {
        JTDTableTemp jtdTable = getTableAnnotation(entityClass);
        if (jtdTable != null) {
            String tableName = JTDUtil.or(jtdTable.tableName(), JTDUtil.toUnderlineCase(entityClass.getSimpleName()));
            jtdTable.setTableName(tableName);
            return JTDTableInfo.builder()
                    .entityName(JTDUtil.firstToLowerCase(entityClass.getSimpleName()))
                    .tableName(tableName)
                    .label(JTDUtil.append(" ", jtdTable.getLabel(), jtdTable.getDescription()))
                    .keyFieldNames(Arrays.stream(jtdTable.keyFieldNames()).collect(Collectors.toList()))
                    .excludeFieldNames(Arrays.stream(jtdTable.excludeFieldNames()).collect(Collectors.toSet()))
                    .sequences(Arrays.stream(jtdTable.sequences()).map(JTDServiceImpl::sqlFormat).map(JTDSequenceInfo::init).filter(Objects::nonNull).collect(Collectors.toList()))
                    .module(jtdTable.module())
                    .entityId(jtdTable.entityId())
                    .description(jtdTable.getDescription())
                    .build();
        }
        return null;
    }

    private static List<JTDFieldInfoDb> buildTableFields(Class<?> entityClass, Map<String, String> fieldPatternNotNullDefaultValueMap, JTDTableInfo jtdTable, String tableName) {
        List<JTDFieldInfoDb> fields = JTDUtil.getAllFields(entityClass).stream().map(field -> {
            JTDFieldSql annotationFieldSql = AnnotationUtils.getAnnotation(field, JTDFieldSql.class);
            if (annotationFieldSql != null) {
                return JTDFieldInfoDbUtil.init(JTDServiceImpl.sqlFormat(annotationFieldSql.fieldConfigSql()), annotationFieldSql.renameFrom(), field);
            } else {
                JTDFieldTemp annotationField = getFieldAnnotation(field);
                Set<String> excludeFieldNames = jtdTable.getExcludeFieldNames();
                if (null != annotationField && !excludeFieldNames.contains(field.getName())) {
                    JTDFieldInfoJava fieldInfo = new JTDFieldInfoJava();
                    fieldInfo.setPropName(field.getName());
                    fieldInfo.setJavaType(field.getType());
                    fieldInfo.setJavaTypeEnumClass(JTDUtil.or((Object.class.equals(annotationField.getJavaTypeEnumClass()) ? null : annotationField.getJavaTypeEnumClass()), field.getType().isEnum() ? field.getType() : null));
                    fieldInfo.setJavaTypeEnumClassMultiple(annotationField.getJavaTypeEnumClassMultiple());
                    fieldInfo.setFieldName(defaultNull(annotationField.fieldName()));
                    fieldInfo.setLabel(appendLabel(annotationField.getLabel(), annotationField.getDescription(), field.getType()));
                    fieldInfo.setSimpleLabel(defaultNull(annotationField.getLabel()));
                    fieldInfo.setType(defaultNull(annotationField.type()));
                    fieldInfo.setUiType(defaultNull(annotationField.uiType()));
                    fieldInfo.setMainLength(defaultNull(annotationField.mainLength()));
                    fieldInfo.setDecimalLength(defaultNull(annotationField.decimalLength()));
                    fieldInfo.setNotNull(defaultNull(annotationField.notNull()));
                    fieldInfo.setDefaultValue(defaultNull(annotationField.defaultValue()));
                    fieldInfo.setCharset(defaultNull(annotationField.getCharset()));
                    fieldInfo.setAutoIncrement(defaultNull(annotationField.autoIncrement()));
                    fieldInfo.setRenameFrom(defaultNull(JTDUtil.toUnderlineCase(annotationField.renameFrom())));
                    fieldInfo.setGroup(defaultNull(annotationField.group()));
                    fieldInfo.setEntityName(defaultNull(annotationField.entityName()));
                    fieldInfo.setEntityClass(defaultNull(annotationField.entityClass()));
                    fieldInfo.setEntityClassKey(defaultNull(annotationField.entityClassKey()));
                    fieldInfo.setEntityClassLabel(defaultNull(annotationField.entityClassLabel()));
                    fieldInfo.setEntityClassTargetProp(defaultNull(annotationField.entityClassTargetProp()));
                    fieldInfo.setExtendProp(defaultNull(annotationField.extendProp()));
                    fieldInfo.setSortable(defaultNull(annotationField.sortable()));
                    fieldInfo.setSort(defaultNull(annotationField.sort()));
                    fieldInfo.setDisabled(defaultNull(annotationField.disabled()));
                    fieldInfo.setDescription(defaultNull(annotationField.description()));
                    return JTDFieldInfoDbUtil.init(
                            jtdTable,
                            fieldInfo,
                            fieldPatternNotNullDefaultValueMap
                    );
                }
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        Set<String> fieldSet = fields.stream().map(JTDFieldInfoDb::getFieldName).collect(Collectors.toSet());
        for (String keyField : jtdTable.getKeyFieldNames()) {
            JTDAssertUtil.true_(fieldSet.contains(keyField), "{0} 表没有{1}字段(主键),可以配置 @JTDTable(value=.., keyFieldNames=\"code\") 来调整主键", JTDUtil.underlineToCamel(tableName), keyField);
        }
        return fields;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static String appendLabel(String label, String description, Class javaType) {
        if (null != description && !description.isEmpty()) {
            return description;
        }
        StringBuilder valueLabel = new StringBuilder();
        if (javaType.isEnum()) {
            valueLabel = new StringBuilder(javaType.getSimpleName() + " ");
            Map<Serializable, String> map = EnumUtil.getNameLabelMap(javaType);
            for (Serializable key : map.keySet()) {
                String value = map.get(key);
                valueLabel.append(" ").append(key).append("-").append(value);
            }
        } else if (javaType.equals(Boolean.class)) {
//            if (label != null && !(label.contains("0") && label.contains("1"))) {
//                valueLabel.append("0-否 1-是");
//            }
        }
        return JTDUtil.append(" ", label, valueLabel.substring(0, Math.min(valueLabel.length(), 512)));
//        return JTDUtil.append(" ", label, valueLabel.substring(0, Math.min(valueLabel.length(), 512)), description);
    }


    private static JTDTableTemp getTableAnnotation(Class<?> entityClass) {
        JTDTableTemp jtdTableTemp = new JTDTableTemp();
        initProperties(jtdTableTemp);
        ApiModel annotation2 = AnnotationUtils.getAnnotation(entityClass, ApiModel.class);
        JTDTable annotation = AnnotationUtils.getAnnotation(entityClass, JTDTable.class);
        if (annotation == null && annotation2 == null) {
            return null;
        }
        if (annotation2 != null) {
            jtdTableTemp.setLabel(annotation2.description());
        }
        if (annotation != null) {
            copyProperties(annotation, jtdTableTemp);
        }
        return jtdTableTemp;
    }

    private static JTDFieldTemp getFieldAnnotation(Field field) {
        JTDFieldTemp jtdFieldTemp = new JTDFieldTemp();
        initProperties(jtdFieldTemp);
        ApiModelProperty annotation2 = AnnotationUtils.getAnnotation(field, ApiModelProperty.class);
        if (annotation2 != null) {
            jtdFieldTemp.setLabel(annotation2.value());
            jtdFieldTemp.setDescription(annotation2.notes());
        }
        JTDField annotation = AnnotationUtils.getAnnotation(field, JTDField.class);
        if (annotation != null) {
            if (!annotation.exist()) {
                return null;
            }
            copyProperties(annotation, jtdFieldTemp);
            return jtdFieldTemp;
        }
        return jtdFieldTemp;
    }


    public static void copyProperties(JTDField from, JTDFieldTemp to) {
        if (JTDUtil.isNotBlank(from.label())) {
            to.setLabel(from.label());
        }
        if (JTDUtil.isNotBlank(from.description())) {
            to.setDescription(from.description());
        }
        to.setAutoIncrement(from.autoIncrement());
        to.setDecimalLength(from.decimalLength());

        if (JTDUtil.isNotBlank(from.defaultValue())) {
            to.setDefaultValue(from.defaultValue());
        }
        if (JTDUtil.isNotBlank(from.charset())) {
            to.setCharset(from.charset());
        }

        if (JTDUtil.isNotBlank(from.fieldName())) {
            to.setFieldName(from.fieldName());
        }
        to.setJavaTypeEnumClass(from.javaTypeEnumClass());
        to.setJavaTypeEnumClassMultiple(from.javaTypeEnumClassMultiple());
        to.setNotNull(from.notNull());
        to.setMainLength(from.mainLength());
        if (JTDUtil.isNotBlank(from.renameFrom())) {
            to.setRenameFrom(from.renameFrom());
        }
        if (!JTDConst.EnumFieldType.none.equals(from.type())) {
            to.setType(from.type());
        }
        if (!JTDConst.EnumFieldUiType.none.equals(from.uiType())) {
            to.setUiType(from.uiType());
        }
        if (JTDUtil.isNotBlank(from.value())) {
            to.setValue(from.value());
        }
        if (JTDUtil.isNotBlank(from.group())) {
            to.setGroup(from.group());
        }
        if (JTDUtil.isNotBlank(from.entityName())) {
            to.setEntityName(from.entityName());
        }
        if (!Object.class.equals(from.entityClass())) {
            to.setEntityClass(from.entityClass());
        }
        if (JTDUtil.isNotBlank(from.entityClassKey())) {
            to.setEntityClassKey(from.entityClassKey());
        }
        if (JTDUtil.isNotBlank(from.entityClassLabel())) {
            to.setEntityClassLabel(from.entityClassLabel());
        }
        if (JTDUtil.isNotBlank(from.entityClassTargetProp())) {
            to.setEntityClassTargetProp(from.entityClassTargetProp());
        }
        if (JTDUtil.isNotBlank(from.extendProp())) {
            to.setExtendProp(from.extendProp());
        }
        to.setSortable(from.sortable());
        to.setSort(from.sort());
        to.setDisabled(from.disabled());
    }

    public static void copyProperties(JTDTable from, JTDTableTemp to) {
        if (JTDUtil.isNotBlank(from.label())) {
            to.setLabel(from.label());
        }
        if (JTDUtil.isNotBlank(from.description())) {
            to.setDescription(from.description());
        }
        if (from.keyFieldNames().length > 0) {
            to.setKeyFieldNames(from.keyFieldNames());
        }
        if (from.excludeFieldNames().length > 0) {
            to.setExcludeFieldNames(from.excludeFieldNames());
        }
        if (from.sequences().length > 0) {
            to.setSequences(from.sequences());
        }
        if (JTDUtil.isNotBlank(from.tableName())) {
            to.setTableName(from.tableName());
        }
        if (JTDUtil.isNotBlank(from.value())) {
            to.setValue(from.value());
        }
        if (JTDUtil.isNotBlank(from.module())) {
            to.setModule(from.module());
        }
        to.setEntityId(from.entityId());
    }

    public static void initProperties(JTDTableTemp to) {
        JTDTable from = jtdTableExample;
        to.setLabel(from.label());
        to.setDescription(from.description());
        to.setKeyFieldNames(from.keyFieldNames());
        to.setExcludeFieldNames(from.excludeFieldNames());
        to.setSequences(from.sequences());
        to.setTableName(from.tableName());
        to.setModule(from.module());
        to.setEntityId(from.entityId());
        to.setValue(from.value());
    }

    public static void initProperties(JTDFieldTemp to) {
        JTDField from = jtdFieldExample;
        to.setLabel(from.label());
        to.setDescription(from.description());
        to.setAutoIncrement(from.autoIncrement());
        to.setCharset(from.charset());
        to.setDecimalLength(from.decimalLength());
        to.setDefaultValue(from.defaultValue());
        to.setFieldName(from.fieldName());
        to.setJavaTypeEnumClass(from.javaTypeEnumClass());
        to.setJavaTypeEnumClassMultiple(from.javaTypeEnumClassMultiple());
        to.setNotNull(from.notNull());
        to.setMainLength(from.mainLength());
        to.setRenameFrom(from.renameFrom());
        to.setType(from.type());
        to.setUiType(from.uiType());
        to.setValue(from.value());
        to.setSort(from.sort());
        to.setSortable(from.sortable());
        to.setEntityClass(from.entityClass());
        to.setEntityClassKey(from.entityClassKey());
        to.setEntityClassLabel(from.entityClassLabel());
        to.setDisabled(from.disabled());
    }

    /**
     * 关键字大写,方便匹配
     */
    private static String sqlFormat(String sql) {
        return sql
                .replaceAll("create ", "CREATE ")
                .replaceAll("table ", "TABLE ")
                .replaceAll("primary ", "PRIMARY ")
                .replaceAll("key ", "KEY ")
                .replaceAll("unique ", "UNIQUE ")
                .replaceAll("not ", "NOT ")
                .replaceAll("default ", "DEFAULT ")
                .replaceAll(" null", " NULL")
                .replaceAll(" comment", " COMMENT")
                .replaceAll(" auto_increment", " AUTO_INCREMENT")
                .trim();
    }

    public static <T> T defaultNull(T val) {
//        JTDConstInner.EMPTY.equals(val) ||
        if (val == null || JTDConst.EnumFieldType.none.equals(val) || JTDConst.EnumFieldNullType.none.equals(val)) {
            return null;
        }
        return val;
    }

//    private Map<String, Integer> getTableHashFromCacheFile() {
//        String basePath = new ApplicationHome(this.getClass()).getSource().getParentFile().getPath() + "/";
//        File jtdJsonFile = new File(basePath + "JTD.json");
//        String json = jtdJsonFile.exists() ? JTDUtil.readString(jtdJsonFile) : null;
//        return null == json ? Collections.emptyMap() : Arrays.stream(json.replaceAll("\\s+", "\n").split("\n")).collect(Collectors.toMap(s -> s.split(":")[0], s -> Integer.valueOf(s.split(":")[1])));
//    }

    @SneakyThrows
    private static String getRsString1(ResultSet rs) {
        return rs.getString(2);
    }

    @Override
    public void reload() {
        executeSql();
    }
}
