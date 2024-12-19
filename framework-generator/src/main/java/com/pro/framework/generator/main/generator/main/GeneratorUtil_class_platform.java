package com.pro.framework.generator.main.generator.main;


import cn.hutool.core.io.resource.ClassPathResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pro.framework.api.model.GeneratorDevConfig;
import com.pro.framework.generator.main.generator.AbsGenerator;
import com.pro.framework.generator.utils.StringFormater;
import lombok.SneakyThrows;

import java.util.List;

/**
 * velocity 代码生成
 */
public class GeneratorUtil_class_platform extends AbsGenerator {

    @SneakyThrows
    public static void generate(List<Class<?>> classes) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        // 读取 YAL
        GeneratorDevConfig generatorConfig = mapper.readValue(new ClassPathResource("system-dev.yml").getFile(), GeneratorDevConfig.class);
        init();

        // 复制生成 dao
        generateInSamePath(classes, StringFormater.format("{workspace}/{platformName}/platform/{platformName}-common/src/main/java/com/pro/{platformName}/common/dao/DemoDao.java", generatorConfig));
        // 复制生成 service
        generateInSamePath(classes, StringFormater.format("{workspace}/{platformName}/platform/{platformName}-common/src/main/java/com/pro/{platformName}/common/service/DemoService.java", generatorConfig));
        // 复制生成 admin controller
//        generateInSamePath(classes, StringFormater.format("{workspace}/{platformName}/platform/{platformName}-admin/src/main/java/com/pro/{platformName}/admin/controller/AdminDemoController.java", generatorConfig));

//        //复制生成 user controller
//        generateInSamePath(classes, "/Users/zubin/IdeaProjects/{platformName}/platform/{platformName}-user/src/main/java/com/pro/{platformName}/user/controller/UserDemoController.java");
    }
}
