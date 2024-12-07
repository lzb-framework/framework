package com.pro.framework.api.database;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeQuery {
    /**
     * 过滤时间字段名
     */
    private String timeColumn = "createTime";
    /**
     * 创建时间开始
     */
    private String start;
    /**
     * 创建时间结束
     */
    private String end;
}
