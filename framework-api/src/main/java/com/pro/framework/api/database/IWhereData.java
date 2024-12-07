package com.pro.framework.api.database;

import com.pro.framework.api.FrameworkConst;

/**
 * @author Administrator
 */
public interface IWhereData {
    /**
     * 获取where内的属性信息
     *
     * @param className 表名
     * @return sqlWhereProps
     */
    default String getSqlWhereProps(String className) {
        return FrameworkConst.Str.EMPTY;
    }
}
