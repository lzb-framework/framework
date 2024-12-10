package com.pro.framework.jdbc.sqlexecutor;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * JDBC 基础查询实现 (各方面性能可能需要单独优化(连接池,业务代码易用性等),推荐使用 {@link DbSpringAdaptor})
 *
 * @author Administrator
 */
@Slf4j
@Deprecated
@NoArgsConstructor
@AllArgsConstructor
public class DbJdbcAdaptor implements DbAdaptor {

    public String driver = "com.mysql.jdbc.Driver";

    public String url = "jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=utf-8";

    public String user = "root";

    public String password = "root";

    @Override
    @SneakyThrows
    public void init() {
        DriverManager.registerDriver((Driver) Class.forName(driver).newInstance());
    }

    @Override
    @SneakyThrows
    public Connection getConnection() {
        return DriverManager.getConnection(url, user, password);
    }

    @Override
    public <T> List<T> select(String sql, Function<ResultSet, T> function) {
        log.trace("Multi 查询Sql:{}", sql);

        List<T> list = new ArrayList<>(2000);
        Connection conn = null;
        Statement stmt = null;
        try {
            log.info("STEP 2: Register JDBC driver");
//            Class<?> driverClass = Class.forName(driver);
//            Driver driver = (Driver) driverClass.newInstance();
            log.info("STEP 3:Open a connection");
            log.info("Connecting to database...");
            conn = getConnection();

            log.info("STEP 4: Execute a query");
            log.info("Creating statement...");
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            log.info("STEP 5: Extract data from result set");
            while (rs.next()) {
                list.add(function.apply(rs));
            }
            log.info("STEP 6: Clean-up environment");
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            log.info("Handle errors for JDBC");
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se2) {
                log.error("sql:" + sql + "\n", se2);
            }// nothing we can do
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }//end finally try
        }//end try
        return list;
    }
}
