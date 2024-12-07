package com.pro.framework.javatodb.model;

import com.pro.framework.javatodb.constant.JTDConstInner;
import com.pro.framework.javatodb.util.JTDFieldInfoDbUtil;
import com.pro.framework.javatodb.util.JTDUtil;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.*;
import java.util.stream.Collectors;

/***
 *
 */
@Data
@SuperBuilder
@ToString
public class JTDTableInfo {
    /**
     * 实体名
     */
    private String entityName;
    /**
     * 表名
     */
    private String tableName;

    /**
     * 表名注释
     */
    private String label;

    /**
     * 主键信息
     */
    private List<String> keyFieldNames;
    /**
     * 排除属性
     */
    private Set<String> excludeFieldNames;
    /**
     * 字段信息
     */
    private List<JTDFieldInfoDb> fields;

    /**
     * 索引信息
     */
    private List<JTDSequenceInfo> sequences;


    /**
     * 模块(便于生成到指定目录下)
     */
    private String module;

    /**
     * 表唯一id(便于生成一些菜单id之类)
     */
    private Integer entityId;

    /**
     * 描述
     */
    private String description;

    public static JTDTableInfo init(String tableConfigSql) {
        String tableName = null;
        String label = null;
        List<String> keyFieldNames = Collections.emptyList();
        List<JTDFieldInfoDb> fields = new ArrayList<>();
        List<JTDSequenceInfo> sequences = new ArrayList<>();
        String[] lines = tableConfigSql.split("[\n\r\t]+");
        for (String line : lines) {
            line = line.trim();
            if (line.endsWith(",")) {
                //去掉结尾，号
                line = line.substring(0, line.length() - 1);
            }
            JTDConstInner.EnumTableMetaType itemType = JTDConstInner.EnumTableMetaType.getTypeByLine(line);
            switch (itemType) {
                case tableName:
                    tableName = line.split("CREATE TABLE `")[1].split("` \\(")[0];
                    break;
                case tableLabel:
                    String[] splitByComment = lines[lines.length - 1].substring(1).split(" COMMENT='");
                    label = splitByComment.length > 1 ? splitByComment[1].split("'")[0] : null;
                    break;
                case fields:
                    fields.add(JTDFieldInfoDbUtil.init(line, null, null));
                    break;
                case primaryKey:
                    keyFieldNames = Arrays.stream(line.split("\\(")[1].split("\\)")[0].split(",")).map(s -> s.substring(1, s.length() - 1)).collect(Collectors.toList());
                    break;
                case sequenceInfos:
                    sequences.add(JTDSequenceInfo.init(line));
                    break;
                default:
            }
        }
        // sql 转 sql配置项
        return JTDTableInfo.builder()
                .tableName(tableName)
                .label(label)
                .keyFieldNames(keyFieldNames)
                .fields(fields)
                .sequences(sequences)
                .build();
    }

    @SneakyThrows
    private static JTDConstInner.EnumTableMetaType getItemTypeByLine(String line) {
        if (line.startsWith("CREATE TABLE")) {
            return JTDConstInner.EnumTableMetaType.tableName;
        }
        if (line.startsWith("PRIMARY KEY")) {
            return JTDConstInner.EnumTableMetaType.primaryKey;
        }
        if (line.startsWith("UNIQUE") || line.startsWith("KEY")) {
            return JTDConstInner.EnumTableMetaType.sequenceInfos;
        }
        if (line.startsWith(")")) {
            return JTDConstInner.EnumTableMetaType.tableLabel;
        }
        if (line.startsWith("`")) {
            return JTDConstInner.EnumTableMetaType.fields;
        }
//        throw new JTDException("未知的SQL行");
        return JTDConstInner.EnumTableMetaType.unknown;
    }


    /**
     * sql配置项 转 sql
     *
     * @return 例如
     * CREATE TABLE `sys_config` (
     * `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '1',
     * `variable` varchar(128) NOT NULL COMMENT '2',
     * `value` varchar(128) DEFAULT '1' COMMENT '3',
     * `set_time` timestamp NULL DEFAULT NULL COMMENT '4',
     * `set_by` tinytext COMMENT '5',
     * PRIMARY KEY (`id`)
     * ) COMMENT='111';
     */
    public String toCreateTableSql() {
        List<String> sqlInners = new ArrayList<>();
        fields.forEach(f -> sqlInners.add(f.toSql(tableName)));
        sqlInners.add(JTDUtil.format(" PRIMARY KEY (`{0}`)", String.join("`,`", keyFieldNames.size() == 0 ? Collections.singleton("id") : keyFieldNames)));
        sequences.forEach((sequenceInfo) -> sqlInners.add(" " + sequenceInfo.toSql(tableName)));

        List<String> sqls = new ArrayList<>();
        sqls.add(JTDUtil.format("CREATE TABLE `{0}` (", tableName));
        sqls.add(sqlInners.stream().filter(JTDUtil::isNotBlank).collect(Collectors.joining(",\n")));
        sqls.add(")" + JTDUtil.format(" COMMENT=''{0}''", label));
        return sqls.stream().filter(JTDUtil::isNotBlank).collect(Collectors.joining("\n"));
    }

    /**
     * 为了判断是否有更变过
     */
    @Override
    public int hashCode() {
        return Objects.hash(tableName, label, keyFieldNames.stream().mapToInt(String::hashCode).sum(), fields.stream().mapToInt(JTDFieldInfoDb::hashCode).sum());
    }
}
