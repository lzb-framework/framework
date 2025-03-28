package com.pro.framework.api;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

/**
 * 只能依赖jdk
 */
public class FrameworkConst {

    public static class Str {
        public static final String EMPTY = "";
        public static final String UNDERLINE = "_";
        public static final String COLON = ":";

        public static final String HEADER_LANGUAGE = "Accept-Language";
        public static final String DEFAULT = "default";

        public static final String[] MODEL_IGNORE_PROPERTIES = {"id", "createTime", "updateTime"};

        public static final String FALSE = "false";
        public static final String TRUE = "true";
        public static final String FALSE_TRANSLATE_KEY = "否";
        public static final String TRUE_TRANSLATE_KEY = "是";

        public static final String split_pound = "##";
        public static final String limit_1 = "limit 1";
        public static final String file_separator = "/";
    }

    public static class Num {
        public static final BigDecimal _10 = new BigDecimal("10");
        public static final BigDecimal _100 = new BigDecimal("100");
        public static final Integer FALSE = 0;
        public static final Integer TRUE = 1;
    }

    /***
     * 公共常量 日期相关
     */
    public static class DateTimes {
        public static final SimpleDateFormat DATE_TIME_FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    }

}
