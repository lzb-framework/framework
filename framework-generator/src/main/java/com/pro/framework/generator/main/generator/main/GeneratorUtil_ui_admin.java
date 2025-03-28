package com.pro.framework.generator.main.generator.main;

import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pro.framework.api.FrameworkConst;
import com.pro.framework.api.model.GeneratorDevConfig;
import com.pro.framework.generator.main.generator.AbsGenerator;
import com.pro.framework.generator.utils.StringFormater;
import lombok.SneakyThrows;

import java.io.File;
import java.util.List;

/**
 * velocity 代码生成
 */
public class GeneratorUtil_ui_admin extends AbsGenerator {

    @SneakyThrows
    public static void generate(List<Class<?>> classes) {
        init();

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        // 读取 YAL
        GeneratorDevConfig generatorConfig = mapper.readValue(new ClassPathResource("system-dev.yml").getFile(), GeneratorDevConfig.class);
        init();

        //1.用 demoAdminVue.vue 生成 demoAdminVue.vue.vm
        //2.用 demoAdminVue.vue.vm 生成 xxxDao.java 管理端页面代码

        generate(classes,
                StringFormater.format("{workspace}/{platformName}/framework/framework-generator/src/main/resources/templates/demoAdminVue.vue", generatorConfig),
                (clazz) ->
                        StringFormater.format("{workspace}/{platformName}/ui-admin/src/views/" + getModule(clazz, "sys") + FrameworkConst.Str.file_separator + StrUtil.lowerFirst(clazz.getSimpleName()) + ".vue", generatorConfig)
        );
    }
}
