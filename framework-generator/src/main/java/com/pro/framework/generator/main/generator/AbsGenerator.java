package com.pro.framework.generator.main.generator;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import com.pro.framework.api.FrameworkConst;
import com.pro.framework.api.util.StrUtils;
import com.pro.framework.javatodb.annotation.JTDTable;
import com.pro.framework.javatodb.config.JTDProperties;
import com.pro.framework.javatodb.constant.JTDConst;
import com.pro.framework.javatodb.model.JTDFieldInfoDb;
import com.pro.framework.javatodb.model.JTDTableInfo;
import com.pro.framework.javatodb.service.JTDServiceImpl;
import com.pro.framework.javatodb.util.JTDAssertUtil;
import com.pro.framework.javatodb.util.JTDUtil;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * velocity 代码生成
 */
public abstract class AbsGenerator {

    // 改成支持windows的
    private static final String templateBasePath = "/Users/Shared/generate/templates";
    public static String DEMO_NAME = "Demo";
    private static VelocityEngine velocityEngine;
    private volatile static Set<String> initedTemplates = new HashSet<>();
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void init() {
        Properties p = new Properties();
//        p.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
//        p.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, "");
        p.setProperty(Velocity.RESOURCE_LOADER, "file");
        p.setProperty(Velocity.RESOURCE_LOADERS, "file");
        p.setProperty("file.resource.loader.path", templateBasePath);
        p.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, templateBasePath);
        p.setProperty(Velocity.ENCODING_DEFAULT, StandardCharsets.UTF_8.name());
        p.setProperty(Velocity.INPUT_ENCODING, StandardCharsets.UTF_8.name());
        p.setProperty("file.resource.loader.unicode", "true");
        velocityEngine = new VelocityEngine(p);
    }

    /**
     * 根据实体类,模板,生成代码
     *
     * @param classes      实体类
     * @param demoFilePath 模板路径
     * @param newFileFun   新文件路径
     */
    public static void generateInSamePath(List<Class<?>> classes, String demoFilePath) {
        generate(classes, demoFilePath, (clazz) -> demoFilePath.replace(DEMO_NAME, clazz.getSimpleName()));
    }

    public static void generate(List<Class<?>> classes, String demoFilePath, Function<Class<?>, String> newFileFun) {
        File templateSourceFile = new File(demoFilePath);
        File templateFileFolder = new File(templateBasePath);
        templateFileFolder.mkdirs();
        File templateFile = new File(templateBasePath + FrameworkConst.Str.file_separator + templateSourceFile.getName() + ".vm");
        // 1.用模板的模板DemoDao.java 去更新模板DemoDao.java.vm
        initTemplateFromDemo(templateSourceFile, templateFile);
        // 2.用新模板DemoDao.java.vm 去生成代码XXXDao.java
        generatorCode(classes, templateFile, newFileFun);
    }

    public static String getModule(Class<?> clazz, String defaultModule) {
        return ObjectUtil.defaultIfEmpty(null == clazz.getAnnotation(JTDTable.class) ? null : clazz.getAnnotation(JTDTable.class).module(), defaultModule);
    }

    /**
     * 为了更好得维护模板
     * <p>
     * 模板 demoDao.dao 代码中尽可能少计算,尽可能简化if分支
     */
    private static void initTemplateFromDemo(File templateSourceFile, File templateFile) {
        String absolutePath = templateSourceFile.getAbsolutePath();
        if (initedTemplates.contains(absolutePath)) {
            return;
        }
        initedTemplates.add(absolutePath);
        String templateTempStr = FileUtil.readUtf8String(templateSourceFile)
                //很多模板卸载注释中,通过GG来区分以下:
                // 常规注释:            原封不动会生成到结果文件中
                // 模板代码语句的注释:    应用成模板语法后,不会出现在结果文件中
                .replaceAll("<!--GG", "     ")
                .replaceAll("GG-->", "")
                .replaceAll("/\\*GG", "     ")
                .replaceAll("GG\\*/", "")

                // demo 替换为 模板中的类名
                // 各个位置的8位字符名称的属性 替换为 模板中的属性名
                .replaceAll("EnumDictType", "\\${field.javaTypeEnumClass}")
                .replaceAll("scope\\.row\\.[a-zA-z0-9]{8}", "scope\\.row\\.\\${field.columnName}")
                .replaceAll("demoFormObj\\.[a-zA-z0-9]{8}", "\\${entityName}FormObj\\.\\${field.columnName}")
                .replaceAll("demoListQuery\\.[a-zA-z0-9]{8}", "\\${entityName}ListQuery\\.\\${field.columnName}")
                .replaceAll(StrUtils.firstToLowerCase(DEMO_NAME), "\\${entityName}")
                .replaceAll(DEMO_NAME, "\\${className}")

                //替换一些系统变量
                .replaceAll("2022-01-01 00:00:00", LocalDateTime.of(LocalDate.now(), LocalTime.MIN).format(DATE_TIME_FORMAT));
        FileUtil.writeUtf8String(templateTempStr, templateFile);
    }

    private static void generatorCode(List<Class<?>> classes, File templateFile, Function<Class<?>, String> newFilePathFun) {
        for (Class<?> aClass : classes) {
            // 从类中解读 类(及其属性)信息
            JTDTableInfo jtdTableInfo = JTDServiceImpl.buildTableFullInfoFromClass(aClass, JTDProperties.FIELD_PATTERN_NOT_NULL_DEFAULT_VALUE_MAP_DEFAULT);
            JTDAssertUtil.notNull(jtdTableInfo, aClass + "不存在@JTDTable配置");
            // 类(及其属性),填充模板,生成最终文件
            writer(buildDatas(jtdTableInfo),
                    templateFile.getName(),
                    newFilePathFun.apply(aClass)
            );
        }
    }

    /**
     * 转换和过滤
     *
     * @param jtdTableInfo 可以应用进数据库层级的 类(包含属性) 信息
     * @return 模板语法需要的 类(包含属性) 信息
     */
    private static Map<String, Object> buildDatas(JTDTableInfo jtdTableInfo) {
        //已经存在的xxxCode字段
        Map<String, Object> table = new HashMap<>(8);
        //类(表)信息
        table.put("label", jtdTableInfo.getLabel());
        table.put("keyFields", String.join(",", jtdTableInfo.getKeyFieldNames()));
        table.put("entityName", JTDUtil.underlineToCamel(jtdTableInfo.getTableName()));
        table.put("className", JTDUtil.firstToUpperCase(JTDUtil.underlineToCamel(jtdTableInfo.getTableName())));
        table.put("entityId", 0 == jtdTableInfo.getEntityId() ? 100 : jtdTableInfo.getEntityId());
        table.put("module", jtdTableInfo.getModule());

        Set<String> relationCodeHeads = new HashSet<>();

        List<JSONObject> fields = jtdTableInfo.getFields().stream()
                .filter(field -> {
                    String fieldName = field.getFieldName();
                    //noinspection RedundantIfStatement
                    if ("update_time".equals(fieldName) ||
                            "deleted".equals(fieldName) ||
                            "isDemo".equals(fieldName) ||
                            "id".equals(fieldName) ||
                            fieldName.matches("^.+_id$")
                    ) {
                        //后端字段,不必在ui的表单中体现
                        return false;
                    }
                    return true;
                })

                //属性(字段)信息
                .map(f -> setField(relationCodeHeads, f))

                .sorted(sortField()).collect(Collectors.toList());

        // 存在 dictCode 就不必显示 dictName 了
        fields = removeNameFields(relationCodeHeads, fields);
        table.put("fields", fields);
        return table;
    }


    private static JSONObject setField(Set<String> relationCodeHeads, JTDFieldInfoDb f) {
        String columnName = JTDUtil.underlineToCamel(f.getFieldName());


        JSONObject jsonObject = new JSONObject();
        //属性(字段)信息
        String label = f.getLabel();
        jsonObject.set("columnName", columnName);
        jsonObject.set("type", f.getType().getValue());
        jsonObject.set("label", f.getLabel());
        jsonObject.set("notNull", JTDConst.EnumFieldNullType.not_null.equals(f.getNotNull()));
        jsonObject.set("defaultValue", getDefaultValue(f.getDefaultValue(), f.getType()));
        if (f.getJavaTypeEnumClass() != null) {
            jsonObject.set("javaTypeEnumClass", f.getJavaTypeEnumClass().getSimpleName());
        }
        if (f.getJavaTypeEnumClassMultiple() != null) {
            jsonObject.set("javaTypeEnumClassMultiple", f.getJavaTypeEnumClassMultiple());
        }

        JTDConst.EnumFieldUiType uiType = getUiType(f, columnName);
        jsonObject.set("uiType", uiType.name());
        jsonObject.set("tableColumnWidth", getTableColumnWidth(uiType, f.getType()));


        if (JTDConst.EnumFieldUiType.relationCode.equals(uiType)) {
            //去掉结尾的'Code'和'编号'
            jsonObject.set("label", label.replace("编号", ""));
            relationCodeHeads.add(columnName.substring(0, columnName.length() - 4));
        }
        return jsonObject;
    }

    private static String getTableColumnWidth(JTDConst.EnumFieldUiType uiType, JTDConst.EnumFieldType type) {
        switch (uiType) {
            case base:
                switch (type) {
                    case tinyint:
                        return "62";
                    case datetime:
                        return "150";
                }
                return "100";
            case relationCode:
                return "120";
            case none:
                return "null";//null表示不指定
            case text:
                return "null";
            case number:
                return "80";
            case image:
                return "120";
            case richText:
                return "null";
            case file:
                return "null";
            case date:
                return "100";
            case time:
                return "100";
            case datetime:
                return "150";
            case bool:
                return "62";
        }
        return "null";
    }

    private static JTDConst.EnumFieldUiType getUiType(JTDFieldInfoDb f, String columnName) {
        //基础属性不可编辑,在 新增/修改表单中 disable
        boolean isBaseField = "createTime".equals(columnName) ||
                // "code".equals(columnName) ||
                "updateTime".equals(columnName) ||
                "isDemo".equals(columnName) ||
                "deleted".equals(columnName) ||
                "id".equals(columnName) ||
                columnName.matches("^.+Id$");
        if (isBaseField) {
            return JTDConst.EnumFieldUiType.base;
        }
        boolean isRelationCode = !columnName.matches("^.+CommonCode$") && (columnName.matches("^.+Code$") || columnName.matches("^.+Id$"));
        if (isRelationCode) {
            return JTDConst.EnumFieldUiType.relationCode;
        }
        JTDConst.EnumFieldUiType fromType = null;
        switch (f.getType()) {
            case datetime:
                fromType = JTDConst.EnumFieldUiType.datetime;
                break;
            case time:
                fromType = JTDConst.EnumFieldUiType.time;
                break;
            case date:
                fromType = JTDConst.EnumFieldUiType.date;
                break;
        }
        if (f.getJavaTypeEnumClass() != null) {
            fromType = JTDConst.EnumFieldUiType.select;
        }
        return JTDUtil.or(f.getUiType(), fromType, JTDConst.EnumFieldUiType.none);
    }

    private static String getDefaultValue(String defaultValue, JTDConst.EnumFieldType type) {
        if (StrUtils.isBlank(defaultValue)) {
            return "''";
        }
        if (defaultValue.startsWith("DEFAULT ")) {
            defaultValue = defaultValue.substring(8);
        }
        // if (defaultValue.startsWith("'")) {
        //     return defaultValue.substring(1, defaultValue.length() - 1);
        // }
        if ("CURRENT_TIMESTAMP".equals(defaultValue)) {
            return "'" + DateUtil.now() + "'";
        }
        switch (type) {
            case tinyint:
                if ("0".equals(defaultValue) || "'0'".equals(defaultValue)) {
                    defaultValue = "false";
                }
                if ("1".equals(defaultValue) || "'1'".equals(defaultValue)) {
                    defaultValue = "true";
                }
                break;
        }
        return "NULL".equals(defaultValue) ? "null" : defaultValue;
    }


    private static List<JSONObject> removeNameFields(Set<String> relationCodeHeads, List<JSONObject> fields) {
        fields = fields.stream().filter(f -> {
            String columnName = f.getStr("columnName");
            if (columnName.endsWith("Name")) {
                String columnNameHead = columnName.substring(0, columnName.length() - 4);
                //noinspection RedundantIfStatement
                if (relationCodeHeads.contains(columnNameHead)) {
                    // 存在 dictCode 就不必显示 dictName 了
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());
        return fields;
    }

    /**
     * id,code字段排前面,createTime,updateTime字段排后面
     */
    private static Comparator<JSONObject> sortField() {
        return (f1, f2) -> {
            if ("id".equals(f1.getStr("columnName")) || "code".equals(f1.getStr("columnName"))) {
                return -1;
            }
            if ("id".equals(f2.getStr("columnName")) || "code".equals(f2.getStr("columnName"))) {
                return 1;
            }
            if ("createTime".equals(f1.getStr("columnName")) || "updateTime".equals(f1.getStr("columnName")) || "deleted".equals(f1.getStr("columnName"))) {
                return 1;
            }
            if ("createTime".equals(f2.getStr("columnName")) || "updateTime".equals(f2.getStr("columnName")) || "deleted".equals(f2.getStr("columnName"))) {
                return -1;
            }
            return 1;
        };
    }

    private static void writer(Map<String, Object> objectMap, String templatePath, String outputFile) {
        if (StrUtils.isBlank(templatePath)) {
            return;
        }
        File outFile = new File(outputFile);
        if (!outFile.getParentFile().exists()) {
            JTDAssertUtil.true_(outFile.getParentFile().mkdirs(), "创建文件目录失败:" + outputFile);
        }
        Template template = velocityEngine.getTemplate(templatePath, StandardCharsets.UTF_8.name());
//        Template template = velocityEngine.getTemplate(templateBasePath + FrameworkConst.Str.file_separator + templatePath, StandardCharsets.UTF_8.name());
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(outputFile);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8));
            template.merge(new VelocityContext(objectMap), writer);
            writer.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("模板:" + templatePath + ";  文件:" + outputFile);
    }
}
