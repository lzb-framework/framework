package com.pro.framework.generator.main.generator;

import cn.hutool.core.util.StrUtil;
import com.pro.framework.generator.utils.AbsGenerator;

import java.util.Arrays;
import java.util.List;

/**
 * velocity 代码生成
 */
public class GeneratorUtil_ui_admin extends AbsGenerator {

    public static void main(String[] args) {
        init();

        List<Class<?>> classes = Arrays.asList(
        );

        //1.用 demoAdminVue.vue 生成 demoAdminVue.vue.vm
        //2.用 demoAdminVue.vue.vm 生成 xxxDao.java 管理端页面代码

        generate(classes,
                "/Users/fa/parent_projects/ai/parent/framework/framework-generator/src/main/resources/templates/demoAdminVue.vue",
                (clazz) -> "/Users/fa/parent_projects/ai/gym-ui-admin/src/views/" + getModule(clazz, "sys") + "/" + StrUtil.lowerFirst(clazz.getSimpleName()) + ".vue"
        );
    }
}
