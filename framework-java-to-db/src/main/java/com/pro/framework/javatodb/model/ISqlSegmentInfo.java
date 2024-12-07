package com.pro.framework.javatodb.model;

public interface ISqlSegmentInfo {
    /**
     * 转sql信息
     * @param tableName
     */
    String toSql(String tableName);
}
