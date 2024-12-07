package com.pro.framework.javatodb.service;

import com.pro.framework.javatodb.constant.JTDConst;
import com.pro.framework.javatodb.model.JTDSqlInfo;
import com.pro.framework.javatodb.model.JTDTableInfo;
import com.pro.framework.javatodb.util.JTDUtil;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

public class JTDTableMetaServiceLabelImpl implements JTDTableMetaService {
    public static final JTDTableMetaService INSTANCE = new JTDTableMetaServiceLabelImpl();

    @Override
    public Serializable getVersionSequence(JTDTableInfo table) {
        return JTDUtil.or(table.getLabel(), "");
    }

    @Override
    public List<JTDSqlInfo> calculateAlterTableSqls(JTDTableInfo told, JTDTableInfo tnew) {
        String sql = MessageFormat.format("ALTER TABLE `{0}` COMMENT ''{1}''", tnew.getTableName(), tnew.getLabel());
        JTDConst.EnumSqlRunType runType = null == told.getLabel() ? JTDConst.EnumSqlRunType.createTableAndMetas : JTDConst.EnumSqlRunType.createModifyTableAndMetas;
        return Collections.singletonList(new JTDSqlInfo(runType, sql));
    }
}
