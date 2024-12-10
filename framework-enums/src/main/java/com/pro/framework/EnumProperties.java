package com.pro.framework;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置文件
 *
 * @author Administrator
 */
@Data
@Component
@ConfigurationProperties(prefix = "enums")
public class EnumProperties {
    /**
     * 定制枚举类
     */
    private Map<String, Class<Enum>> enumClassReplaceMap = new ConcurrentHashMap<>();
}
