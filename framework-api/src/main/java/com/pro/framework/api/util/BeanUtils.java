package com.pro.framework.api.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.pro.framework.api.FrameworkConst;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

public class BeanUtils extends BeanUtil {
    public static void copyPropertiesModel(Object o1, Object o2) {
        copyProperties(o1, o2, CopyOptions.create().ignoreNullValue().setIgnoreProperties(FrameworkConst.Str.MODEL_IGNORE_PROPERTIES));
    }

    @SuppressWarnings("unchecked")
    public static <T> T convert(String value, Class<T> targetType) {
        if (targetType == null || targetType == String.class) {
            return (T) value;
        }
        if (targetType == BigDecimal.class) {
            return (T) new BigDecimal(value);
        } else if (targetType == BigInteger.class) {
            return (T) new BigInteger(value);
        } else if (targetType == Integer.class || targetType == int.class) {
            return (T) Integer.valueOf(value);
        } else if (targetType == Long.class || targetType == long.class) {
            return (T) Long.valueOf(value);
        } else if (targetType == Double.class || targetType == double.class) {
            return (T) Double.valueOf(value);
        } else if (targetType == Float.class || targetType == float.class) {
            return (T) Float.valueOf(value);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return (T) Boolean.valueOf(value);
        } else if (targetType == Short.class || targetType == short.class) {
            return (T) Short.valueOf(value);
        } else if (targetType == Byte.class || targetType == byte.class) {
            return (T) Byte.valueOf(value);
        } else if (targetType == Character.class || targetType == char.class) {
            if (value.length() == 1) {
                return (T) Character.valueOf(value.charAt(0));
            } else {
                throw new IllegalArgumentException("Cannot convert a String with more than one character to char");
            }
        } else if (Enum.class.isAssignableFrom(targetType)) {
            return (T) Arrays.stream(((Class<Enum>) targetType).getEnumConstants()).filter(e -> e.name().equals(value)).findAny().orElse(null);
        } else {
            throw new IllegalArgumentException("Unsupported target type: " + targetType.getName());
        }
    }
}
