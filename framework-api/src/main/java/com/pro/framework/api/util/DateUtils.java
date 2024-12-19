package com.pro.framework.api.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.pro.framework.api.FrameworkConst;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    public static LocalDateTime parseDateTime(String dateStr, boolean isStart) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        // 判断字符串是否包含时分秒
        if (dateStr.length() == 10) {  // 格式为 yyyy-MM-dd
            dateStr += (isStart ? " 00:00:00" : " 23:58:59");    // 拼接默认时间 00:00:00
        }

        // 定义日期时间格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 解析字符串为 LocalDateTime
        return LocalDateTime.parse(dateStr, formatter);
    }
}
