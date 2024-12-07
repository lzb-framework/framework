package com.pro.framework.javatodb.util;

import lombok.SneakyThrows;

import java.text.MessageFormat;

/**
 * 工具类
 *
 * @author administrator
 * @date 2022-01-20
 */
public class JTDAssertUtil {

    public static final String EMPTY = "";


    /**
     * @param errMsg 例如 name={0}, age={1}
     */
    @SneakyThrows
    public static void true_(Boolean flag, String errMsg, String... params) {
        if (null == flag || !flag) {
            throw new JTDException(MessageFormat.format(errMsg, (Object[]) params));
        }
    }


    @SneakyThrows
    public static void notNull(Object o, String errMsg, String... params) {
        if (null == o) {
            throw new JTDException(MessageFormat.format(errMsg, (Object[]) params));
        }
    }

    @SneakyThrows
    public static void notBlack(String s, String errMsg, String... params) {
        if (isBlank(s)) {
            throw new JTDException(MessageFormat.format(errMsg, (Object[]) params));
        }
    }

    public static boolean isBlank(String str) {
        return null == str || EMPTY.equals(str.trim());
    }

    // public static boolean isNotBlank(String str) {
    //     return !isBlank(str);
    // }

}
