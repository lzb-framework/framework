package com.pro.framework.mybatisplus;

import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 启用事务及mybatis扫描
 */
@MapperScan("com.pro.**.dao")
@Configuration
@EnableCaching
//        (proxyTargetClass = true)
//@EnableAspectJAutoProxy
@EnableTransactionManagement
public class ConfigDatabase {

    /**
     * MyBatis-Plus 文档 https://baomidou.com/
     * 分页插件
     */
    @Bean
    public PaginationInnerInterceptor paginationInterceptor() {
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor();
        paginationInterceptor.setMaxLimit(-1L);
        return paginationInterceptor;
    }
}
