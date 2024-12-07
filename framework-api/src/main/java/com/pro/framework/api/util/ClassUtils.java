package com.pro.framework.api.util;

import cn.hutool.core.util.ClassUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class ClassUtils extends ClassUtil {
    public static final String _SystemPackage = "com.pro";

    public static <T> Set<Class<? extends T>> getSubTypesOf(Class<T> clazz) {
        //noinspection unchecked
        return (Set) scanPackageBySuper(_SystemPackage, clazz);
    }

    public static Class<?> getGenericClass(Class<?> clazz, Integer i) {
        return (Class<?>) ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[i];
    }

    public static List<Field> getAllFields(Class<?> clazz) {
        return getClassMetas(clazz, c -> {
            return Arrays.stream(c.getDeclaredFields()).filter(f -> !Modifier.isStatic(f.getModifiers()) && !Modifier.isFinal(f.getModifiers()) && !Modifier.isTransient(f.getModifiers())).collect(Collectors.toList());
        });
    }

    private static <T> List<T> getClassMetas(Class<?> clazz, Function<Class<?>, List<T>> getOneClassMetaFun) {
        List<T> list = new ArrayList<>(64);
        Class<?> currClazz = clazz;
        while (!currClazz.equals(Object.class)) {
            list.addAll(0, getOneClassMetaFun.apply(currClazz));
            currClazz = currClazz.getSuperclass();
        }
        return list;
    }

    public static <T> boolean checkImplement(Class<?> subClass, Class<T> parentClass) {
        if (subClass == parentClass) {
            return true;
        }
        // 遍历接口数组，检查是否包含特定的接口
        for (Class<?> iface : subClass.getInterfaces()) {
            if (iface == parentClass) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取类名的方法 (多层级内部类会获取到 OuterClass.MiddleClass.InnerClass...)
     */
    public static String getClassName(Class<?> clazz) {
        // 获取外部类名
        String outerClassName = clazz.getDeclaringClass() != null ? getClassName(clazz.getDeclaringClass()) : "";

        // 获取内部类名
        String innerClassName = clazz.getSimpleName();

        // 拼接外部类和内部类名
        return outerClassName.isEmpty() ? innerClassName : outerClassName + "_" + innerClassName;
    }

    public static boolean isBasicDataType(Class<?> clazz) {
        if (null == clazz) {
            return false;
        } else {
            return clazz.isEnum() || clazz.isPrimitive() || isPrimitiveWrapper(clazz)
                    || String.class.equals(clazz)
                    || Timestamp.class.equals(clazz)
                    || Blob.class.equals(clazz)
                    || Clob.class.equals(clazz)
                    || BigDecimal.class.equals(clazz)
                    || Date.class.equals(clazz)
                    || LocalDateTime.class.equals(clazz)
                    || LocalDate.class.equals(clazz)
                    || LocalTime.class.equals(clazz)
                    ;
        }
    }

    public static List<Method> getAllMethods(Class<?> clazz) {
        return getClassMetas(clazz, s -> Arrays.stream(s.getDeclaredMethods()).collect(Collectors.toList()));
    }


}
