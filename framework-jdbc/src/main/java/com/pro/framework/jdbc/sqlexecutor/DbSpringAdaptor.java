package com.pro.framework.jdbc.sqlexecutor;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import java.util.function.Function;

/**
 * @author Administrator
 */
@Slf4j
public class DbSpringAdaptor implements DbAdaptor {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public DbSpringAdaptor(JdbcTemplate jdbcTemplate
            , DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void init() {

    }
    @Override
    @SneakyThrows
    public Connection getConnection() {
        return dataSource.getConnection();
    }

    @Override
    public <T> List<T> select(String sql, Function<ResultSet, T> function) {
        log.trace("Multi 查询Sql:\n {}", sql);
        return jdbcTemplate.query(sql, (rs, i) -> function.apply(rs));
    }

//    @Override
//    public Map<String, Object> selectFirstRow(String sql) {
//        log.trace("Multi 查询Sql:\n {}", sql);
//        return jdbcTemplate.query(sql, new Object[]{}, new BeanPropertyRowMapper<>(HashMap.class))
//                        .stream().findFirst().orElse(null);
////        return jdbcTemplate.queryForMap(sql);
//    }
//    @Override
//    public Map<String, Object> selectFirstRow(String sql) {
//        log.trace("Multi 查询Sql:\n {}", sql);
//        return jdbcTemplate.queryForMap(sql);
//    }
//    public Map<String, Object> queryForMapOrNull(String sql, Object... args) {
//            try {
//                return jdbcTemplate.queryForObject(sql, args, (resultSet, rowNum) -> {
//                    int columnCount = resultSet.getMetaData().getColumnCount();
//                    Map<String, Object> map = new HashMap<>(columnCount);
//                    for (int i = 1; i <= columnCount; i++) {
//                        String columnName = resultSet.getMetaData().getColumnName(i);
//                        Object value = resultSet.getObject(i);
//                        map.put(columnName, value);
//                    }
//                    return map;
//                });
//            } catch (EmptyResultDataAccessException ex) {
//                // 如果结果为空，捕获 EmptyResultDataAccessException 异常，返回 null
//                return null;
//            }
//        }
}
