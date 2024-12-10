package com.pro.framework.javatodb.model;

import com.pro.framework.javatodb.annotation.JTDField;
import com.pro.framework.javatodb.annotation.JTDTable;

@JTDTable
public class ExampleClazz {
    @JTDField
    private Long id;
    @JTDField
    private String field;
}
