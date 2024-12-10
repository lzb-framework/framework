package com.pro.framework.javatodb.model;

import com.pro.framework.javatodb.constant.JTDConstInner;
import com.pro.framework.javatodb.util.JTDUtil;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author administrator
 * @date 2022-01-20
 */
@Data
@SuperBuilder
@ToString
public class JTDSequenceInfo implements ISqlSegmentInfo, Serializable {

    /**
     * 索引名称
     */
    private String name;

    /**
     * 索引类型
     */
    private JTDConstInner.EnumSequenceType type;

    /**
     * 索引名称字段 有序
     */
    private List<String> fieldNames;


    /**
     * sql配置项 转 sql
     *
     * @param tableName
     * @return sql 例如: UNIQUE KEY `uk_variable_valName` (`variable`,`val_name`) USING BTREE
     */
    @Override
    public String toSql(String tableName) {
        return Stream.of(
                type.getValue(),
                MessageFormat.format("`{0}`", name),
                MessageFormat.format("(`{0}`)", String.join("`,`", fieldNames))
        ).filter(JTDUtil::isNotBlank).collect(Collectors.joining(" "));
    }

    /**
     * sql 转 sql配置项
     *
     * @param sequenceConfigSql 例如:  UNIQUE KEY `uk_variable_valName` (`variable`,`val_name`) USING BTREE
     */
    public static JTDSequenceInfo init(String sequenceConfigSql) {
        if (JTDUtil.isBlank(sequenceConfigSql)) {
            return null;
        }
        JTDConstInner.EnumSequenceType type = JTDConstInner.EnumSequenceType.KEY;
        String subLine = sequenceConfigSql;
        if (subLine.trim().startsWith("UNIQUE")) {
            subLine = subLine.trim();
            subLine = subLine.substring(6);
            type = JTDConstInner.EnumSequenceType.UNIQUE_KEY;
        }
        if (subLine.trim().startsWith("KEY")) {
            subLine = subLine.trim();
            subLine = subLine.substring(3);
        }
        String name = subLine.trim().split(" ")[0].replaceAll("`", "");

        List<String> fieldNames = Arrays.stream(subLine.split("\\(")[1].split("\\)")[0].replaceAll("`", "").split(",")).collect(Collectors.toList());
        // sql 转 sql配置项
        return JTDSequenceInfo.builder()
                .name(name)
                .type(type)
                .fieldNames(fieldNames)
                .build();
    }
}
