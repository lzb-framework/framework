/*
 * Copyright (c) 2011-2020, baomidou (jobob@qq.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.pro.framework.api.util.inner;

import com.pro.framework.api.util.OtherUtil;
import com.pro.framework.api.util.StrUtils;
import lombok.Data;
import lombok.SneakyThrows;

/**
 * @author Administrator
 */
@SuppressWarnings("unused")
@Data
public class SerializedLambdaData {
    /**
     * 类
     */
    private Class<?> clazz;
    /**
     * 属性名(驼峰)
     */
    private String propName;
    /**
     * 属性了行
     */
    private Class<?> propReturnType;

    @SneakyThrows
    public SerializedLambdaData(SerializedLambda serializedLambda) {
        //实体类
        clazz = OtherUtil.toClassConfident(serializedLambda.getImplClass().replaceAll("/", ".").replaceAll("\\\\", "."));
        //去掉"get",首字母小写
        propName = StrUtils.firstToLowerCase(serializedLambda.getImplMethodName().substring(3));
        propReturnType = OtherUtil.toClassConfident(serializedLambda.getImplMethodSignature().substring(3, serializedLambda.getImplMethodSignature().indexOf(";")).replaceAll("/", ".").replaceAll("\\\\", "."));
    }
}
