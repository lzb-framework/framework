package com.pro.framework.api.enums;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE) // 该注解用于类级别
@Retention(RetentionPolicy.RUNTIME) // 在运行时保留注解
public @interface EnumOrder {
    int value() default 100; // 默认为最大值，表示优先级最低
}
