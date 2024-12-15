package com.pro.framework.cache.template.condition;



import java.util.HashMap;
import java.util.Map;

public class ConditionContext {
    private final Map<String, ConditionEvaluator> evaluators = new HashMap<>();

    // 注册不同的条件解析器策略
    public void addEvaluator(String operator, ConditionEvaluator evaluator) {
        evaluators.put(operator, evaluator);
    }

    // 根据条件选择合适的策略并进行评估
    public boolean evaluate(String condition, Map<String, Object> params) {
        for (Map.Entry<String, ConditionEvaluator> entry : evaluators.entrySet()) {
            if (condition.contains(entry.getKey())) {
                return entry.getValue().evaluate(condition, params);
            }
        }
        return false;
    }
}
