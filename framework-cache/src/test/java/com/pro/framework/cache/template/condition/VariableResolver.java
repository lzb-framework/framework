package com.pro.framework.cache.template.condition;

import java.util.Map;

public class VariableResolver {

    public static Object resolve(String expression, Map<String, Object> params) {
        String[] parts = expression.split("\\.");  // 以 . 分隔路径

        Object value = params.get(parts[0]);  // 获取第一个字段
        for (int i = 1; i < parts.length; i++) {
            if (value instanceof Map) {
                value = ((Map<?, ?>) value).get(parts[i]);  // 如果是 Map，则根据键查找值
            } else if (value != null) {
                // 对象字段查找，反射方式获取字段值
                try {
                    value = value.getClass().getField(parts[i]).get(value);  // 通过反射获取字段
                } catch (Exception e) {
                    return null;  // 如果字段不存在，返回 null
                }
            }
            if (value == null) {
                return null;
            }
        }
        return value;
    }
}
