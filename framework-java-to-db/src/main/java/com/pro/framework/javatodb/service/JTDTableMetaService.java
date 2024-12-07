package com.pro.framework.javatodb.service;

import com.pro.framework.javatodb.model.JTDTableInfo;
import com.pro.framework.javatodb.model.JTDSqlInfo;

import java.io.Serializable;
import java.util.List;

public interface JTDTableMetaService {

    /**
     * 获取版本序列 获取取全内容的hashcode来判断版本是否一致
     */
    Serializable getVersionSequence(JTDTableInfo table);

    /**
     * 新表,旧表,对比生成表格修改语句
     */
    List<JTDSqlInfo> calculateAlterTableSqls(JTDTableInfo told, JTDTableInfo tnew);
}
