package com.pro.framework.api.util;


import com.pro.framework.api.model.FrameworkException;

import java.util.List;

public class AssertUtil {

    public static <T> List<T> notEmpty(List<T> o, String errorMsgTemplate) {
        isTrue(null != o && !o.isEmpty(), errorMsgTemplate);
        return o;
    }

    public static String notEmpty(String o, String errorMsgTemplate) {
        isTrue(null != o && !o.isEmpty(), errorMsgTemplate);
        return o;
    }

    public static <T> T notEmpty(T o, String errorMsgTemplate) {
        isTrue(null != o, errorMsgTemplate);
        return o;
    }

    public static <T> List<T> notEmpty(List<T> o, String errorMsgTemplate, Object... param) {
        isTrue(null != o && !o.isEmpty(), errorMsgTemplate, param);
        return o;
    }

    public static String notEmpty(String o, String errorMsgTemplate, Object... param) {
        isTrue(null != o && !o.isEmpty(), errorMsgTemplate, param);
        return o;
    }

    public static <T> T notEmpty(T o, String errorMsgTemplate, Object... param) {
        isTrue(null != o, errorMsgTemplate, param);
        return o;
    }

    public static void isTrue(Boolean flag, String errorMsgTemplate, Object... param) {
        if (null == flag || !flag) {
            throw new FrameworkException(errorMsgTemplate, param);
        }
    }

}
