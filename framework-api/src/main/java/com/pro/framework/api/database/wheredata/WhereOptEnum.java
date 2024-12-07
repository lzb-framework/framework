package com.pro.framework.api.database.wheredata;

import com.pro.framework.api.util.CollUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;

/**
 * @author Administrator
 */
@Getter
@AllArgsConstructor
public enum WhereOptEnum {
    /**
     * 查询where操作类型
     */
    eq("`%s` = '%s'", "#eq#", "等于", true),
//    eq("`%s` = '%s'", FrameworkConst.Str.EMPTY, "等于", true),
    ne("`%s` != '%s'", "#ne#", "不等于", true),
    isNull("`%s` is null", "#nu#", "为空", false),
    isNotNull("`%s` is not null", "#nn#", "不为空", false),
    in("`%s` in (%s)", "#in#", "在列表中", false),
    not_in("`%s` not in (%s)", "#ni#", "不在列表", false),
    gt("`%s` > '%s'", "#gt#", "大于", true),
    ge("`%s` >= '%s'", "#ge#", "大于等于", true),
    lt("`%s` < '%s'", "#lt#", "小于", true),
    le("`%s` <= '%s'", "#le#", "小于等于", true),
    likeAll("`%s` like '%%%s%%'", "#la#", "模糊匹配", true),
    likeLeft("`%s` like '%%%s'", "#ll#", "左模糊匹配", false),
    likeRight("`%s` like '%s%%'", "#lr#", "右模糊匹配", false),
    //%%转义为%   张三% 才能走,"索引
    ;

    private final String template;
    private final String paramMapOptPrefix;
    private final String label;
    private final Boolean usually;
    public static final Map<String, WhereOptEnum> PARAM_MAP_OPT_PREFIX_MAP = CollUtils.listToMap(Arrays.asList(WhereOptEnum.values()), WhereOptEnum::getParamMapOptPrefix);
}
