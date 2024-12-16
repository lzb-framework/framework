package com.pro.framework.javatodb.util;

import com.pro.framework.api.structure.Tuple2;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * JDBC 基础查询实现
 *
 * @author Administrator
 */
@Slf4j
//@AllArgsConstructor
public class JTDJDBCBaseAdaptor implements JTDAdaptor {

    private DataSourceProperties properties;

    @SneakyThrows
    public JTDJDBCBaseAdaptor(DataSourceProperties properties) {
        this.properties = properties;
        Class<?> driverClass = Class.forName(properties.getDriverClassName());
        Driver driver = (Driver) driverClass.newInstance();
        DriverManager.registerDriver(driver);
    }
//    public String driver = "com.mysql.jdbc.Driver";
//
//    public String url = "jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=utf-8";
//
//    public String user = "test";
//
//    public String password = "test";

    @SneakyThrows
    @Override
    public <T> List<T> executeQuery(String sql, Function<ResultSet, T> function) {
        log.trace("Multi 查询Sql:\n {}", sql);

        @Cleanup Connection conn = DriverManager.getConnection(properties.getUrl(), properties.getUsername(), properties.getPassword());
        @Cleanup Statement stmt = conn.createStatement();
        @Cleanup ResultSet rs = stmt.executeQuery(sql);
        if (function == null) {
            return null;
        }
        List<T> list = new ArrayList<>(2000);
        while (rs.next()) {
            list.add(function.apply(rs));
        }
        return list;
    }

    @SneakyThrows
    @Override
    public boolean execute(String sql) {
        if (JTDUtil.isBlank(sql)) {
            return false;
        }
        @Cleanup Connection conn = DriverManager.getConnection(properties.getUrl(), properties.getUsername(), properties.getPassword());
        @Cleanup Statement stmt = conn.createStatement();
        return stmt.execute(sql);
    }

    @Override
    @SneakyThrows
    public boolean createDatabase() {
        // 数据库连接信息（这里使用的是 MySQL）
        String url = properties.getUrl();
        Tuple2<String, String> tuple2 = JTDUtil.dbUrlExtractor(url);
        String dbName = tuple2.getT1();
        url = tuple2.getT2();
        String user = properties.getUsername();
        String password = properties.getPassword();

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
