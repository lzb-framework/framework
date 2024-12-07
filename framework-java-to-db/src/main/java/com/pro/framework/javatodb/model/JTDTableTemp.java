package com.pro.framework.javatodb.model;

import com.pro.framework.javatodb.annotation.JTDTable;
import lombok.Data;

import java.lang.annotation.Annotation;

@Data
public class JTDTableTemp implements JTDTable {
    /**
     * 表中文名
     */
    String value;

    String label;

    String description;

    /**
     * 表名
     */
    String tableName;

    /**
     * 主键信息
     */
    String[] keyFieldNames;
    /**
     * 排除属性
     */
    String[] excludeFieldNames;

    /**
     * 主键信息
     */
    String[] sequences;

    /**
     * 模块(便于生成到指定目录下)
     */
    private String module;

    /**
     * 表唯一id(便于生成一些菜单id之类)
     */
    private int entityId;

    @Override
    public String value() {
        return value;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public String tableName() {
        return tableName;
    }

    @Override
    public String[] keyFieldNames() {
        return keyFieldNames;
    }

    @Override
    public String[] excludeFieldNames() {
        return excludeFieldNames;
    }

    @Override
    public String[] sequences() {
        return sequences;
    }

    @Override
    public String module() {
        return module;
    }

    @Override
    public int entityId() {
        return entityId;
    }


    @Override
    public Class<? extends Annotation> annotationType() {
        return JTDTable.class;
    }
}
