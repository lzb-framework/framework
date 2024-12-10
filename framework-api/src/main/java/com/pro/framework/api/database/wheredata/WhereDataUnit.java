package com.pro.framework.api.database.wheredata;

import com.pro.framework.api.FrameworkConst;
import com.pro.framework.api.database.ClientPreparedQueryBindings;
import com.pro.framework.api.database.IWhereData;
import com.pro.framework.api.enums.IEnum;
import com.pro.framework.api.util.OtherUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WhereDataUnit implements IWhereData {
    private String propName;
    private WhereOptEnum opt;
    private Object values;

    @Override
    public String getSqlWhereProps(String className) {
        return String.format(opt.getTemplate(), OtherUtil.getFieldNameFinal(className, propName), formatValues(values));
    }

    private static Object formatValues(Object value) {
        if (null == value) {
            return null;
        } else if (value instanceof IEnum) {
            value = ((IEnum) value).name();
        } else if (value instanceof Enum) {
            value = ((Enum<?>) value).name();
        } else if (value instanceof Date) {
            value = FrameworkConst.DateTimes.DATE_TIME_FORMAT_DATE.format((Date) value);
        } else if (value instanceof LocalDateTime) {
            value = FrameworkConst.DateTimes.DATE_TIME_FORMAT.format((LocalDateTime) value);
        } else if (value instanceof LocalDate) {
            value = FrameworkConst.DateTimes.DATE_FORMAT.format((LocalDate) value);
        } else if (value instanceof LocalTime) {
            value = FrameworkConst.DateTimes.TIME_FORMAT.format((LocalTime) value);
        } else if (value instanceof Boolean) {
            value = (Boolean) value ? 1 : 0;
        } else if (value instanceof String) {
            //防止SQL注入
            value = ClientPreparedQueryBindings.sanitizeInput((String) value);
            // TODO 待修改
            if ("true".equals(value)) {
                value = "1";
            }
            if ("false".equals(value)) {
                value = "0";
            }
        } else if (value instanceof Collection) {
            if (((Collection<?>) value).size() == 0) {
                return "1!=1";// where user_id in () 空数组 直接返回空结果
            }
            value = ((Collection<?>) value).stream().filter(Objects::nonNull).map(v -> '\'' + v.toString() + '\'').collect(Collectors.joining(","));
        } else if (value.getClass().isArray()) {
            if (((Object[]) value).length == 0) {
                return "1!=1";
            }
            value = Arrays.stream(((Object[]) value)).filter(Objects::nonNull).map(v -> '\'' + v.toString() + '\'').collect(Collectors.joining(","));
        }
        return value;
    }
}
