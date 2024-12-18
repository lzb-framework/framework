package com.pro.framework.generator.utils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.HashMap;

public class StringFormater {
    private static Map<String, Field> fieldCache = new HashMap<>();

//    public static void main(String[] args) throws Exception {
//        User user = new User("john_doe", new Profile("John", "Doe"));
//        String template = "Username: {username}, Full Name: {user.firstName} {user.lastName}";
//
//        String result = formatString(template, user);
//        System.out.println(result); // 输出: Username: john_doe, Full Name: John Doe
//    }

    // 格式化字符串，根据占位符解析字段
    public static String format(String template, Object object) throws Exception {
        StringBuilder result = new StringBuilder(template);

        // 使用正则表达式查找 {field} 样式的占位符
        while (result.indexOf("{") != -1) {
            int start = result.indexOf("{");
            int end = result.indexOf("}", start);
            String placeholder = result.substring(start + 1, end);

            // 获取字段名和嵌套对象的处理
            String[] parts = placeholder.split("\\.");
            Object value = getFieldValue(object, parts, 0);

            // 替换占位符
            result.replace(start, end + 1, value != null ? value.toString() : "");
        }

        return result.toString();
    }

    // 获取嵌套对象的字段值
    public static Object getFieldValue(Object object, String[] parts, int index) throws IllegalAccessException {
        if (object == null || index >= parts.length) {
            return null;
        }

        String fieldName = parts[index];
        Field field = fieldCache.get(fieldName);

        if (field == null) {
            try {
                field = object.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                fieldCache.put(fieldName, field);
            } catch (NoSuchFieldException e) {
                throw new IllegalAccessException("Field not found: " + fieldName);
            }
        }

        Object value = field.get(object);

        if (index + 1 < parts.length && value != null) {
            return getFieldValue(value, parts, index + 1);
        }

        return value;
    }
}
