package com.pro.framework.api.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.pro.framework.api.model.IModel;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CollUtils extends CollUtil {
    /**
     *
     */
    public static <PROP, T> Set<PROP> propSet(Collection<T> list, Function<T, PROP> keyFun) {
        return list.stream().map(keyFun).collect(Collectors.toSet());
    }

    /**
     *
     */
    public static <KEY, T> Map<KEY, T> listToMap(Collection<T> list, Function<T, KEY> keyFun) {
        return listToMap(list, keyFun, o -> o);
    }

    /**
     * list转Map  同key不能相同,value不能为空
     */
    public static <KEY, VALUE, T> Map<KEY, VALUE> listToMap(Collection<T> list, Function<T, KEY> keyFun, Function<T, VALUE> valueFun) {
        return list.stream().collect(Collectors.toMap(keyFun, valueFun, (v1, v2) -> {
            throw new RuntimeException("不能重复|v1=" + v1 + "v2=" + v2);
        }, LinkedHashMap::new));
    }

    public static Map<String, Object> flatMap(Map<String, ?> map) {
        if (map == null) {
            return null;
        }
        Map<String, Object> mapNew = new LinkedHashMap<>();
        map.forEach((key, val) -> {
            if (val instanceof IModel) {
                BeanUtil.beanToMap(val).forEach((subKey, subValue) -> {
                    mapNew.put(key + "." + subKey, subValue);
                });
            } else {
                mapNew.put(key, val);
            }
            //            if (null == val || val instanceof String || val instanceof Number || ClassUtil.isBasicType(val.getClass())) {
            //                mapNew.put(key, val);
            //            } else {
            //                BeanUtil.beanToMap(val).forEach((subKey, subValue) -> {
            //                    mapNew.put(key + "." + subKey, subValue);
            //                });
            //            }
        });
        return mapNew;
    }

    /**
     * list转Map  key和value可以为空,同key会覆盖,value可以为空
     */
    public static <KEY, Value, T> Map<KEY, Value> listToMapAllRight(List<T> list, Function<T, KEY> keySupplier, Function<T, Value> valueSupplier) {
        Map<KEY, Value> map = new LinkedHashMap<>();
        for (T t : list) {
            map.put(keySupplier.apply(t), valueSupplier.apply(t));
        }
        return map;
    }

    /**
     * 分割大数组，每400个一组执行操作，然后合并结果 (小批次处理,性能最佳)
     */
    public static <T, V> List<V> execute(List<T> list, Function<List<T>, V> function) {
        return execute(list, 400, function);
    }

    public static <T, V> void execute(List<T> list, java.util.function.Consumer<List<T>> consumer) {
        execute(list, 400, consumer);
    }

    public static <T, V> List<V> execute(List<T> list, int batchSize, Function<List<T>, V> function) {
        return list.stream()
                .collect(Collectors.groupingBy(e -> list.indexOf(e) / batchSize))
                .values().stream().map(function)
                .collect(Collectors.toList());
    }

    public static <T, V> void execute(List<T> list, int batchSize, java.util.function.Consumer<List<T>> consumer) {
        list.stream()
                .collect(Collectors.groupingBy(e -> list.indexOf(e) / batchSize))
                .values().forEach(consumer);
    }

    public static <T> BigDecimal sum(List<T> list, Function<T, BigDecimal> amountFun) {
        return list.stream().map(amountFun).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

    public static <T> Integer sumInteger(List<T> list, Function<T, Integer> amountFun) {
        return list.stream().map(amountFun).reduce(Math::addExact).orElse(0);
    }
}
