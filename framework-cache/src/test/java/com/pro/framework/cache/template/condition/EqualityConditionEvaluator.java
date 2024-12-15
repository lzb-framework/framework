package com.pro.framework.cache.template.condition;



import java.util.Map;

public class EqualityConditionEvaluator implements ConditionEvaluator {
    @Override
    public boolean evaluate(String condition, Map<String, Object> params) {
        String[] parts = condition.split("==");
        if (parts.length == 2) {
            Object left = getValue(parts[0].trim(),params);
            Object right = getValue(parts[1].trim(),params);
            return left.equals(right);
        }
        return false;
    }
    private static Object getValue(String condition, Map<String, Object> params){
        if (condition.startsWith("'") && condition.endsWith("'")) {
            return condition.replace("'", "");
        }
        return params.get(condition);
    }
}