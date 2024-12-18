package com.pro.framework.javatodb.service;

import com.pro.framework.api.util.JSONUtils;
import com.pro.framework.javatodb.constant.JTDConst;
import com.pro.framework.javatodb.model.JTDFieldInfoDb;
import com.pro.framework.javatodb.model.JTDSqlInfo;
import com.pro.framework.javatodb.model.JTDTableInfo;
import com.pro.framework.javatodb.util.JTDAssertUtil;
import com.pro.framework.javatodb.util.JTDUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class JTDTableMetaServiceFieldImpl implements JTDTableMetaService {
    public static final JTDTableMetaService INSTANCE = new JTDTableMetaServiceFieldImpl();

    @Override
    public Serializable getVersionSequence(JTDTableInfo table) {
        return null == table.getFields() ? null : table.getFields().stream().mapToInt(JTDFieldInfoDb::hashCode).sum();
    }

    @Override
    public List<JTDSqlInfo> calculateAlterTableSqls(JTDTableInfo told, JTDTableInfo tnew) {
        Map<String, JTDFieldInfoDb> oldFieldMap = JTDUtil.listToMap(told.getFields(), JTDFieldInfoDb::getFieldName);
        List<String> renameFromFields = told.getFields().stream().map(JTDFieldInfoDb::getRenameFrom).filter(JTDUtil::isNotBlank).collect(Collectors.toList());
        String tableName = tnew.getTableName();
        JTDAssertUtil.true_(renameFromFields.size() == new HashSet<>(renameFromFields).size(), "代码异常,{0}存在重复的renameFrom", JTDUtil.firstToUpperCase(JTDUtil.underlineToCamel(tableName)));
        List<JTDSqlInfo> createOrModifyField = tnew.getFields().stream().map(newField -> {
            String fieldName = newField.getFieldName();
            String renameFrom = newField.getRenameFrom();
            JTDFieldInfoDb oldField = oldFieldMap.get(fieldName);
            String oldFieldName = fieldName;
            JTDFieldInfoDb renameFromField = oldFieldMap.get(renameFrom);
            //renameFrom存在,并且旧字段存在,并且新字段不存在，才是rename，否则是普通更新
            JTDFieldInfoDb newFieldExist = JTDUtil.isNotBlank(renameFrom) ? oldFieldMap.get(fieldName) : null;
            boolean isRename = JTDUtil.isNotBlank(renameFrom) && renameFromField != null && null == newFieldExist;
            if (isRename) {
                oldFieldName = renameFrom;
                oldField = renameFromField;
            }
            newField.setRenameFrom(null);
            // 没有指定chatset就不用判断charset是否一致
            if (null == newField.getCharset() && null != oldField) {
                oldField.setCharset(null);
            }
            // newField.setJavaTypeEnumClass(null);
            if (isRename || !newField.equals(oldField)) {
                if (!isRename) {
                    log.info("属性差异 {}\noldField= {} \n newField={} ", tableName, JSONUtils.toString(oldField), JSONUtils.toString(newField));
                }
                if (JTDUtil.isNotBlank(renameFrom) && renameFromField == null) {
                    log.error("{}表设置从旧字段{}更名为{},旧字段{}已不存在,只执行新增/修改新字段本身", tableName, fieldName, renameFrom, renameFrom);
                }
                if (JTDUtil.isNotBlank(renameFrom) && newFieldExist != null) {
                    log.error("{}表设置从旧字段{}更名为{},新字段{}已存在,只执行修改新字段本身", tableName, fieldName, renameFrom, fieldName);
                }
                boolean isAdd = oldField == null;
                String sqlFieldHeader = isAdd ? "ALTER TABLE `{0}` ADD COLUMN {2}" : "ALTER TABLE `{0}` CHANGE COLUMN `{1}` {2}";
                String content = MessageFormat.format(sqlFieldHeader, tableName, oldFieldName, newField.toSql(told.getTableName()));
                JTDConst.EnumSqlRunType runType = isAdd ? JTDConst.EnumSqlRunType.createTableAndMetas : JTDConst.EnumSqlRunType.createModifyTableAndMetas;
                String defaultValue = newField.getDefaultValue();
                // 修改 并且 not null
                if (!isAdd && JTDConst.EnumFieldNullType.not_null.equals(newField.getNotNull()) && JTDUtil.isNotBlank(defaultValue)) {
                    String updateNullSql = MessageFormat.format("UPDATE `{0}` set `{1}` = {2},update_time =now() where `{3}` is null; ", tableName, oldFieldName, defaultValue, oldFieldName);
                    content = updateNullSql + content;
                }
                return new JTDSqlInfo(runType, content);
            } else {
                if (JTDUtil.isNotBlank(renameFrom)) {
                    //需确认已上线过，再清理（注意多环境环混乱）
                    log.info("本环境,旧字段已不存在,可确定清楚后,清理 {} 中的, renameFrom = \"{}\" 信息.", JTDUtil.firstToUpperCase(JTDUtil.underlineToCamel(tableName)), renameFrom);
                }
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
        List<String> newFieldNames = tnew.getFields().stream().map(JTDFieldInfoDb::getFieldName).collect(Collectors.toList());
        List<JTDSqlInfo> deleteField = told.getFields().stream()
                .map(oldField -> JTDUtil.or(oldField.getRenameFrom(), oldField.getFieldName()))
                .filter(oldFieldName -> !newFieldNames.contains(oldFieldName))
                .distinct().map(oldFieldName -> {
                    String sqlFieldHeader = "ALTER TABLE `{0}` DROP COLUMN `{1}`";
                    String content = MessageFormat.format(sqlFieldHeader, tableName, oldFieldName);
                    JTDConst.EnumSqlRunType runType = JTDConst.EnumSqlRunType.createModifyDeleteAll;
                    return new JTDSqlInfo(runType, content);
                }).collect(Collectors.toList());
        List<JTDSqlInfo> list = new ArrayList<>();
        list.addAll(createOrModifyField);
        list.addAll(deleteField);
        return list;

    }

//    public static void main(String[] args) {
//        String result = MessageFormat.format("''{0}''", "");
//        System.out.println(result+"123");
//    }
}
