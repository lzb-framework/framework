package com.pro.framework.api.database;


import com.pro.framework.api.structure.FirstHashMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * mysql聚合函数返回信息
 * 统一只有三种类型(根据源字段类型)
 * Long,BigDecimal,String
 * map的key为字段名
 * @author Administrator
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AggregateResult {
    /**
     * 求和(支持多字段计算)
     */
    private FirstHashMap<String, ?> props = FirstHashMap.emptyMap();
    /**
     * 求和(支持多字段计算)
     */
    private FirstHashMap<String, ?> sum = FirstHashMap.emptyMap();
    /**
     * 求平均值
     */
    private FirstHashMap<String, ?> avg = FirstHashMap.emptyMap();
    /**
     * 计数
     */
    private Long count;
    /**
     * 计数去重
     */
    private FirstHashMap<String, Long> countDistinct = FirstHashMap.emptyMap();
    /**
     * 最大值
     */
    private FirstHashMap<String, ?> max = FirstHashMap.emptyMap();
    /**
     * 最小值
     */
    private FirstHashMap<String, ?> min = FirstHashMap.emptyMap();
    /**
     * 分组组合拼接
     */
    private FirstHashMap<String, String> groupConcat = FirstHashMap.emptyMap();
}
