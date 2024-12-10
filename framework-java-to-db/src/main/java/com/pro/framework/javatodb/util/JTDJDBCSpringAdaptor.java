package com.pro.framework.javatodb.util;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.util.List;
import java.util.function.Function;

/**
 * @author Administrator
 */
@Slf4j
@AllArgsConstructor
public class JTDJDBCSpringAdaptor implements JTDAdaptor {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public <T> List<T> executeQuery(String sql, Function<ResultSet, T> function) {
        log.trace("Multi 查询Sql:\n {}", sql);
        return jdbcTemplate.query(sql, (rs, i) -> function.apply(rs));
    }

    @Override
    public boolean execute(String sql) {
        if (JTDUtil.isBlank(sql)) {
            return false;
        }
        log.info("Multi 更新Sql:\n\n{}\n", sql);
        jdbcTemplate.execute(sql);
        return true;
    }
}
