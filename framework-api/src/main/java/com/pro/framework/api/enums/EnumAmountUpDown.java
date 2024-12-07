package com.pro.framework.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum EnumAmountUpDown implements IEnum {
    up("增加", o -> o),
    down("减少", BigDecimal::negate),
    ;
    private String label;
    private Function<BigDecimal, BigDecimal> dealNumFun;
    public static final Map<String, EnumAmountUpDown> MAP = Arrays.stream(values()).collect(Collectors.toMap(EnumAmountUpDown::name, o -> o));

}
