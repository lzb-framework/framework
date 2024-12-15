package com.pro.framework.cache.template;

import lombok.SneakyThrows;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MyBatisTemplateParserTest2 {

    @SneakyThrows
    public static void main(String[] args) {
        // 模拟输入的参数
        Map<String, Object> params = new HashMap<>();
        params.put("platform", "lottery");
        params.put("modules", Arrays.asList("user", "admin", "agent", "libs"));
        Map<String, Object> secrets = new HashMap<>();
        secrets.put("SERVER_PASSWORD", "mypassword123");
        secrets.put("SERVER_IP", "192.168.1.1");
        params.put("secrets", secrets);

// 模拟从模板中解析
        String textTemplate = " <foreach collection=\"modules\" item=\"module\">\n" +
                "        <if test=\"module == 'libs'\">\n" +
                "            {\n" +
                "                \"type\":\"cmd\",\n" +
                "                \"params\":{\n" +
                "                    \"cmd\":\"sshpass -p \\\\\\\"#{ secrets.SERVER_PASSWORD }\\\\\\\" scp -o StrictHostKeyChecking=no -q platform/#{platform}-admin/target/libs.tar.gz root@#{ secrets.SERVER_IP }:/project/#{platform}/ \"\n" +
                "                }\n" +
                "            },\n" +
                "        </if>\n" +
                "</foreach>";

// 替换模板中的变量并解析
        String finalText = MyBatisTemplateParser.parseTextTemplate(textTemplate, params);
        System.out.println(finalText);


    }
}
