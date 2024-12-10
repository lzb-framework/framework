package com.pro.framework.enums;

import com.pro.framework.api.enums.IEnum;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 字典枚举(页面展示可以直接在这枚举里取)
 *
 * @author Administrator
 */
@Slf4j
public class EnumUtil {
    /**
     * 【jdk内部】枚举name()【存入库】
     * 【jdk内部】枚举变量$VALUES,即values()返回的内容
     * 【jdk内部】枚举values()
     */
    private static final String FIELD_NAME = "name";
    private static final String FIELD_VALUES = "$VALUES";
    private static final String METHOD_VALUES = "values";
    /**
     * 枚举label【界面展示】
     */
    private static final String FIELD_LABEL = "label";

    /**
     * 取枚举所有项的每个字段,name和label字段组成 Map<name,LABEL>
     *
     * @return 例如 [{name:'INIT',LABEL:'初始化',color:'blue'},{name:'SUCCESS',LABEL:'成功',color:'green'}]
     */
    public static <T extends Enum<T>> List<Map<String, ?>> getFullList(Class<T> eClass) {
        return enumList(eClass).stream().map(EnumUtil::toMap).collect(Collectors.toList());
    }

    /**
     * 枚举实例 转
     *
     * @param t 枚举实例                例如,EnumExample.INIT
     * @return Map(propName, values)    例如,{name:'INIT',label:'初始化',color:'blue'}
     */
    @SneakyThrows
    public static <T extends Enum<T>> Map<String, Object> toMap(T t) {
        Map<String, Object> map = new HashMap<>(16);
        Class<?> aClass = t.getClass();
        for (Field field : aClass.getDeclaredFields()) {
            if (!field.isEnumConstant() && !(Modifier.isFinal(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) && !FIELD_VALUES.equals(field.getName())) {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                map.put(field.getName(), field.get(t));
            }
        }
        map.computeIfAbsent("code", k -> t.name());
        map.computeIfAbsent("label", k -> (t instanceof IEnum) ? ((IEnum) t).getLabel() : t.name());
        return map;
    }

    /**
     * 获取字典
     *
     * @return Map(name, label)
     * label>  例如 {INIT:'初始化',SUCCESS:'成功'}
     */
    @SneakyThrows
    public static <T extends Enum<T>> Map<Serializable, String> getNameLabelMap(Class<T> eClass) {
        Map<Serializable, String> map = new LinkedHashMap<>(16);
        try {
            for (T t : enumList2(eClass)) {
                map.put(getDbValue(t), getLabel(t));
            }
        } catch (NoSuchFieldException e) {
            for (T t : enumList2(eClass)) {
                map.put(getDbValue(t), null);
            }
        }
        return map;
    }

    private static <T extends Enum<T>> Serializable getDbValue(T t) {
        if (IEnum.class.isAssignableFrom(t.getClass())) {
            return ((IEnum) t).getCode();
        }
        return t.name();
    }

    private static <T> String getLabel(T t) throws NoSuchFieldException, IllegalAccessException {
        if (IEnum.class.isAssignableFrom(t.getClass())) {
            return ((IEnum) t).getLabel();
        }
        Field label = t.getClass().getDeclaredField(EnumUtil.FIELD_LABEL);
        label.setAccessible(true);
        return (String) label.get(t);
    }


    @SneakyThrows
    public static <T extends Enum<T>> List<T> enumList(Class<T> clazz) {
        try {
            return new ArrayList<>(EnumSet.allOf(clazz));
        } catch (ClassCastException e) {
            log.error("Enum.enumList()异常|" + e.getMessage() + "|className=" + clazz);
            return new ArrayList<>(0);
        }
    }

    @SneakyThrows
    public static <T extends Enum<T>> List<T> enumList2(Class<T> clazz) {
        if (null == clazz || !clazz.isEnum()) {
            throw new Exception("无法获取枚举字典,无效的枚举类=" + clazz);
        }
        return enumList(clazz);
    }

    public static String lowerFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        char[] charArray = str.toCharArray();
        charArray[0] = Character.toLowerCase(charArray[0]);
        return new String(charArray);
    }
}
