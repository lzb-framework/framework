package com.pro.framework.jdbc.config;

import com.pro.framework.jdbc.sqlexecutor.DbAdaptor;
import com.pro.framework.jdbc.sqlexecutor.DbJdbcAdaptor;
import com.pro.framework.jdbc.sqlexecutor.DbSpringAdaptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * DB数据库配置 支持spring的JdbcTemplate
 *
 * @author Administrator
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(JdbcTemplate.class)
public class ConfigJDBC {

    /***
     * sql执行实现
     * @return IMultiSqlExecutor
     */
    @Bean
    @ConditionalOnMissingBean(DbAdaptor.class)
    @ConditionalOnClass(JdbcTemplate.class)
    public DbAdaptor dbSpringAdaptor(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        return new DbSpringAdaptor(jdbcTemplate, dataSource);
    }
    /***
     * sql执行实现
     * @return IMultiSqlExecutor
     */
    @Bean
    @ConditionalOnMissingBean(DbAdaptor.class)
    public DbAdaptor dbJdbcAdaptor() {
        return new DbJdbcAdaptor();
    }
}
