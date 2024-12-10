package com.pro.framework.api.util.inner;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @author Administrator
 */
@FunctionalInterface
public interface FunSerializable<T, R> extends Function<T, R>, Serializable {
}
