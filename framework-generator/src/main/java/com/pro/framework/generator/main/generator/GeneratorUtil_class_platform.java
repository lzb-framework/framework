package com.pro.framework.generator.main.generator;


import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pro.framework.generator.utils.AbsGenerator;
import com.pro.framework.generator.utils.StringFormater;
import lombok.SneakyThrows;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * velocity 代码生成
 */
public class GeneratorUtil_class_platform extends AbsGenerator {

    @SneakyThrows
    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        // 读取 YAML 文件并转换为 AppConfig 对象
        GeneratorDevConfig appConfig = mapper.readValue(new File("config.yml"), GeneratorDevConfig.class);
        init();

        List<Class<?>> classes = Arrays.asList(
        );
//        StrUtil.replace()
        //dao /Users/zubin/IdeaProjects/snowball/platform/snowball-common/src/main/java/com/pro/snowball/common/dao/DemoDao.java
        generateInSamePath(classes, StringFormater.format("{workspace}/{platformName}/platform/{platformName}-common/src/main/java/com/pro/{platformName}/common/dao/DemoDao.java", appConfig));
        //service
        generateInSamePath(classes, StringFormater.format("{workspace}/{platformName}/platform/{platformName}-common/src/main/java/com/pro/{platformName}/common/service/DemoService.java", appConfig));
        //admin controller
        generateInSamePath(classes, "/Users/fa/parent_projects/ai/platform/gym-admin/src/main/java/com/pro/ai/admin/controller/AdminDemoController.java");

//        //user controller
//        generateInSamePath(classes, "/Users/fa/parent_projects/ai/platform/gym-user/src/main/java/com/pro/ai/user/controller/UserDemoController.java");

//        //user common controller
//        generateInSamePath(classes, "/Users/fa/parent_projects/ai/platform/gym-user/src/main/java/com/pro/ai/user/controller/DemoController.java");
    }
}
