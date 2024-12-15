package com.pro.framework.cache.template;

import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MyBatisTemplateParserTest {

    @SneakyThrows
    public static void main(String[] args) {
        // 模拟从 XML 中提取 SQL 模板
        String textTemplate = Files.readString(Path.of("/Users/zubin/IdeaProjects/lottery-github/framework/framework-cache/src/test/java/com/pro/framework/cache/template/ori.xml"));

        // 模拟输入的参数
        Map<String, Object> params = new HashMap<>();
        params.put("id", 123);
        params.put("name", "John");
        params.put("ids", Arrays.asList(1, 2, 3));

        // 解析 SQL 模板
        String finalText = MyBatisTemplateParser.parseTextTemplate(textTemplate, params);

        System.out.println("Final: " + finalText);
    }
}
