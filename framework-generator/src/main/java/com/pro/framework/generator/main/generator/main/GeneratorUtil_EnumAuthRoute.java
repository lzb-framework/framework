package com.pro.framework.generator.main.generator.main;

import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pro.framework.api.util.AssertUtil;
import com.pro.framework.generator.main.generator.AbsGenerator;
import com.pro.framework.api.model.GeneratorDevConfig;
import com.pro.framework.generator.utils.StringFormater;
import com.pro.framework.javatodb.config.JTDProperties;
import com.pro.framework.javatodb.model.JTDTableInfo;
import com.pro.framework.javatodb.service.JTDServiceImpl;
import com.pro.framework.javatodb.util.JTDAssertUtil;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * velocity 代码生成
 */
public class GeneratorUtil_EnumAuthRoute extends AbsGenerator {

    public static final String DEMO_LABEL = ", \"项目产品\",";
    public static final String DEMO_CODE = "demo";
    private static final String DEMO_REGEX = "^\\s*(//\\s*|)demo";

    @SneakyThrows
    public static void generate(List<Class<?>> classes) {
        init();

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        // 读取 YAL
        GeneratorDevConfig generatorConfig = mapper.readValue(new ClassPathResource("system-dev.yml").getFile(), GeneratorDevConfig.class);
        generatorConfig.setPlatformNameUpperFirst(StrUtil.upperFirst(generatorConfig.getPlatformNameUpperFirst()));

        // 读取枚举内容
        String enumPath = StringFormater.format("{workspace}/{platformName}/platform/{platformName}-api/src/main/java/com/pro/{platformName}/api/enums/EnumAuthRoute{platformName}.java", generatorConfig);

        // 按行读取 enumAuthRouteFile 文件全内容到 fileLines
        List<String> fileLines = Files.readAllLines(Paths.get(enumPath));
        // 另外读出以demo开头的每一行 (trim()以后)
        Pattern compile = Pattern.compile(DEMO_REGEX);
        List<String> fileLinesDemo = fileLines.stream().filter(line -> compile.matcher(line).find()).map(s -> s.trim().startsWith("//") ? s.replaceFirst("//", "") : s).toList();
        AssertUtil.notEmpty(fileLinesDemo, enumPath + " no contains demo lines");

        Matcher matcher = Pattern.compile("\\d+").matcher(fileLinesDemo.get(0));
        String routeId = matcher.find() ? matcher.group() : "100200";
        String routeIdHead = routeId.substring(0, routeId.length() - 2);

        for (Class<?> aClass : classes) {
            // 从类中解读 类(及其属性)信息
            JTDTableInfo jtdTableInfo = JTDServiceImpl.buildTableFullInfoFromClass(aClass, JTDProperties.FIELD_PATTERN_NOT_NULL_DEFAULT_VALUE_MAP_DEFAULT);
            JTDAssertUtil.notNull(jtdTableInfo, aClass + "不存在@JTDTable配置");

            String entityName = jtdTableInfo.getEntityName();
            Integer entityId = jtdTableInfo.getEntityId();
            String label = jtdTableInfo.getLabel();
            // 2.删除enumAuthRouteFile中已存在的行
            Set<String> oldLines = fileLines.stream().filter(line -> line.trim().startsWith(entityName + "_") || line.trim().startsWith(entityName + "(")).collect(Collectors.toSet());
            oldLines.forEach(fileLines::remove);

            // 3.根据demo开的几行(内容),复制新的行 (添加到第一个;所在行的前一行)
            List<String> fileLinesNew = fileLinesDemo.stream().map(s ->
                    s
                            .replaceAll(DEMO_CODE, entityName)
                            .replaceAll(DEMO_LABEL, ", \"" + label + "\",")
                            .replaceAll(", " + routeIdHead, ", " + entityId)
            ).toList();

            String enumLastLine = fileLines.stream().filter(line -> !line.startsWith("package") && !line.startsWith("import") && line.contains(";")).findFirst().orElse("");
            int lastIndex = fileLines.indexOf(enumLastLine) - 1;
            for (String line : fileLinesNew) {
                fileLines.add(lastIndex, line);
                lastIndex++;
            }
        }

        Files.write(Paths.get(enumPath), fileLines);  // 默认覆盖原文件
    }

//    public static void main(String[] args) {
//        System.out.println("demo(ADMIN, catalog_snowball, MENU, \"项目产品\", \"/snowball/demo\", null, null, null, null, null, null, 100200, null),".replaceAll());
//    }
}
