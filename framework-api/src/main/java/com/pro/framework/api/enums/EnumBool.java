package com.pro.framework.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnumBool implements IEnum{

    _0("否"),
    _1("是"),
    ;

    private final String label;


}
