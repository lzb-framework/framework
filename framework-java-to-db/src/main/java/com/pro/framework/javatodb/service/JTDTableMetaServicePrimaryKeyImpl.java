package com.pro.framework.javatodb.service;

import com.pro.framework.javatodb.constant.JTDConst;
import com.pro.framework.javatodb.model.JTDFieldInfoDb;
import com.pro.framework.javatodb.model.JTDSqlInfo;
import com.pro.framework.javatodb.model.JTDTableInfo;
import com.pro.framework.javatodb.util.JTDUtil;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

public class JTDTableMetaServicePrimaryKeyImpl implements JTDTableMetaService {
    public static final JTDTableMetaService INSTANCE = new JTDTableMetaServicePrimaryKeyImpl();

    @Override
    public Serializable getVersionSequence(JTDTableInfo t) {
        return null == t.getKeyFieldNames() ? null : t.getKeyFieldNames().stream().mapToInt(String::hashCode).sum();
    }

    @Override
    public List<JTDSqlInfo> calculateAlterTableSqls(JTDTableInfo told, JTDTableInfo tnew) {
        String tableName = tnew.getTableName();
        String content = "ALTER TABLE `" + tableName + "` ";
        List<String> subContents = new ArrayList<>();
        JTDConst.EnumSqlRunType runType = JTDConst.EnumSqlRunType.createTableAndMetas;
        // 主键不允许删除
        if (JTDUtil.notEmpty(told.getKeyFieldNames())) {
            Map<String, JTDFieldInfoDb> fieldMapNew = JTDUtil.listToMap(tnew.getFields(), JTDFieldInfoDb::getFieldName);
            for (String keyFieldName : told.getKeyFieldNames()) {
                JTDFieldInfoDb newField = fieldMapNew.get(keyFieldName);
                String sqlFieldHeader = "CHANGE COLUMN `{0}` {1}";
                subContents.add(MessageFormat.format(sqlFieldHeader, keyFieldName, newField.toSql(told.getTableName())));
            }
            subContents.add("DROP PRIMARY KEY");
            //noinspection ConstantConditions
//            content += (hasSql ? "," : "") + "DROP PRIMARY KEY ";
//            hasSql = true;
//            runType = EnumSqlRunType.createModifyTableAndMetas;
        }
        if (JTDUtil.notEmpty(tnew.getKeyFieldNames())) {
            subContents.add("ADD PRIMARY KEY ( " + tnew.getKeyFieldNames().stream().map(field -> "`" + field + "`").collect(Collectors.joining(",")) + " )");
        }
        if (subContents.isEmpty()) {
            return null;
        }
        return Collections.singletonList(new JTDSqlInfo(runType, content+ String.join(",\n", subContents)));
    }
}
