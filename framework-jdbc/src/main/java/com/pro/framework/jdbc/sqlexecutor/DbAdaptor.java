package com.pro.framework.jdbc.sqlexecutor;

import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Administrator
 */
public interface DbAdaptor {
    void init();
    Connection getConnection();

    /**
     * 执行sql返回结果集
     *
     * @param sql      sql
     * @param function 返回结果组装方法
     * @return 结果集(Set.next ()一直遍历下去)
     */
    <T> List<T> select(String sql, Function<ResultSet, T> function);

    /**
     * 执行sql返回结果集
     *
     * @param sql sql
     * @return 结果集(Set.next ()一直遍历下去)
     */
    default Map<String, Object> selectFirstRow(String sql) {
        List<Map<String, Object>> list = this.select(sql, DbAdaptor::rsToMap);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * rsToMap
     *
     * @param rs rs
     * @return map
     */
    @SneakyThrows
    static Map<String, Object> rsToMap(ResultSet rs) {
        Map<String, Object> rowData = new HashMap<>(64);
        ResultSetMetaData md = rs.getMetaData();
        int columnCount = md.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            rowData.put(md.getColumnName(i), rs.getObject(i));
        }
        return rowData;
    }
}
