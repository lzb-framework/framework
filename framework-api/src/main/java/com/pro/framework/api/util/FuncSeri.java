package com.pro.framework.api.util;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @author Administrator
 */
@FunctionalInterface
public interface FuncSeri<T, R> extends Function<T, R>, Serializable {
}
