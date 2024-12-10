package com.pro.framework.mtq.service.multiwrapper.util;

import com.pro.framework.mtq.service.multiwrapper.annotations.MultiTableField;
import com.pro.framework.mtq.service.multiwrapper.annotations.MultiTableId;
import com.pro.framework.mtq.service.multiwrapper.constant.MultiConstant;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 表对应实体,表和表关系的缓存
 * String若为属性名,类名则为驼峰小写开头  只有在输出sql进行驼峰转下划线处理
 *
 * @author Administrator
 */
@SuppressWarnings({"unused", "AlibabaLowerCamelCaseVariableNaming"})
public class MultiRelationCaches {
    public static void clear() {
        TABLE_CLASS_ID_FIELD_MAP.clear();
        TABLE_CLASS_LOGIC_FIELD_MAP.clear();
        TABLE_CLASS_FIELD_SET_METHOD_MAP.clear();
        TABLE_CLASS_FIELD_SET_METHOD_MAP_FULL.clear();
        TABLE_WITH_TABLE_GET_SET_METHOD_MAP.clear();
    }

    /**
     * 表的主键(对应的Field)
     */
    private static final Map<Class<?>, Field> TABLE_CLASS_ID_FIELD_MAP = new WeakHashMap<>(4096);
    /**
     * 表的主键(对应的Field)
     */
    private static final Map<Class<?>, String> TABLE_CLASS_LOGIC_FIELD_MAP = new WeakHashMap<>(4096);
    /**
     * 将resultSet数据set到实体里,需要判断数据类型
     */
    private static final Map<Class<?>, Map<String, MultiTuple3<Field, Method, Method>>> TABLE_CLASS_FIELD_SET_METHOD_MAP = new WeakHashMap<>(4096);
    private static final Map<Class<?>, Map<String, MultiTuple3<Field, Method, Method>>> TABLE_CLASS_FIELD_SET_METHOD_MAP_FULL = new WeakHashMap<>(4096);
    /**
     * 主表-子表数据,set到主表对象上去
     */
    private static final Map<Class<?>, Map<String, MultiTuple2<Method, Method>>> TABLE_WITH_TABLE_GET_SET_METHOD_MAP = new WeakHashMap<>(4096);

    /**
     * 表中每个属性和set方法
     */
    public static Map<String, MultiTuple3<Field, Method, Method>> getClassInfos(Class<?> tableClass) {
        return getFieldSetMethod(tableClass, TABLE_CLASS_FIELD_SET_METHOD_MAP, false);
    }

    public static Map<String, MultiTuple3<Field, Method, Method>> getClassInfosFull(Class<?> tableClass) {
        return getFieldSetMethod(tableClass, TABLE_CLASS_FIELD_SET_METHOD_MAP_FULL, true);
    }

    /**
     * 表中所有属性名称
     */
    public static List<String> getFieldNamesByClass(Class<?> tableClass) {
        return getClassInfos(tableClass).values().stream().map(tuple2 -> tuple2.getT1().getName()).collect(Collectors.toList());
    }

    /**
     * 获取主表下某个关联表对象(列表)的get/set方法
     *
     * @param tableClass   主表类
     * @param relationCode 关联表对应relationCode
     */
    public static MultiTuple2<Method, Method> getTableWithTable_getSetMethod(Class<?> tableClass, String relationCode) {
        return TABLE_WITH_TABLE_GET_SET_METHOD_MAP
                .computeIfAbsent(tableClass, (c) -> new WeakHashMap<>())
                .computeIfAbsent(relationCode, (code) -> {
                    String fieldNameUpperFirst = MultiUtil.firstToUpperCase(code);
                    //初始化map
                    Method getMethod = MultiUtil.getAllMethods(tableClass).stream()
                            .filter(m -> unStaticUnFinal(m.getModifiers(), false))
                            .filter(m -> m.getName().equals("get" + fieldNameUpperFirst))
                            .filter(m -> !MultiUtil.isBasicDataType(m.getReturnType()))
                            .findAny().orElse(null);

                    Method setMethod = MultiUtil.getAllMethods(tableClass).stream()
                            .filter(m -> unStaticUnFinal(m.getModifiers(), false))
                            .filter(m -> m.getName().equals("set" + fieldNameUpperFirst))
                            .filter(m -> m.getParameterTypes().length == 1 && !MultiUtil.isBasicDataType(m.getParameterTypes()[0]))
                            .findAny().orElse(null);

                    if (setMethod == null && getMethod == null) {
                        return null;
                    }
                    MultiUtil.assertNoNull(setMethod, "找不到{0}对应的set{1}()", tableClass, fieldNameUpperFirst);
                    MultiUtil.assertNoNull(getMethod, "找不到{0}对应的get{1}()", tableClass, fieldNameUpperFirst);

                    return new MultiTuple2<>(getMethod, setMethod);
                });
    }

