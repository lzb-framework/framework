package com.pro.framework.mtq.service.multiwrapper.util;

import com.pro.framework.mtq.service.multiwrapper.constant.MultiConstant;
import com.pro.framework.mtq.service.multiwrapper.util.mybatisplus.MultiFunction;
import com.pro.framework.mtq.service.multiwrapper.util.mybatisplus.SerializedLambda;
import com.pro.framework.mtq.service.multiwrapper.util.mybatisplus.SerializedLambdaData;
import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Administrator
 * 部分代码参考 hutool 为了避免引用多余包,因此拷贝过来
 * https://www.hutool.cn/docs/
 */
@SuppressWarnings("unused")
public class MultiUtil {


    public static String getFieldNameFinal(String relationCode, String propName) {
        String tableNameAppend;
        String fieldName;
        if (propName.indexOf(".") > 0) {
            String[] split = propName.split("\\.");
            tableNameAppend = split[0] + ".";
            fieldName = split[1];
        } else {
            tableNameAppend = null == relationCode ? "" : (relationCode + ".");
            fieldName = propName;
        }
//        MultiUtil.camelToUnderline(fieldName)
        return tableNameAppend + fieldName;
    }

    public static String dateStart(String date) {
        if (!isEmpty(date) && date.length() == 10) {
            return date + " 00:00:00";
        }
        return date;
    }

    public static String dateEnd(String date) {
        if (!isEmpty(date) && date.length() == 10) {
            return date + " 23:59:59";
        }
        return date;
    }

    public static Boolean isEmpty(Collection<?> list) {
        return null == list || list.isEmpty();
    }

    public static Boolean isEmpty(Object[] list) {
        return null == list || list.length == 0;
    }

    public static Boolean isEmpty(String str) {
        return null == str || str.length() == 0;
    }

    public static Boolean noEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 首字母转换小写
     *
     * @param param 需要转换的字符串
     * @return 转换好的字符串
     */
    public static String firstToLowerCase(String param) {
        if (null == param || param.length() == 0) {
            return MultiConstant.Strings.EMPTY;
        }
        return param.substring(0, 1).toLowerCase() + param.substring(1);
    }

    /**
     * 首字母转换小写
     *
     * @param param 需要转换的字符串
     * @return 转换好的字符串
     */
    public static String firstToUpperCase(String param) {
        if (null == param || param.length() == 0) {
            return MultiConstant.Strings.EMPTY;
        }
        return param.substring(0, 1).toUpperCase() + param.substring(1);
    }


    /**
     * <p>
     * 请仅在确定类存在的情况下调用该方法
     * </p>
     *
     * @param name 类名称
     * @return 返回转换后的 Class
     */
    public static Class<?> toClassConfident(String name) {
        try {
            return Class.forName(name, false, getDefaultClassLoader());
        } catch (ClassNotFoundException e) {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException ex) {
                throw new MultiException("找不到指定的class！请仅在明确确定会有 class 的时候，调用该方法");
            }
        }
    }


    public static <T> T assertNoNull(T o, String errorMessage, Object... params) {
        if (null == o) {
            throw new MultiException(MessageFormat.format(errorMessage, params));
        }
        return o;
    }

    /**
     * Return the default ClassLoader to use: typically the thread context
     * ClassLoader, if available; the ClassLoader that loaded the ClassUtils
     * class will be used as fallback.
     * <p>Call this method if you intend to use the thread context ClassLoader
     * in a scenario where you clearly prefer a non-null ClassLoader reference:
     * for example, for class path resource loading (but not necessarily for
     * {@code Class.forName}, which accepts a {@code null} ClassLoader
     * reference as well).
     *
     * @return the default ClassLoader (only {@code null} if even the system
     * ClassLoader isn't accessible)
     * @see Thread#getContextClassLoader()
     * @see ClassLoader#getSystemClassLoader()
     * @since 3.3.2
     */
    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = MultiUtil.class.getClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                }
            }
        }
        return cl;
    }


    /**
     * Serialize the given object to a byte array.
     *
     * @param object the object to serialize
     * @return an array of bytes representing the object in a portable fashion
     */
    public static byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(object);
            oos.flush();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to serialize object of type: " + object.getClass(), ex);
        }
        return baos.toByteArray();
    }

    /**
     * 字符串驼峰转下划线格式
     *
     * @param param 需要转换的字符串
     * @return 转换好的字符串
     */
    public static String camelToUnderline(String param) {
        if (null == param || param.length() == 0) {
            return MultiConstant.Strings.EMPTY;
        }
        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                sb.append(MultiConstant.Strings.UNDERLINE);
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }


