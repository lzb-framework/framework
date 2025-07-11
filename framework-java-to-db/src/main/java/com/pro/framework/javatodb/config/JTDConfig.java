package com.pro.framework.javatodb.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pro.framework.api.entity.IEntityProperties;
import com.pro.framework.api.util.JSONUtils;
import com.pro.framework.javatodb.service.JTDServiceImpl;
import com.pro.framework.javatodb.util.JTDAdaptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;

@Slf4j
@Configuration("jtdConfig")
@EnableConfigurationProperties(JTDProperties.class)
//@ConditionalOnProperty(name = "jtd.runOnStartUp", havingValue = "true")
public class JTDConfig {

    @Bean("jTDServiceImpl")
    @DependsOn("objectMapper")
    public JTDServiceImpl jtdService(JTDProperties jtdProperties, JTDAdaptor adaptor, IEntityProperties entityProperties) {
        JTDServiceImpl service = new JTDServiceImpl(jtdProperties, adaptor, entityProperties);
        if (jtdProperties.getRunOnStartUp()) {
            try {
                //执行sql
                service.executeSql();
            } catch (Exception e) {
                log.error("error", e);
                e.printStackTrace();
            }
        }
        return service;
    }
}
