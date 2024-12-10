package com.pro.framework.api.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

public class LogicUtils {

    /**
     * 取第一个不为空对象
     */
    @SafeVarargs
    public static <T extends String> T or(T... list) {
        return Arrays.stream(list).filter(StrUtils::isNotBlank).findAny().orElse(list[list.length - 1]);
    }

    /**
     * 取第一个不为空对象
     */
    @SafeVarargs
    public static <T extends Object> T or(T... list) {
        return Arrays.stream(list).filter(Objects::nonNull).findAny().orElse(list[list.length - 1]);
    }

    /**
     * 若t不为空 ,返回 fun.apply(t)
     */
    public static <T, R> R and(T t, Function<T, R> fun) {
        if (t == null) {
            return null;
        }
        return fun.apply(t);
    }

    /**
     * 若t不为空 ,返回 fun.apply(t)
     */
    public static <T extends Collection, R> R and(T t, Function<T, R> fun) {
        if (null == t) {
            return null;
        }
        return fun.apply(t);
    }

    /**
     * 若t不为空 ,返回 fun.apply(t)
     */
    public static <T, R1, R2> R2 and(T t, Function<T, R1> fun1, Function<R1, R2> fun2) {
        return and(and(t, fun1), fun2);
    }


//    /**
//     * 3元表达式
//     */
//    public static <T> T bool(Boolean bool, Supplier<T> trueSupplier, Supplier<T> falseSupplier) {
//        if (null == bool || !bool) {
//            return and(falseSupplier, Supplier::get);
//        }
//        return and(trueSupplier, Supplier::get);
//    }
}
