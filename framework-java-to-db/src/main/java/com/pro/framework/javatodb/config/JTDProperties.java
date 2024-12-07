package com.pro.framework.javatodb.config;

import com.pro.framework.javatodb.constant.JTDConst;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置文件
 *
 * @author Administrator
 */
@Data
@ConfigurationProperties(prefix = "jtd")
public class JTDProperties {

    /**
     * 环境启动时自动执行一次
     */
    private Boolean runOnStartUp = false;
    /**
     * sql自动执行计划
     * none-关闭 createTable-仅新建表 createTableAndMetas-新建表和表内元素 createModifyTableAndMetas-新建和修改表和表内元素
     */
    private JTDConst.EnumSqlRunType runType = JTDConst.EnumSqlRunType.none;
    private String[] basePackages = new String[]{};
    /**
     * key: fieldPattern
     * value: defaultValueString
     */
    private Map<String,String> fieldPatternNotNullDefaultValueMap = new HashMap<>();

    public static Map<String,String> FIELD_PATTERN_NOT_NULL_DEFAULT_VALUE_MAP_DEFAULT;
    static {
        FIELD_PATTERN_NOT_NULL_DEFAULT_VALUE_MAP_DEFAULT = new HashMap<>();
        FIELD_PATTERN_NOT_NULL_DEFAULT_VALUE_MAP_DEFAULT.put(".*_id","");
        FIELD_PATTERN_NOT_NULL_DEFAULT_VALUE_MAP_DEFAULT.put("enabled","1");
        FIELD_PATTERN_NOT_NULL_DEFAULT_VALUE_MAP_DEFAULT.put("deleted","o");
        FIELD_PATTERN_NOT_NULL_DEFAULT_VALUE_MAP_DEFAULT.put("create_time","CURRENT_TIMESTAMP");
        FIELD_PATTERN_NOT_NULL_DEFAULT_VALUE_MAP_DEFAULT.put("update_time","CURRENT_TIMESTAMP");
        // FIELD_PATTERN_NOT_NULL_DEFAULT_VALUE_MAP_DEFAULT = Collections.unmodifiableMap(FIELD_PATTERN_NOT_NULL_DEFAULT_VALUE_MAP_DEFAULT);
    }
}
