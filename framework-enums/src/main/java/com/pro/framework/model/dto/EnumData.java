package com.pro.framework.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EnumData<ENTITY> {
    private ENTITY entity;
    private String enumToDbCode;
    private String forceChangeTime;
}
