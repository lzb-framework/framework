package com.pro.framework.mtq.service.multiwrapper.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 常量
 *
 * @author Administrator
 */
public class MultiConstant {

    /***
     * 公共常量 日期相关
     */
    public static class Strings {
        public static final String EMPTY = "";
        public static final char UNDERLINE = '_';
        public static final String ID_FIELD_NAME_DEFAULT = "id";
        public static final String ID_FIELD_NAME_DEFAULT_CODE = "code";
    }

    /***
     * 公共常量 日期相关
     */
    public static class DateTimes {
        public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    }

    /**
     * @author Administrator
     */
    @Getter
    @AllArgsConstructor
    public enum JoinTypeEnum {
        /***/
        left_join("left join "),
        inner_join("inner join "),
        right_join("right join "),
        ;
        private final String joinSqlSegment;
    }

    /**
     * 聚合操作
     *
     * @author Administrator
     */
    @Getter
    @AllArgsConstructor
    public enum MultiAggregateTypeEnum {
        /**
         * 聚合操作
         */
        PROPS("属性", "COUNT(%s)", c -> false),
        COUNT("计数", "COUNT(%s)", c -> false),
        COUNT_DISTINCT("计数去重", "COUNT(DISTINCT %s)", c -> !LocalDateTime.class.isAssignableFrom(c) && !Date.class.isAssignableFrom(c) && !BigDecimal.class.isAssignableFrom(c)&& !Boolean.class.isAssignableFrom(c)),
        SUM("求和", "IFNULL(SUM(%s), 0)", c -> Integer.class.isAssignableFrom(c) || BigDecimal.class.isAssignableFrom(c)),
        AVG("求平均值", "AVG(%s)", c -> Integer.class.isAssignableFrom(c) || BigDecimal.class.isAssignableFrom(c)),
        MAX("最大值", "MAX(%s)", c -> true),
        MIN("最小值", "MIN(%s)", c -> true),

        //select SId, group_concat(cId,cName),group_concat(score order by score desc separator '  ')   group_concat_max_len  如果没有group by 默认合成一条
        GROUP_CONCAT("分组组合拼接", "GROUP_CONCAT(%s)", c -> true),
//        JSON_ARRAYAGG("组装成JsonArray"),  //JSON_ARRAYAGG(col or expr) 　　将结果集聚合为单个JSON数组，其元素由参数列的值组成。此数组中元素的顺序未定义。该函数作用于计算为单个值的列或表达式。
//        JSON_OBJECTAGG("组装成JsonObject"), //JSON_OBJECTAGG(key,value)     两个列名或表达式作为参数，第一个用作键，第二个用作值，并返回包含键值对的JSON对象。
        ;
        private final String label;
        private final String sqlTemplate;
        /**
         * 聚合函数只适配的字段,对应java类型
         */
        public static final Map<String, MultiAggregateTypeEnum> MAP = Arrays.stream(values()).collect(Collectors.toMap(MultiAggregateTypeEnum::name, o -> o));
        private final Function<Class<?>, Boolean> fieldTypeFilter;
    }
}
