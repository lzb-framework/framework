package com.pro.framework.javatodb.config;

import com.pro.framework.javatodb.util.JTDAdaptor;
import com.pro.framework.javatodb.util.JTDJDBCSpringAdaptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(JdbcTemplate.class)
public class JTDJDBCSpringConfig {
    /***
     * sql执行实现
     * @return IMultiSqlExecutor
     */
    @Bean
    @ConditionalOnMissingBean(JTDAdaptor.class)
    public JTDAdaptor jtdAdaptorSpring(JdbcTemplate jdbcTemplate, DataSourceProperties dataSourceProperties) {
        return new JTDJDBCSpringAdaptor(jdbcTemplate, dataSourceProperties);
    }
}
