package com.pro.framework.javatodb.model;

import com.pro.framework.javatodb.constant.JTDConst;
import lombok.Data;

import java.util.List;
import java.util.Set;

/***
 *
 */
@Data
public class JTDTableInfoVo {
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
    private List<JTDFieldInfoDbVo> fields;

//    /**
//     * 索引信息
//     */
//    private List<JTDSequenceInfo> sequences;


    /**
     * 模块(便于生成到指定目录下)
     */
    private String module;

    /**
     * 表唯一id(便于生成一些菜单id之类)
     */
    private Integer entityId;

    /**
     * 管理端默认功能按钮
     */
    private JTDConst.EnumAdminButton[] adminButtons;
}
