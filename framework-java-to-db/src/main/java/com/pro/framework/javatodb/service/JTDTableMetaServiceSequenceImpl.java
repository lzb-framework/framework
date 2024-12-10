package com.pro.framework.javatodb.service;

import com.pro.framework.javatodb.constant.JTDConst;
import com.pro.framework.javatodb.model.JTDSequenceInfo;
import com.pro.framework.javatodb.model.JTDSqlInfo;
import com.pro.framework.javatodb.model.JTDTableInfo;
import com.pro.framework.javatodb.util.JTDUtil;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class JTDTableMetaServiceSequenceImpl implements JTDTableMetaService {
    public static final JTDTableMetaService INSTANCE = new JTDTableMetaServiceSequenceImpl();

    @Override
    public Serializable getVersionSequence(JTDTableInfo table) {
        return null == table.getSequences() ? null : table.getSequences().stream().mapToInt(JTDSequenceInfo::hashCode).sum();
    }

    @Override
    public List<JTDSqlInfo> calculateAlterTableSqls(JTDTableInfo told, JTDTableInfo tnew) {
        Map<String, JTDSequenceInfo> sequenceInfoMap = JTDUtil.listToMap(told.getSequences(), JTDSequenceInfo::getName);
        String tableName = tnew.getTableName();
        List<JTDSqlInfo> addOrModify = tnew.getSequences().stream().map(newSeq -> {
            String sequenceName = newSeq.getName();
//                        String info = newSeq.getValue();
            boolean isOldExist = sequenceInfoMap.containsKey(sequenceName);
            if (isOldExist && newSeq.equals(sequenceInfoMap.get(sequenceName))) {
                return null;
            }

            String dropSql = MessageFormat.format("DROP INDEX `{0}` ON `{1}`;\n", sequenceName, tableName);
            String addSql = MessageFormat.format("ALTER TABLE `{0}` ADD {1}", tableName, newSeq.toSql(tableName));
            String content = (isOldExist ? dropSql : "") + addSql;
            JTDConst.EnumSqlRunType runType = isOldExist ? JTDConst.EnumSqlRunType.createModifyTableAndMetas : JTDConst.EnumSqlRunType.createTableAndMetas;
            return new JTDSqlInfo(runType, content);
        }).filter(Objects::nonNull).collect(Collectors.toList());

        List<String> newNames = tnew.getSequences().stream().map(JTDSequenceInfo::getName).collect(Collectors.toList());
        List<JTDSqlInfo> delete = told.getSequences().stream()
                .map(JTDSequenceInfo::getName)
                .filter(oldName -> !newNames.contains(oldName))
                .distinct().map(oldName -> {
                    String sqlHeader = "ALTER TABLE `{0}` DROP INDEX `{1}`";
                    String content = MessageFormat.format(sqlHeader, tableName, oldName);
                    JTDConst.EnumSqlRunType runType = JTDConst.EnumSqlRunType.createModifyDeleteAll;
                    return new JTDSqlInfo(runType, content);
                }).collect(Collectors.toList());
        List<JTDSqlInfo> list = new ArrayList<>();
        list.addAll(addOrModify);
        list.addAll(delete);
        return list;
    }
}
