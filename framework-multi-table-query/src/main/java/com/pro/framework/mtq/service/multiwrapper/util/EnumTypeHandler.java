package com.pro.framework.mtq.service.multiwrapper.util;

import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 重写mybatis枚举处理器
 */
@SuppressWarnings("unused")
@Slf4j
public class EnumTypeHandler<E extends Enum<E>> extends BaseTypeHandler<E> {

    private final Class<E> type;

    public EnumTypeHandler(Class<E> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
        String name = parameter.name();
        if (parameter instanceof IEnum) {
            Serializable value = ((IEnum) parameter).getValue();
            name = String.valueOf(value);
        }
        if (jdbcType == null) {
            ps.setString(i, name);
        } else {
            ps.setObject(i, name, jdbcType.TYPE_CODE); // see r3589
        }
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String s = rs.getString(columnName);
        return s == null ? null : valueOf(type, s);
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String s = rs.getString(columnIndex);
        return s == null ? null : valueOf(type, s);
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String s = cs.getString(columnIndex);
        return s == null ? null : valueOf(type, s);
    }


    /**
     * 值映射为枚举
     */
    private static <E extends Enum<E>> E valueOf(Class<E> enumClass, String value) {
        if (null == value || value.isEmpty()) {
            return null;
        }
//        if (IEnum.class.isAssignableFrom(enumClass)) {
//            List<E> enumList = EnumUtil.enumList(enumClass);
//            return enumList.stream().filter(e -> {
//                IEnum iEnum = (IEnum) e;
//                return String.valueOf(value).equals(String.valueOf(iEnum.getValue()));
//            }).findAny().orElse(null);
//        } else {
//            try {
//                return Enum.valueOf(enumClass, value);
//            } catch (final IllegalArgumentException ex) {
//                log.warn("该枚举值不存在|enumClass={}|值={}", enumClass, value);
//            }
//        }
        return Enum.valueOf(enumClass, value);
    }
}