//    /**
//     * 字符串下划线转驼峰格式
//     *
//     * @param param 需要转换的字符串
//     * @return 转换好的字符串
//     */
//    public static String underlineToCamel(String param) {
//        if (null == param || param.length() == 0) {
//            return MultiConstant.Strings.EMPTY;
//        }
//        String temp = param.toLowerCase();
//        int len = temp.length();
//        StringBuilder sb = new StringBuilder(len);
//        for (int i = 0; i < len; i++) {
//            char c = temp.charAt(i);
//            if (c == MultiConstant.Strings.UNDERLINE) {
//                if (++i < len) {
//                    sb.append(Character.toUpperCase(temp.charAt(i)));
//                }
//            } else {
//                sb.append(c);
//            }
//        }
//        return sb.toString();
//    }

    public static List<Field> getAllFields(Class<?> clazz) {
        return getClassMetas(clazz, Class::getDeclaredFields);
    }

    @SafeVarargs
    public static List<Field> getAllFields(Class<?> clazz, Predicate<Field>... filters) {
        return getClassMetas(clazz, Class::getDeclaredFields).stream()
                .filter(Arrays.stream(filters).reduce(x -> true, Predicate::and))
                .collect(Collectors.toList());
    }

    // 定义过滤器
    public static final Predicate<Field> isStatic = field -> Modifier.isStatic(field.getModifiers());
    public static final Predicate<Field> noStatic = isStatic.negate();
    public static final Predicate<Field> isFinal = field -> Modifier.isFinal(field.getModifiers());
    public static final Predicate<Field> noFinal = isFinal.negate();
    public static final Predicate<Field> isTransient = field -> Modifier.isTransient(field.getModifiers());
    public static final Predicate<Field> noTransient = isTransient.negate();


    public static List<Method> getAllMethods(Class<?> clazz) {
        return getClassMetas(clazz, Class::getDeclaredMethods);
    }

    private static <T> List<T> getClassMetas(Class<?> clazz, Function<Class<?>, T[]> getOneClassMetaFun) {
        List<T> list = new ArrayList<>(64);
        Class<?> currClazz = clazz;
        while (!currClazz.equals(Object.class)) {
            list.addAll(Arrays.asList(getOneClassMetaFun.apply(currClazz)));
            currClazz = currClazz.getSuperclass();
        }
        return list;
    }

