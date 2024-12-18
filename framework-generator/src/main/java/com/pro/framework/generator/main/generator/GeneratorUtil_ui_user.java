package com.pro.framework.generator.main.generator;

import cn.hutool.core.util.StrUtil;
import com.pro.framework.generator.utils.AbsGenerator;

import java.util.Arrays;
import java.util.List;

/**
 * velocity 代码生成
 */
public class GeneratorUtil_ui_user extends AbsGenerator {

    public static void main(String[] args) {
        init();

        List<Class<?>> classes = Arrays.asList(
                // Goods.class,
                // GoodsCate.class,
                // GoodsSpec.class,
                // LoanProductStateInfo.class,
                // LoanOrder.class,
                // LoanRepayOrder.class,
        );

        //1.用 demoUserFormVue.vue 生成 demoUserFormVue.vue.vm
        //2.用 demoUserFormVue.vue.vm 生成 xxx.vue 用户端页面代码
        generate(classes,
                "/Users/fa/parent_projects/ai/parent/framework/framework-generator/src/main/resources/templates/demoUserFormVue.vue",
                (clazz) -> "/Users/fa/parent_projects/ai/gym-ui-user/src/views/" + getModule(clazz, "sys") + "/" + StrUtil.lowerFirst(clazz.getSimpleName()) + "UserForm.vue"
        );
        generate(classes,
                "/Users/fa/parent_projects/ai/parent/framework/framework-generator/src/main/resources/templates/demoUserListVue.vue",
                (clazz) -> "/Users/fa/parent_projects/ai/gym-ui-user/src/views/" + getModule(clazz, "sys") + "/" + StrUtil.lowerFirst(clazz.getSimpleName()) + "UserList.vue"
        );
    }
}
