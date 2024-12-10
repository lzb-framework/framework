package com.pro.framework.api.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

public class NumUtils {

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T extends Comparable> Boolean between(T start, T amount, T end) {
        if (amount == null) {
            return false;
        }
        boolean startOK = gt0(start) ? amount.compareTo(start) >= 0 : true;
        boolean endOK = gt0(end) ? amount.compareTo(end) <= 0 : true;
        return startOK && endOK;
    }

    public static <T extends Comparable> Boolean gt0(T... numbers) {
        return Arrays.stream(numbers).allMatch(Objects::nonNull) && Arrays.stream(numbers).allMatch(n -> {
            if (n instanceof Integer) {
                return n.compareTo(0) > 0;
            }
            if (n instanceof Long) {
                return n.compareTo(0L) > 0;
            }
            if (n instanceof Double) {
                return n.compareTo(0d) > 0;
            }
            if (n instanceof BigInteger) {
                return n.compareTo(BigInteger.ZERO) > 0;
            }
            if (n instanceof BigDecimal) {
                return n.compareTo(BigDecimal.ZERO) > 0;
            }
            return false;
        });
    }

    public static <T extends Comparable> Boolean ge(T... numbers) {
        return Arrays.stream(numbers).allMatch(Objects::nonNull) && Arrays.stream(numbers).allMatch(n -> {
            if (n instanceof Integer) {
                return n.compareTo(0) >= 0;
            }
            if (n instanceof Long) {
                return n.compareTo(0L) >= 0;
            }
            if (n instanceof Double) {
                return n.compareTo(0d) >= 0;
            }
            if (n instanceof BigInteger) {
                return n.compareTo(BigInteger.ZERO) >= 0;
            }
            if (n instanceof BigDecimal) {
                return n.compareTo(BigDecimal.ZERO) >= 0;
            }
            return false;
        });
    }

    public static Long getLong(String numStr) {
        if (null == numStr || numStr.isEmpty() || !numStr.matches("\\d+")) {
            return null;
        }
        return Long.valueOf(numStr);
    }
}
