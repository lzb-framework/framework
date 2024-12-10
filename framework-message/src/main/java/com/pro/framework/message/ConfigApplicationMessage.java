package com.pro.framework.message;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigApplicationMessage {
    @Bean
    @ConditionalOnMissingBean
    IApplicationMessageSerializer<?> serializeService() {
        return new BaseApplicationMessageSerializer<>();
    }
}
