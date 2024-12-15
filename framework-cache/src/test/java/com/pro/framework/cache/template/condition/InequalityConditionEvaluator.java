package com.pro.framework.cache.template.condition;


import java.util.Map;

public class InequalityConditionEvaluator implements ConditionEvaluator {
    @Override
    public boolean evaluate(String condition, Map<String, Object> params) {
        String[] parts = condition.split("!=");
        if (parts.length == 2) {
            String left = parts[0].trim();
            String right = parts[1].trim().replace("'", "");

            Object paramValue = params.get(left);
            return paramValue != null && !paramValue.toString().equals(right);
        }
        return false;
    }
}