    /**
     * 获取表的id属性
     *
     * @param tableClass 表对应类
     */
    public static Field getTableIdField(Class<?> tableClass) {


        return TABLE_CLASS_ID_FIELD_MAP.computeIfAbsent(tableClass, (clazz) -> {
            List<Field> allFields = MultiUtil.getAllFields(clazz);
            Field idField = allFields.stream().filter(f -> null != f.getAnnotation(MultiTableId.class)).findAny().orElse(null);
            if (idField == null) {
                idField = allFields.stream().filter(f -> MultiConstant.Strings.ID_FIELD_NAME_DEFAULT.equals(f.getName())).findAny().orElse(null);
            }
            if (idField == null) {
                idField = allFields.stream().filter(f -> MultiConstant.Strings.ID_FIELD_NAME_DEFAULT_CODE.equals(f.getName())).findAny().orElse(null);
            }
            MultiUtil.assertNoNull(idField, "找不到id字段(或者@MutilTableId对应属性){0}", tableClass);
            return idField;
        });
    }

    /**
     * 获取表的deleted属性
     *
     * @param tableClass 表对应类
     */
    public static String getTableLogicFieldName(Class<?> tableClass) {
        return TABLE_CLASS_LOGIC_FIELD_MAP.computeIfAbsent(tableClass, (clazz) -> {
            List<Field> allFields = MultiUtil.getAllFields(clazz);
            Field idField = allFields.stream().filter(f ->
                            "deleted".equals(f.getName())
                    // || null != f.getAnnotation(TableLogic.class)
            ).findAny().orElse(null);
            if (null != idField) {
                return MultiUtil.camelToUnderline(idField.getName());
            }
            // MultiUtil.assertNoNull(idField, "找不到id字段(或者@MutilTableId对应属性){0}", tableClass);
            return null;
        });
    }

//    public static Map<String, MultiTuple3<Field, Method, Method>> computeIfAbsentClassFieldMap(Class<?> tableClass) {
//        return getFieldSetMethod(tableClass, TABLE_CLASS_FIELD_SET_METHOD_MAP, false);
//    }

    private static Map<String, MultiTuple3<Field, Method, Method>> getFieldSetMethod(Class<?> tableClass, Map<Class<?>, Map<String, MultiTuple3<Field, Method, Method>>> map, boolean containsTransient) {
        return map.computeIfAbsent(tableClass, (key) ->
        {
            Map<String, Method> setMethodMap = getMethodMap(tableClass, containsTransient, "set", parameterTypes -> parameterTypes.length == 1 && MultiUtil.isBasicDataType(parameterTypes[0]));
            Map<String, Method> getMethodMap = getMethodMap(tableClass, containsTransient, "get", parameterTypes -> parameterTypes.length == 0);

            return Collections.unmodifiableMap(MultiUtil.getAllFields(tableClass).stream()
                    .filter(field -> unStaticUnFinal(field.getModifiers(), containsTransient))
                    .filter(field -> MultiUtil.isBasicDataType(field.getType()))
                    .filter(field -> {
                        MultiTableField multiTableField = field.getAnnotation(MultiTableField.class);
                        return null == multiTableField || multiTableField.exist();
                    })
                    .collect(Collectors.toMap(Field::getName,
                            f -> new MultiTuple3<>(f
                                    , MultiUtil.assertNoNull(setMethodMap.get(f.getName()), "{0}类中{1}属性没有set方法", tableClass, f.getName())
                                    , MultiUtil.assertNoNull(getMethodMap.get(f.getName()), "{0}类中{1}属性没有get方法", tableClass, f.getName())
                            ), (v1, v2) -> v1)));
        });
    }

    private static Map<String, Method> getMethodMap(Class<?> tableClass, boolean containsTransient, String methodHead, Function<Class<?>[], Boolean> parameterTypesFilter) {
        Map<String, Method> setMethodMap = MultiUtil.getAllMethods(tableClass).stream()
                .filter(m -> unStaticUnFinal(m.getModifiers(), containsTransient))
                .filter(m -> m.getName().startsWith(methodHead))
                .filter(m -> parameterTypesFilter.apply(m.getParameterTypes()))
                .filter(field -> {
                    MultiTableField multiTableField = field.getAnnotation(MultiTableField.class);
                    return null == multiTableField || multiTableField.exist();
                })
                .collect(Collectors.toMap(m -> MultiUtil.firstToLowerCase(m.getName().substring(3)), m -> m, (v1, v2) -> v1));
        return setMethodMap;
    }

//    public static Map<String, MultiTuple3<Field, Method, Method>> computeIfAbsentClassFieldMapFull(Class<?> tableClass) {
//        return getFieldSetMethod(tableClass, TABLE_CLASS_FIELD_SET_METHOD_MAP_FULL, true);
//    }

    private static boolean unStaticUnFinal(int modifiers, boolean containsTransient) {
        return !Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers) && (containsTransient || !Modifier.isTransient(modifiers));
    }


}
