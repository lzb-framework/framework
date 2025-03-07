package com.pro.framework.mtq.service.multiwrapper.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pro.framework.api.entity.IEntityProperties;
import com.pro.framework.api.util.JSONUtils;
import com.pro.framework.mtq.service.multiwrapper.entity.IMultiClassRelationService;
import com.pro.framework.jdbc.sqlexecutor.DbAdaptor;
import com.pro.framework.mtq.service.multiwrapper.util.MultiClassRelationFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.Objects;

/**
 * 基础配置
 *
 * @author Administrator
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MultiProperties.class)
@Slf4j
public class BaseMultiConfig {
    public static MultiProperties multiProperties;
    public static DbAdaptor multiDbAdaptor;

    @Bean
    @DependsOn("objectMapper")
    public MultiClassRelationFactory multiClassRelationFactory(
            MultiProperties multiProperties,
            DbAdaptor dbAdaptor,
            ObjectProvider<IMultiClassRelationService> multiTableRelationServiceProvider,
            IEntityProperties entityProperties
    ) {
        if (false) {
            return null;
        }
//        if (!multiProperties.enabled) {
//            return null;
//        }

        BaseMultiConfig.multiProperties = multiProperties;
        BaseMultiConfig.multiDbAdaptor = dbAdaptor;
        log.info("加载multiClassRelationFactory");
        return new MultiClassRelationFactory(Objects.requireNonNull(multiTableRelationServiceProvider.getIfAvailable(), "请先实现 IMultiClassRelationService "),entityProperties);
    }
}