//    public static String methodNameToFieldName(String methodName) {
//        return camelToUnderline(methodName.substring(3));
//    }

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

    private static boolean isPrimitiveWrapper(Class<?> clazz) {
        return null != clazz && WRAPPER_PRIMITIVE_MAP.containsKey(clazz);
    }

    public static final Map<Class<?>, Class<?>> WRAPPER_PRIMITIVE_MAP = new ConcurrentHashMap<>(8);

    static {
        WRAPPER_PRIMITIVE_MAP.put(Boolean.class, Boolean.TYPE);
        WRAPPER_PRIMITIVE_MAP.put(Byte.class, Byte.TYPE);
        WRAPPER_PRIMITIVE_MAP.put(Character.class, Character.TYPE);
        WRAPPER_PRIMITIVE_MAP.put(Double.class, Double.TYPE);
        WRAPPER_PRIMITIVE_MAP.put(Float.class, Float.TYPE);
        WRAPPER_PRIMITIVE_MAP.put(Integer.class, Integer.TYPE);
        WRAPPER_PRIMITIVE_MAP.put(Long.class, Long.TYPE);
        WRAPPER_PRIMITIVE_MAP.put(Short.class, Short.TYPE);
    }

    /**
     * Date转LocalDate
     */
    public static LocalDateTime date2LocalDateTime(Date date) {
        if (null == date) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * 枚举类通过name()找到枚举项
     *
     * @param type 枚举类
     * @param name name
     * @return 枚举项
     */
    @SneakyThrows
    public static <T extends Enum<T>> T getEnumByName(Class<T> type, String name) {
        if (type.isEnum()) {
            //noinspection unchecked
            return (T) type.getDeclaredMethod("valueOf", String.class).invoke(null, name);
        }
        return null;
    }


    /**
     * list 转 map
     */
    public static <T, KEY> Map<KEY, T> listToMap(Collection<T> list, Function<T, KEY> keyFun) {
        return listToMap(list, keyFun, o -> o, true);
    }


    /**
     * list 转 map
     */
    public static <T, KEY, Val> Map<KEY, Val> listToMap(Collection<T> list, Function<T, KEY> keyFun, Function<T, Val> valFun) {
        return listToMap(list, keyFun, valFun, true);
    }

    /**
     * list 转 map
     * 例如   Map<String, User> userMap = listToMap(new ArrayList<User>(), User::getUsername);
     * 把   [{id:1,username:'u1'},{id:2,username:'u2'},{id:3,username:'u3'}]
     * 转成 {1:{id:1,username:'u1'},2:{id:2,username:'u2'},3:{id:3,username:'u3'}}
     */
    public static <T, KEY, Val> Map<KEY, Val> listToMap(Collection<T> list, Function<T, KEY> keyFun, Function<T, Val> valFun, boolean repeatReplace) {
        Map<KEY, Val> map = new LinkedHashMap<>();
        if (null != list) {
            for (T t : list) {
                if (null != t) {
                    KEY key = keyFun.apply(t);
                    if (map.containsKey(key) && !repeatReplace) {
                        throw new MultiException("listToMap不能存在重复键值:" + key);
                    }
                    map.put(key, valFun.apply(t));
                }
            }
        }
        return map;
    }

    public static Class<?> getGenericFirst(Class<?> clazz) {
        return (Class<?>) ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];
    }


    @SafeVarargs
    public static <VAL, T, PROP_INFO> MultiTuple2<String, List<PROP_INFO>> calcMultiFunctions(Function<SerializedLambdaData, PROP_INFO> builder, MultiFunction<T, VAL>... propFuncs) {
        String className = null;
        List<PROP_INFO> list = new ArrayList<>(32);
        if (null != propFuncs) {
            for (MultiFunction<T, VAL> propFunc : propFuncs) {
                SerializedLambdaData serializedLambdaData = SerializedLambda.resolveCache(propFunc);
                if (className == null) {
                    className = MultiUtil.firstToLowerCase(serializedLambdaData.getClazz().getSimpleName());
                }
                list.add(builder.apply(serializedLambdaData));
            }
        }
        return new MultiTuple2<>(className, list);
    }

    public static <T> T or(T... objs) {
        return Arrays.stream(objs).filter(Objects::nonNull).findFirst().orElse(null);
    }

    /**
     * 替换字符串中的占位符为指定的值。
     *
     * @param template 模版 例如 "Hello, ${name}! Your age is ${age}."
     * @param params   参数 例如 {"name":"zhang""age":"18"}
     * @return 替换后的字符串
     */
    public static String format(String template, Map<String, Object> params) {
        Map<String, Object> paramsNew = params.entrySet().stream().collect(Collectors.toMap(e -> "${" + e.getKey() + "}", Map.Entry::getValue));
        for (Map.Entry<String, Object> entry : paramsNew.entrySet()) {
            String placeholder = entry.getKey();
            Object replacement = entry.getValue();
            if (replacement != null) {
                template = template.replace(placeholder, replacement.toString());
            }
        }
        return template;
    }

    public static String logThrowable(Throwable e) {
        StackTraceElement[] stackTrace = e.getStackTrace();
        StringBuilder s = new StringBuilder(e.getMessage() + "\n");
        for (int i = 0; i < stackTrace.length; i++) {
            String temp = stackTrace[i].toString().trim();
            if (temp.startsWith("com.pro.") && temp.contains(":")) {
                s.append("	").append(temp).append("\n");
            }
        }
        if (s.length() == 0) {
            for (int i = 0; i < stackTrace.length; i++) {
                s.append("	").append(stackTrace[i].toString()).append("\n");
            }
        }
        Throwable cause = e.getCause();
        if (cause != null) {
            s.append("	").append(logThrowable(cause)).append("\n");
        }
        return s.toString();
    }

    @SafeVarargs
    public static <T> List<T> union(List<T>... listList) {
        return Arrays.stream(listList).flatMap(Collection::stream).collect(Collectors.toList());
    }
}
