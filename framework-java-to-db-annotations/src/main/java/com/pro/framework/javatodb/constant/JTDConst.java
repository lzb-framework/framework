package com.pro.framework.javatodb.constant;

import com.pro.framework.api.clazz.IConst;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class JTDConst implements IConst {
    public static final int INT__1 = -1;

    /**
     * 数据库类型
     */
    @Getter
    @AllArgsConstructor
    public enum EnumFieldType {
        /***/
        none(null),
        date("date"),
        time("time"),
        year("year"),
        datetime("datetime"),
        timestamp("timestamp"),

        char_("char"),
        varchar("varchar"),
        tinyblob("tinyblob"),
        tinytext("tinytext"),

        blob("blob"),
        text("text"),
        mediumblob("mediumblob"),
        mediumtext("mediumtext"),
        longblob("longblob"),
        longtext("longtext"),

        tinyint("tinyint"),

        smallint("smallint"),
        mediumint("mediumint"),
        int_("int"),
        integer("integer"),
        bigint("bigint"),
        float_("float"),
        double_("double"),
        decimal("decimal"),

        geometry("geometry"),
        json("json"),
        ;
        private final String value;
        public static final Map<String, EnumFieldType> MAP = Arrays.stream(values()).collect(Collectors.toMap(EnumFieldType::getValue, o -> o));
        // public static final Set<EnumFieldType> NUM_TYPE = new HashSet<>(Arrays.asList(tinyint, int_, bigint, float_, double_));
    }

    /**
     * 页面端类型
     */
    @AllArgsConstructor
    @Getter
    public enum EnumFieldUiType {
        hide("隐藏"),
        none("不指定按默认"),
        base("id_deleted等"),//一般不显示到页面上,无需手动指定
        relationCode("下拉选择其他配置表_无需手动指定"),// 实体和实体,通过code关联,直接生成zselect下拉类型
        date("日期"),
        select("下拉选择"),
        radio("单切选择"),
//        _switch("选择"), 与java语法关键字冲突,考虑用大写暂不使用
        time("时间"),
        datetime("日期时间"),
        text("文本"),
        textarea("多行文本"),
        number("数字"),
        bool("是否"),
        password("密码"),
        image("图片"),
        images("多图片"),
        richText("富文本"),
        file("文件"),
        strs("字符串数组"),
        json("json对象"),
        ;
        private String label;
    }

    /**
     * 页面端类型
     */
    @AllArgsConstructor
    @Getter
    public enum EnumFieldNullType {
        none(null),
        can_null("可以为空"),
        not_null("不能为空"),
        ;
        private String label;
    }

    /**
     * 字符集
     */
    @AllArgsConstructor
    @Getter
    public enum EnumCharset {
        none,
        utf8,
        utf8mb4,
        ;
        // private String label;
    }

    /**
     * 管理端对应按钮
     */
    @Getter
    @AllArgsConstructor
    public enum EnumAdminButton {
        query("查询"),
        add("添加"),
        edit("编辑"),
        copy("复制"),
        delete("删除"),
        export("导出"),
        ;
        private final String label;
    }

    /**
     * sql自动执行计划
     */
    @Getter
    @AllArgsConstructor
    public enum EnumSqlRunType {
        /***/
        none("关闭", Collections.emptySet()),
        createTable("新建整表", Collections.emptySet()),
        createTableAndMetas("新建整表_新建表内元素", new HashSet<>(Collections.singletonList(createTable))),
        createModifyTableAndMetas("新建整表_新建或修改表内元素", new HashSet<>(Arrays.asList(createTable, createTableAndMetas))),
        createModifyDeleteAll("新建整表_新建或修改表内元素_删除表内元素", new HashSet<>(Arrays.asList(createTable, createTableAndMetas, createModifyTableAndMetas))),
        ;
        private final String label;
        private final Set<EnumSqlRunType> containsMore;
    }
}
