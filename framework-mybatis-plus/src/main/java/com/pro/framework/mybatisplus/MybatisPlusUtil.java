package com.pro.framework.mybatisplus;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.pro.framework.api.database.TimeQuery;
import com.pro.framework.api.util.ClassUtils;
import com.pro.framework.api.util.DateUtils;
import com.pro.framework.api.util.StrUtils;
import com.pro.framework.mybatisplus.wrapper.MyQueryWrapper;
import lombok.SneakyThrows;
import org.apache.ibatis.reflection.property.PropertyNamer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.regex.Pattern;

public class MybatisPlusUtil {
    public static Pattern PATTERN_OPTION = Pattern.compile("##.+##");

    public static <T> String getFieldName(SFunction<T, ?> sFunction) {
        return StrUtils.camelToUnderline(PropertyNamer.methodToProperty(LambdaUtils.extract(sFunction).getImplMethodName()));
    }

    /**
     * 组装过滤条件
     */
    @SneakyThrows
    public static <T> QueryWrapper<T> create(T entity, TimeQuery timeQuery) {
        QueryWrapper<T> wrapper = new MyQueryWrapper<>();
        if (entity != null) {
            for (Field field : ClassUtils.getDeclaredFields(entity.getClass())) {
                if (Modifier.isTransient(field.getModifiers())) {
                    continue;
                }
                addSort(field.getName(), wrapper);
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                String name = StrUtils.camelToUnderline(field.getName());
                Object val = field.get(entity);
                if (val != null && !StrUtils.EMPTY.equals(val)) {

                    //某个字段对应的扩展字段,便于in,等其他条件模式
                    if (name.endsWith("Str") && val instanceof String && PATTERN_OPTION.matcher(val.toString()).find()) {
                        String valString = val.toString();
                        String optMatch = PATTERN_OPTION.matcher(val.toString()).group();
                        String opt = optMatch.substring(2, optMatch.length() - 2);
                        //noinspection SwitchStatementWithTooFewBranches
                        switch (opt) {
                            case "in":
                                String[] values = valString.substring(valString.indexOf(optMatch)).split(",");
                                wrapper.in(name, (Object[]) values);
                                break;
                            // case "isNull":
                            //     wrapper.isNull(name);
                            //     break;
                        }
                        //去掉str
                        name = name.substring(0, name.length() - 3);
                    } else {
//                        if (field.getAnnotation(UnLike.class) == null && String.class.equals(field.getType())) {
//                            //每个字段like
//                            wrapper.like(name, val);
//                        } else {
//
//                        }
                        //每个字段eq
                        wrapper.eq(name, val);
                    }
                }
            }
        }
        if (timeQuery != null) {
            String timeColumn = StrUtils.camelToUnderline(timeQuery.getTimeColumn());
            wrapper.ge(StrUtils.isNotBlank(timeQuery.getStart()), timeColumn, DateUtils.parseDateTime(timeQuery.getStart(), true));
            wrapper.le(StrUtils.isNotBlank(timeQuery.getEnd()), timeColumn, DateUtils.parseDateTime(timeQuery.getEnd(), false));
        }
        return wrapper;
    }

    private static <T> void addSort(String fieldName, QueryWrapper<T> wrapper) {
        if (fieldName != null) {
            switch (fieldName) {
                case "sort":
                    wrapper.orderByAsc("sort");
                    break;
            }
        }
    }
}
