package com.pro.framework.javatodb.model;

import com.pro.framework.javatodb.constant.JTDConst;
import com.pro.framework.javatodb.enums.EnumPositionAlign;
import com.pro.framework.javatodb.enums.EnumUIArea;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 表信息对象
 * <p>
 * // const tableConfigDefault = {
 * //   tableConfig: {
 * //     base: {
 * //       ...commonProps,
 * //       fieldNames: [],
 * //     },
 * //     search: {},
 * //     tableColumn: {
 * //       fieldName: null,
 * //       label: null,
 * //       uiType: null,
 * //     },
 * //     form: {},
 * //     table: {},
 * //   },
 * //   fieldConfigsMap: {
 * //     id: {
 * //       base: {
 * //         ...commonProps
 * //       },
 * //       search: {},
 * //       tableColumn: {},
 * //       form: {},
 * //     }
 * //   }
 * // }
 */
@Data
@ApiModel(description = "表信息对象")
public class UITableInfo {
    /**
     * 整表性配置信息
     */
    private Map<EnumUIArea, TableConfigOne> tableConfigs;
    /**
     * 字段性配置信息
     */
    private Map<String, Map<EnumUIArea, FieldConfigOne>> fieldConfigsMap = new HashMap<>(32);

    // 页面区域
    public static final List<String> uiAreasTable = Arrays.asList(
            "base",
            "search",
//            "tableColumn",
            "form",
            "table"
    );
    // 页面区域
    public static final List<String> uiAreasField = Arrays.asList(
            "base",
            "search",
            "tableColumn",
            "form"
//            "table"
    );


    /**
     * 整表性配置信息 源自 {@link JTDTableInfo}
     */
    @Data
    public static class TableConfigOne implements Serializable {
        private Boolean hide;
        private Boolean isEdit;
        private List<String> fieldNames;
        private String tableName;
        private String label;
        private EnumPositionAlign labelPosition;
        private List<String> keyFieldNames;
//        private List<JTDSequenceInfo> sequences;
        private String module;
        private Integer entityId;

        // 增上改查 接口路径
        private String getPageUrl;
        private String getOneUrl;
        private String insertUrl;
        private String updateUrl;
        private String deleteUrl;
        private String exportUrl;

        /**
         * 管理端默认功能按钮
         */
        private JTDConst.EnumAdminButton[] adminButtons;
//        /**
//         * 删除显示属性(不显示)
//         */
//        private List<String> removeFieldNames;
//        /**
//         * 额外显示属性
//         */
//        private List<String> addFieldNames;

    }

    /**
     * 整表性配置信息
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class FieldConfigOne extends JTDFieldInfoDbVo  {
        private Boolean hide;
        private Boolean isEdit;
        private String javaTypeEnumClassName;
        private Boolean javaTypeEnumClassMultiple;
        /**
         * 默认值
         */
        private Object defaultValueObj;
    }

//    public static void main(String[] args) {
//        JTDFieldInfoDb jtdFieldInfoDb = new JTDFieldInfoDb();
//        jtdFieldInfoDb.setSortable(true);
//        FieldConfigOne one = new FieldConfigOne();
//        BeanUtils.copyProperties(jtdFieldInfoDb,one);
//        System.out.println(one.getSortable());
//    }
}
