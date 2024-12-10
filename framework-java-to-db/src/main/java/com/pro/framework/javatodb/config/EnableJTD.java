/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pro.framework.javatodb.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启表结构,根据实体类(新增/修改)
 * todo 添加校验 default 不能为null 开关(大项目应该必要的,没走索引)
 * todo 支持,表名字段,驼峰型
 * todo 项目太多魔法值字符常量 后面统一抽出去
 * @author administrator
 * @date 2022-01-20
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(JTDConfig.class)
@Documented
public @interface EnableJTD {
}
