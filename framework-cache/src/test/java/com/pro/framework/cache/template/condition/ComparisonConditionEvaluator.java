package com.pro.framework.cache.template.condition;

import java.util.Map;

public class ComparisonConditionEvaluator implements ConditionEvaluator {
    @Override
    public boolean evaluate(String condition, Map<String, Object> params) {
        if (condition.contains(">")) {
            String[] parts = condition.split(">");
            String left = parts[0].trim();
            String right = parts[1].trim();

            // 获取左侧参数值
            Object leftValue = params.get(left);
            if (leftValue == null) {
                return false;
            }

            // 解析右侧的值（可以是浮动数或整数）
            try {
                double rightValue = Double.parseDouble(right);  // 支持小数比较
                double leftValueAsDouble = parseToDouble(leftValue);  // 支持整数与浮动数之间的比较
                return leftValueAsDouble > rightValue;
            } catch (NumberFormatException e) {
                return false;  // 如果解析失败，返回 false
            }
        } else if (condition.contains("<")) {
            String[] parts = condition.split("<");
            String left = parts[0].trim();
            String right = parts[1].trim();

            // 获取左侧参数值
            Object leftValue = params.get(left);
            if (leftValue == null) {
                return false;
            }

            // 解析右侧的值（可以是浮动数或整数）
            try {
                double rightValue = Double.parseDouble(right);  // 支持小数比较
                double leftValueAsDouble = parseToDouble(leftValue);  // 支持整数与浮动数之间的比较
                return leftValueAsDouble < rightValue;
            } catch (NumberFormatException e) {
                return false;  // 如果解析失败，返回 false
            }
        }
        return false;
    }

    /**
     * 将输入的对象转换为 double 类型
     * @param value 输入值，可能是整数或浮动数
     * @return 转换后的 double 值
     */
    private double parseToDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else {
            try {
                return Double.parseDouble(value.toString());
            } catch (NumberFormatException e) {
                return 0.0;  // 无法转换为数字时返回 0.0
            }
        }
    }
}
