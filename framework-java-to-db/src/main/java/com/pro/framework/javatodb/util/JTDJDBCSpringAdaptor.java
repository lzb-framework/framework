package com.pro.framework.javatodb.util;

import com.pro.framework.api.structure.Tuple2;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Administrator
 */
@Slf4j
@AllArgsConstructor
public class JTDJDBCSpringAdaptor implements JTDAdaptor {

    private final JdbcTemplate jdbcTemplate;
    private final DataSourceProperties dataSourceProperties;

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
    @Override
    @SneakyThrows
    public boolean createDatabase() {
        // 数据库连接信息（这里使用的是 MySQL）
        String url = dataSourceProperties.getUrl();
        Tuple2<String, String> tuple2 = JTDUtil.dbUrlExtractor(url);
        String dbName = tuple2.getT1();
        url = tuple2.getT2();
        String user = dataSourceProperties.getUsername();
        String password = dataSourceProperties.getPassword();

        // 创建数据库的 SQL 语句
        String createDatabaseSQL = "CREATE DATABASE IF NOT EXISTS " + dbName;

        @Cleanup Connection connection = DriverManager.getConnection(url, user, password);
        @Cleanup Statement statement = connection.createStatement();
        // 执行创建数据库的 SQL 语句
        statement.executeUpdate(createDatabaseSQL);
        System.out.println("Database created successfully or already exists.");
        return false;
    }



    public static String removeDatabaseNameFromUrl(String url) {
        // 使用正则表达式只去掉数据库名称部分（即 /snowball2）
        return url.replaceFirst("(/[^/?]+)(?=\\?)", "");
    }
}
