package com.pro.framework.core;

import com.pro.framework.EnumProperties;
import com.pro.framework.api.clazz.IConst;
import com.pro.framework.api.util.ClassUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 加载所有枚举类
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class EnumConstant {

    public static Map<String, Class<? extends Enum>> simpleNameClassMap = Collections.emptyMap();
    public static Map<String, Class<? extends Enum>> simpleNameClassMapNoReplace = Collections.emptyMap();
//    public static Map<String, Map<String, String>> dictMapAll = Collections.emptyMap();

    public static void load(EnumProperties enumProperties) {
        // 常规外层枚举类
        Map<String, Class<? extends Enum>> map = ClassUtils.getSubTypesOf(Enum.class).stream().collect(Collectors.toMap(Class::getSimpleName, c -> c));

        // 内部类枚举类
        Map<String, Class<? extends Enum>> innerMap = new HashMap<>();
        ClassUtils.getSubTypesOf(IConst.class)
                .forEach(parentClass -> Arrays.stream(parentClass.getDeclaredClasses())
                        .filter(Class::isEnum)
                        .forEach(innerEnumClass -> innerMap.put(parentClass.getSimpleName() + "_" + innerEnumClass.getSimpleName(), (Class<? extends Enum>) innerEnumClass)));

        simpleNameClassMap = new HashMap<>(2048);
        simpleNameClassMap.putAll(innerMap);
        simpleNameClassMap.putAll(map);
        simpleNameClassMapNoReplace = new HashMap<>(2048);
        simpleNameClassMapNoReplace.putAll(simpleNameClassMap);
        // 覆盖定制枚举
        simpleNameClassMap.putAll(enumProperties.getEnumClassReplaceMap());

//        dictMapAll = simpleNameClassMap.keySet().stream().collect(Collectors.toMap(o -> o,
//                className -> EnumUtil.getNameLabelMap(simpleNameClassMap.get(className))));
    }
}
