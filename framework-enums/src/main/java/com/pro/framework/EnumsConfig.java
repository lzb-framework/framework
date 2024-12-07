package com.pro.framework;

import com.pro.framework.api.entity.IEntityProperties;
import com.pro.framework.jdbc.service.IBaseService;
import com.pro.framework.service.EnumsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Slf4j
@Configuration
@DependsOn("jTDServiceImpl")
@EnableConfigurationProperties(EnumProperties.class)
public class EnumsConfig {
    @Bean("enumsServiceImpl")
    public EnumsServiceImpl jtdService(EnumProperties enumProperties, IBaseService dbBaseService, IEntityProperties entityProperties) {
        EnumsServiceImpl service = new EnumsServiceImpl(dbBaseService,enumProperties, entityProperties);
        service.executeSql();
        return service;
    }
}
