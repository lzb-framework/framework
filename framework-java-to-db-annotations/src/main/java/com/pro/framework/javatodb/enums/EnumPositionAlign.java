package com.pro.framework.javatodb.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnumPositionAlign {
    right("向右"),
    left("向左"),
    top("向顶部"),
    center("向中间"),
    ;
    final String label;
}
