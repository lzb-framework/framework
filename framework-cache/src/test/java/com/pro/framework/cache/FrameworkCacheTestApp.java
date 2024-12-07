package com.pro.framework.cache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@ComponentScan("com.pro")
public class FrameworkCacheTestApp {

    public static void main(String[] args) {
        SpringApplication.run(FrameworkCacheTestApp.class, args);
    }
}
