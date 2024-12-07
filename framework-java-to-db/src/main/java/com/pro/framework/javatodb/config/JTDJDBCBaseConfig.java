package com.pro.framework.javatodb.config;

import com.pro.framework.javatodb.util.JTDAdaptor;
import com.pro.framework.javatodb.util.JTDJDBCBaseAdaptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class JTDJDBCBaseConfig {
    /***
     * sql执行实现
     * @return IMultiSqlExecutor
     */
    @Bean
    @ConditionalOnMissingBean(JTDAdaptor.class)
    public JTDAdaptor jtdAdaptorBase(DataSourceProperties dataSourceProperties) {
        return new JTDJDBCBaseAdaptor(dataSourceProperties);
    }
}
