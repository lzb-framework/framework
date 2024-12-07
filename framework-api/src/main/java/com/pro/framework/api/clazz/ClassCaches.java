package com.pro.framework.api.clazz;

import com.pro.framework.api.structure.Tuple2;
import com.pro.framework.api.structure.Tuple3;
import com.pro.framework.api.util.ClassUtils;
import com.pro.framework.api.entity.TableField;
import com.pro.framework.api.entity.TableId;
import com.pro.framework.api.util.AssertUtil;
import com.pro.framework.api.util.StrUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 表对应实体,表和表关系的缓存
 * String若为属性名,类名则为驼峰小写开头  只有在输出sql进行驼峰转下划线处理
 *
 * @author Administrator
 */
@SuppressWarnings({"unused", "AlibabaLowerCamelCaseVariableNaming"})
public class ClassCaches {
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
     * 类的具体信息
     * Map<类,Map<fieldName,Tuple<Field,setMethod,getMethod></>>>
     */
    private static final Map<Class<?>, Map<String, Tuple3<Field, Method, Method>>> TABLE_CLASS_FIELD_SET_METHOD_MAP = new WeakHashMap<>(4096);
    private static final Map<Class<?>, Map<String, Tuple3<Field, Method, Method>>> TABLE_CLASS_FIELD_SET_METHOD_MAP_FULL = new WeakHashMap<>(4096);
    /**
     * 主表-子表数据,set到主表对象上去
     */
    private static final Map<Class<?>, Map<String, Tuple2<Method, Method>>> TABLE_WITH_TABLE_GET_SET_METHOD_MAP = new WeakHashMap<>(4096);

    /**
     * 表中每个属性和set方法
     */
    public static Map<String, Tuple3<Field, Method, Method>> getClassInfos(Class<?> tableClass) {
        return getFieldSetMethod(tableClass, TABLE_CLASS_FIELD_SET_METHOD_MAP, false);
    }

    public static Map<String, Tuple3<Field, Method, Method>> getClassInfosFull(Class<?> tableClass) {
        return getFieldSetMethod(tableClass, TABLE_CLASS_FIELD_SET_METHOD_MAP_FULL, true);
    }

    /**
     * 表中所有属性名称
     */
    public static List<String> getFieldNamesByClass(Class<?> tableClass) {
        return getClassInfos(tableClass).values().stream().map(tuple2 -> tuple2.getT1().getName()).collect(Collectors.toList());
    }

    /**
     * 获取表的id属性
     */
    public static Field getTableIdField(Class<?> tableClass, String propName) {
        return TABLE_CLASS_ID_FIELD_MAP.computeIfAbsent(tableClass, (clazz) -> {
            List<Field> allFields = ClassUtils.getAllFields(clazz);
            Field idField = allFields.stream().filter(f -> null != f.getAnnotation(TableId.class)).findAny().orElse(null);
            if (idField == null) {
                idField = allFields.stream().filter(f -> propName.equals(f.getName())).findAny().orElse(null);
            }
            AssertUtil.notEmpty(idField, "找不到id字段(或者@MutilTableId对应属性){0}", tableClass);
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
            List<Field> allFields = ClassUtils.getAllFields(clazz);
            Field idField = allFields.stream().filter(f ->
                            "deleted".equals(f.getName())
                    // || null != f.getAnnotation(TableLogic.class)
            ).findAny().orElse(null);
            if (null != idField) {
                return StrUtils.camelToUnderline(idField.getName());
            }
             AssertUtil.notEmpty(idField, "找不到id字段(或者@MutilTableId对应属性){0}", tableClass);
            return null;
        });
    }

    private static Map<String, Tuple3<Field, Method, Method>> getFieldSetMethod(Class<?> tableClass, Map<Class<?>, Map<String, Tuple3<Field, Method, Method>>> map, boolean containsTransient) {
        return map.computeIfAbsent(tableClass, (key) ->
        {
            Map<String, Method> setMethodMap = getMethodMap(tableClass, containsTransient, "set", parameterTypes -> parameterTypes.length == 1 && ClassUtils.isBasicDataType(parameterTypes[0]));
            Map<String, Method> getMethodMap = getMethodMap(tableClass, containsTransient, "get", parameterTypes -> parameterTypes.length == 0);

            return Collections.unmodifiableMap(ClassUtils.getAllFields(tableClass).stream()
                    .filter(field -> unStaticUnFinal(field.getModifiers(), containsTransient))
                    .filter(field -> ClassUtils.isBasicDataType(field.getType()))
                    .filter(field -> {
                        TableField tableField = field.getAnnotation(TableField.class);
                        return null == tableField || tableField.exist();
                    })
                    .collect(Collectors.toMap(Field::getName,
                            f -> new Tuple3<>(f
                                    , AssertUtil.notEmpty(setMethodMap.get(f.getName()), "{0}类中{1}属性没有set方法", tableClass, f.getName())
                                    , AssertUtil.notEmpty(getMethodMap.get(f.getName()), "{0}类中{1}属性没有get方法", tableClass, f.getName())
                            ), (v1, v2) -> v1)));
        });
    }

    private static Map<String, Method> getMethodMap(Class<?> tableClass, boolean containsTransient, String methodHead, Function<Class<?>[], Boolean> parameterTypesFilter) {
        Map<String, Method> setMethodMap = ClassUtils.getAllMethods(tableClass).stream()
                .filter(m -> unStaticUnFinal(m.getModifiers(), containsTransient))
                .filter(m -> m.getName().startsWith(methodHead))
                .filter(m -> parameterTypesFilter.apply(m.getParameterTypes()))
                .filter(field -> {
                    TableField tableField = field.getAnnotation(TableField.class);
                    return null == tableField || tableField.exist();
                })
                .collect(Collectors.toMap(m -> StrUtils.firstToLowerCase(m.getName().substring(3)), m -> m, (v1, v2) -> v1));
        return setMethodMap;
    }

    public static Map<String, Tuple3<Field, Method, Method>> computeIfAbsentClassFieldMapFull(Class<?> tableClass) {
        return getFieldSetMethod(tableClass, TABLE_CLASS_FIELD_SET_METHOD_MAP_FULL, true);
    }

    private static boolean unStaticUnFinal(int modifiers, boolean containsTransient) {
        return !Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers) && (containsTransient || !Modifier.isTransient(modifiers));
    }


}
