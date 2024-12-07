package com.pro.framework.mtq.service.multiwrapper.wrapper.inner;

import com.pro.framework.api.database.wheredata.WhereDataUnit;
import com.pro.framework.api.database.wheredata.WhereOptEnum;
import com.pro.framework.api.util.JSONUtils;
import com.pro.framework.mtq.service.multiwrapper.constant.MultiConstant;
import com.pro.framework.mtq.service.multiwrapper.entity.IMultiClassRelation;
import com.pro.framework.mtq.service.multiwrapper.sqlsegment.MultiWrapperAggregate;
import com.pro.framework.mtq.service.multiwrapper.sqlsegment.MultiWrapperSelect;
import com.pro.framework.mtq.service.multiwrapper.util.*;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 多表联查器
 * 举例说明 主表:user_staff 副表:user_staff_address
 *
 * @param <MAIN, ?>
 * @author Administrator
 */
@NoArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class MultiWrapperInner<MAIN> {

    /**
     * 主表信息
     */
    public MultiWrapperMainInner<MAIN, ?> wrapperMain;

    /**
     * 副表信息
     */
    public List<MultiWrapperSubAndRelation<?>> wrapperSubAndRelations = new ArrayList<>(8);
    public List<MultiWrapperSubAndRelation<?>> wrapperSubAndRelationSort = new ArrayList<>(8);


    /**
     * 计算SQL时,初始化表关系树
     */
    public MultiTreeNode<IMultiWrapperSubAndRelationTreeNode> relationTree;
    List<IMultiWrapperSubAndRelationTreeNode> relationsAndMain;

    public Map<String, ?> extendParams = Collections.emptyMap();
    public List<String> groupBy = Collections.emptyList();
    /**
     * 参数Map 例如
     * {
     * "userAndUserStaff.balance":"#gt#100", //其中userAndUserStaff是relationCode
     * "userStaff.sex":"#eq#1"
     * }
     */
    public Map<String, List<WhereDataUnit>> extendParamMap = Collections.emptyMap();
    public Boolean loaded = false;


    public MultiWrapperInner(MultiWrapperMainInner<MAIN, ?> wrapperMain) {
        this.wrapperMain = wrapperMain;
    }

    public MultiWrapperInner(MultiWrapperMainInner<MAIN, ?> wrapperMain, MultiWrapperSubInner<?, ?>... subTableWrappers) {
        this.wrapperMain = wrapperMain;
        //默认leftJoin
        Arrays.stream(subTableWrappers).forEach(this::leftJoin);
    }

    public MultiWrapperInner(MultiWrapperMainInner<MAIN, ?> wrapperMain, Class<?>... subTableClasses) {
        this.wrapperMain = wrapperMain;
        Arrays.stream(subTableClasses).forEach(subTableClass -> this.leftJoin(new MultiWrapperSubInner<>(subTableClass)));
    }

    /**
     * 主表信息
     * 例如 select * from user_staff
     *
     * @return MultiWrapper
     */
    public static <MAIN> MultiWrapperInner<MAIN> main(MultiWrapperMainInner<MAIN, ?> wrapperMain) {
        MultiWrapperInner<MAIN> wrapper = new MultiWrapperInner<>();
        wrapper.wrapperMain = wrapperMain;
        return wrapper;
    }

    /***
     * join 副表信息
     *
     * @param subTableWrapper subTableWrapper
     * @return MultiWrapper
     */
    public MultiWrapperInner<MAIN> leftJoin(MultiWrapperSubInner<?, ?> subTableWrapper) {
        return leftJoin(null, subTableWrapper);
    }

    /***
     * join是有顺序的,前后两张表,必须有直接关联
     *
     * @param subTableWrapper subTableWrapper
     * @return MultiWrapper
     */
    public MultiWrapperInner<MAIN> innerJoin(MultiWrapperSubInner<?, ?> subTableWrapper) {
        return innerJoin(null, subTableWrapper);
    }

    /***
     * join是有顺序的,前后两张表,必须有直接关联
     *
     * @param relationCode      {@link IMultiClassRelation#getCode()}
     * @param subTableWrapper 副表的select和 on内条件信息
     * @return MultiWrapper
     */
    public MultiWrapperInner<MAIN> leftJoin(String relationCode, MultiWrapperSubInner<?, ?> subTableWrapper) {
        MultiConstant.JoinTypeEnum joinType = MultiConstant.JoinTypeEnum.left_join;
        return this.getMainMultiWrapper(joinType, relationCode, subTableWrapper);
    }

    public MultiWrapperInner<MAIN> innerJoin(String relationCode, MultiWrapperSubInner<?, ?> subTableWrapper) {
        MultiConstant.JoinTypeEnum joinType = MultiConstant.JoinTypeEnum.inner_join;
        return this.getMainMultiWrapper(joinType, relationCode, subTableWrapper);
    }

    public MultiWrapperInner<MAIN> getMainMultiWrapper(MultiConstant.JoinTypeEnum joinType, String relationCode, MultiWrapperSubInner<?, ?> subTableWrapper) {
        wrapperSubAndRelations.add(new MultiWrapperSubAndRelation<>(joinType, relationCode, subTableWrapper));
        return this;
    }

    // 普通变量格式
    private static final Pattern patternVar = Pattern.compile("^[\\w]+$");

    /**
     * @param extendParams 参数Map例如:
     *                     {
     *                     "userAndUserStaff_balance":"100#%#", //其中userAndUserStaff是relationCode,  like '张三%' 才能走索引
     *                     "userStaff_sex":"1"
     *                     }
     */
    public MultiWrapperInner<MAIN> extendParams(Map<String, ?> extendParams) {
        Map<String, List<WhereDataUnit>> extendParamMap = new LinkedHashMap<>();
        for (String key : extendParams.keySet()) {
//            String keyNew = key.replaceFirst("\\[\\d]", "");
            String relationCode = null;
            String propName = key;
            Field field = null;
            String[] keys = key.split("\\.");
            if (keys.length == 2) {
//                relationCode = keys[0];
                propName = keys[1];
            }
            if (!Arrays.stream(keys).allMatch(s->patternVar.matcher(s).matches())){
                continue;
            }
            // 查询字段
            for (IMultiWrapperSubAndRelationTreeNode node : relationsAndMain) {
                MultiTuple3<Field, Method, Method> fieldMethod = MultiRelationCaches.getClassInfos(node.getTableClassThis()).get(propName);
                if (fieldMethod != null) {
//                    if (relationCode == null) {
                    relationCode = node.getRelationCode();
//                    }
                    field = fieldMethod.getT1();
                    break;
                }
            }

            Object value = extendParams.get(key);

            // 默认操作
            WhereOptEnum opt = WhereOptEnum.eq;
            if (field == null) {
                continue;
            }
            if (String.class.equals(field.getType())) {
                opt = WhereOptEnum.likeAll;
            }
            if (value instanceof String) {
                String valueStr = (String) value;
                if (MultiUtil.isEmpty(valueStr)) {
                    continue;
                }
                //特定操作
                if (valueStr.indexOf("#") == 0 && valueStr.indexOf("#", 1) == 3) {
                    String optPrefix = valueStr.substring(0, 4);
                    opt = WhereOptEnum.PARAM_MAP_OPT_PREFIX_MAP.get(optPrefix);
                    MultiUtil.assertNoNull(opt, "无效的查询条件操作类型{}|{} {}", optPrefix, key, value);
                    value = ((String) value).substring(4);
                    switch (opt) {
                        case in:
                        case not_in:
                            if (MultiUtil.isEmpty((String) value)) {
                                // 空字符串不过滤
                                continue;
                            } else {
                                value = Arrays.stream(((String) value).split(",")).collect(Collectors.toList());
                            }
                            break;
                    }
                }
            }
            extendParamMap.computeIfAbsent(relationCode, c -> new ArrayList<>(8)).add(new WhereDataUnit(key, opt, value));
        }
        this.extendParamMap = extendParamMap;
        return this;
    }

    /**
     * 输出最终sql
     */
    public String computeAggregateSql() {

        String mainClassName = wrapperMain.getClassName();

        if (!loaded) {
            loaded = true;
            MultiUtil.assertNoNull(mainClassName, "请先通过MultiWrapperMain.lambda(UserInfo.class)或者.eq(UserInfo::getId)确定表名,在执行查询");
            // 1. 解析 关系
            this.loadRelations();

            this.extendParams(extendParams);
        }


        String mainTableIdFieldName = MultiUtil.camelToUnderline(MultiRelationCaches.getTableIdField(wrapperMain.getClazz()).getName());
        //全部字段聚合 select sum(t1.amount),sum(t2.qty) ...
        // sum/avg全部数字型字段 count(1) countDistinct全部字段
        List<MultiConstant.MultiAggregateTypeEnum> aggregateAllTypes = this.wrapperMain.getAggregateAllTypes();
        List<String> aggregateFields = new ArrayList<>(32);
        if (aggregateAllTypes.size() > 0) {
            aggregateFields.addAll(
                    aggregateAllTypes.stream().flatMap(aggregateAllType ->
                                    MultiConstant.MultiAggregateTypeEnum.COUNT.equals(aggregateAllType) ?
//                                    DISTINCT " + mainClassName + "." + MultiUtil.camelToUnderline(mainTableIdFieldName) + ")
                                            Stream.of("count(1) as \"COUNT. . \"") :
                                            Stream.of(
                                                    this.computeAggregateFieldAssOne(this.wrapperMain, mainClassName, aggregateAllType),
                                                    this.wrapperSubAndRelationSort.stream().flatMap(
                                                            multiWrapperSubAndRelation -> this.computeAggregateFieldAssOne(multiWrapperSubAndRelation.getWrapperSub(), multiWrapperSubAndRelation.getRelationCode(), aggregateAllType)
                                                    )
                                            ).flatMap(l -> l)
                    ).collect(Collectors.toList())
            );
            //过滤掉非数字的属性
        }
        this.getAggregateInfosRecursion(this.relationTree, aggregateFields);
        if (MultiUtil.isEmpty(aggregateFields)) {
            if (MultiUtil.isEmpty(aggregateAllTypes)) {
                throw new MultiException("没有可以(要)聚合的类型,无法查询");
            }
            throw new MultiException("没有可以(要)聚合的列,无法查询");
        }
        List<String> groupByFieldsTemp = this.getWrapperMain().getGroupByFields();
        List<String> groupByFields = groupByFieldsTemp.stream()
                .map(fieldNameTemp -> "`" + MultiUtil.getFieldNameFinal(this.relationTree.getCurr().getRelationCode(), fieldNameTemp) + "`").collect(Collectors.toList());
        List<String> groupByFieldsAs = groupByFieldsTemp.stream()
                .map(fieldNameTemp -> {
                    String fieldNameFinal = MultiUtil.getFieldNameFinal(this.relationTree.getCurr().getRelationCode(), fieldNameTemp);
                    return "`" + fieldNameFinal + "` as \"PROPS." + fieldNameFinal + "\"";
                }).collect(Collectors.toList());
        aggregateFields.addAll(groupByFieldsAs);
        //指定字段聚合
        String sqlSelect = "select " + String.join(",\n", aggregateFields);
        String sqlFrom = "\nfrom " + MultiUtil.camelToUnderline(mainClassName) + " " + mainClassName;
        String sqlLeftJoinOn = "\n" + String.join("\n", getSqlJoinRecursion(this.relationTree));
        String sqlWhere = this.getSqlWhere();
        String groupBy = groupByFields.isEmpty() ? "" : " group by " + String.join(",", groupByFields);

        return this.addSelectContour(sqlSelect, sqlFrom, sqlLeftJoinOn, sqlWhere, "", "", groupBy);
    }

    private String addSelectContour(String sqlSelect, String sqlFrom, String sqlLeftJoinOn, String sqlWhere, String sqlOrder, String sqlLimit, String groupBy) {
        List<String> fullSelectSqls = relationTree.readNodes(node ->
                {

                    String sqlSelectProps = node.getCurr().getMultiWrapperSelectInfo().getSqlSelectProps(null == node.getParent() ? null : node.getParent().getTableClassThis(), node.getCurr().getRelationCode());
                    return sqlSelectProps;
                })
                .stream().filter(Objects::nonNull).collect(Collectors.toList());
        return MultiUtil.format(
                "${sqlSelect} \n" +
                        " from (\n" +
                        "   select \n" +
                        "   ${fullSelectSql} \n" +
                        "   ${sqlFrom} \n" +
                        "   ${sqlLeftJoinOn} \n" +
                        ") t \n" +
                        "${sqlWhere} \n" +
                        "${sqlOrder} \n" +
                        "${sqlLimit} \n" +
                        "${groupBy}",
                Map.of(
                        "sqlSelect", sqlSelect,
                        "fullSelectSql", String.join(",\n", fullSelectSqls),
                        "sqlFrom", sqlFrom,
                        "sqlLeftJoinOn", sqlLeftJoinOn,
                        "sqlWhere", sqlWhere,
                        "sqlOrder", sqlOrder,
                        "sqlLimit", sqlLimit,
                        "groupBy", groupBy
//                        "selects", String.join(",\n", fullSelectSql),
                ));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void getAggregateInfosRecursion(MultiTreeNode<IMultiWrapperSubAndRelationTreeNode> relationTree, List<String> aggregateFields) {
        IMultiWrapperSubAndRelationTreeNode curr = relationTree.getCurr();
        if (curr instanceof MultiWrapperMainInner) {
            aggregateFields.addAll(((MultiWrapperMainInner) curr).getSqlAggregate(relationTree.getCurr().getRelationCode()));
        } else if (curr instanceof MultiWrapperSubAndRelation) {
            aggregateFields.addAll(((MultiWrapperSubAndRelation) curr).getWrapperSub().getSqlAggregate(relationTree.getCurr().getRelationCode()));
        }
        relationTree.getChildren().forEach(child -> getAggregateInfosRecursion(child, aggregateFields));
    }


    /**
     * 输出最终sql
     */
    public String computeSql() {
        String mainClassName = wrapperMain.getClassName();

        if (!loaded) {
            loaded = true;
            MultiUtil.assertNoNull(mainClassName, "请先通过MultiWrapperMain.lambda(UserInfo.class)或者.eq(UserInfo::getId)确定表名,在执行查询");
            // 1. 解析 关系
            this.loadRelations();

            this.extendParams(extendParams);
        }
        // 2.1. 解析 select要查出的字段语句片段
        // select user_staff.* from user_staff
//        )
//        List<String> selectPropsList = relationTree.readNodes(node -> node.getCurr().getMultiWrapperSelectInfo().getSqlSelectProps(null == node.getParent() ? null : node.getParent().getTableClassThis(), node.getCurr().getRelationCode())).stream().filter(Objects::nonNull).collect(Collectors.toList());
//        List<String> selectPropsListMore = relationTree.readNodes(node -> String.join(",\n", node.getCurr().getMultiWrapperSelectInfo().getSelectMores())).stream().filter(MultiUtil::noEmpty).collect(Collectors.toList());
//        selectPropsList.addAll(selectPropsListMore);
        //        List<String> selectPropsList = this.wrapperSubAndRelations.stream().map(o -> o.getWrapperSub().getSqlSelectProps(o.getWrapperSub().getClazz(),o.getRelationCode())).filter(Objects::nonNull).collect(Collectors.toList());
//        selectPropsList.add(0, wrapperMain.getSqlSelectProps(null, wrapperMain.getRelationCode()));
//        String sqlSelect = "\nselect\n" + selectPropsList.stream().filter(Objects::nonNull).collect(Collectors.joining(",\n"));

        // 3. 解析 from主表,limit主表语句片段
        //	SELECT u.*,p.* FROM user_info                          u LEFT JOIN principal_user p ON p.user_id = u.id where p.admin_flag = 1;
        //	SELECT u.*,p.* FROM (select * from user_info limit 10) u LEFT JOIN principal_user p ON p.user_id = u.id where p.admin_flag = 1;
        String sqlFrom = "\nFROM " + wrapperMain.getSqlFrom(mainClassName);

        String sqlLeftJoinOn = "\n" + String.join("\n", getSqlJoinRecursion(this.relationTree));

        String sqlWhere = this.getSqlWhere();
        String sqlLimit = wrapperMain.getSqlLimit();

        // 读取 selectProps
        relationTree.readNodes(node -> node.getCurr().getMultiWrapperSelectInfo().loadSelectFields(null == node.getParent() ? null : node.getParent().getTableClassThis(), node.getCurr().getRelationCode()));

        // String sqlOrder = "";
        List<String> orders =
                wrapperMain.getOrderInfos()
                        .stream().map(order -> {
                            String orderStr = order.toSql();
                            if (!orderStr.contains(".")) {
                                String[] orderSplit = orderStr.split(" ");
                                String orderKey = orderSplit[0];
                                String ascOrDesc = orderSplit.length == 2 ? orderSplit[1] : "";
                                List<String> relationCodes = relationTree.readNodes(node -> node.getCurr().getMultiWrapperSelectInfo().getSelectFields().contains(orderKey) ? node.getCurr().getRelationCode() : null).stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
                                if (relationCodes.size() > 1) {
                                    throw new MultiException(String.join(",", relationCodes) + " has the prop: " + orderKey);
                                }
                                if (relationCodes.size() == 1) {
                                    return "`" + relationCodes.get(0) + "." + orderKey + "` " + ascOrDesc;
                                }
                            }
                            return orderStr;
                        }).collect(Collectors.toList());

        String sqlOrder = String.join(",", orders);
        sqlOrder = MultiUtil.isEmpty(sqlOrder) ? "" : "\n ORDER BY " + sqlOrder;

        return this.addSelectContour("select * ", sqlFrom, sqlLeftJoinOn, sqlWhere, sqlOrder, sqlLimit, "");
    }


    public Stream<String> computeAggregateFieldAssOne(MultiWrapperSelect<?, ?> multiWrapperSelect, String relationCode, MultiConstant.MultiAggregateTypeEnum aggregateAllType) {
        Map<String, MultiTuple3<Field, Method, Method>> classInfos = MultiRelationCaches.getClassInfos(multiWrapperSelect.getClazz());
        return multiWrapperSelect.getSelectFieldNames().stream()
                .filter(propName -> {
                            //过滤部分字段
                            MultiTuple3<Field, Method, Method> fieldInfo = classInfos.get(propName);
                            MultiUtil.assertNoNull(fieldInfo, "{0}不存在{1}属性", multiWrapperSelect.getClazz(), propName);
                            Class<?> relationFieldType = fieldInfo.getT1().getType();
                            return aggregateAllType.getFieldTypeFilter().apply(relationFieldType);
                        }
                ).map(propName -> MultiWrapperAggregate.appendOneField(aggregateAllType, relationCode, propName, null));
    }

    public List<String> getSqlJoinRecursion(MultiTreeNode<IMultiWrapperSubAndRelationTreeNode> relationTree) {
        String relationCode = relationTree.getCurr().getRelationCode();
        List<String> leftJoins = relationTree.getChildren().stream().map(r -> {
            if (r.getCurr() instanceof MultiWrapperSubAndRelation) {
                //noinspection rawtypes
                MultiWrapperSubAndRelation<?> curr = (MultiWrapperSubAndRelation) r.getCurr();
                String subRelationCode = curr.getRelationCode();
                IMultiClassRelation relation = MultiClassRelationFactory.INSTANCE.getRelationCodeMap().get(subRelationCode);
                boolean thisIs1 = curr.getClassNameThis().equals(relation.getClassName1());
                String leftJsonOn = " " + curr.getJoinType().getJoinSqlSegment() + " " + MultiUtil.camelToUnderline(curr.getClassNameThis()) + " " + subRelationCode + " on "
                        + subRelationCode + "." + MultiUtil.camelToUnderline(thisIs1 ? relation.getClass1KeyProp() : relation.getClass2KeyProp())
                        + "="
                        + relationCode + "." + MultiUtil.camelToUnderline(thisIs1 ? relation.getClass2KeyProp() : relation.getClass1KeyProp());
                String sqlWhereSub = null;
//                String sqlWhereSub = curr.getWrapperSub().getSqlWhereProps(curr.getTableClassThis(), curr.getRelationCode());
                return leftJsonOn + (MultiUtil.isEmpty(sqlWhereSub) ? " " : " and " + sqlWhereSub);
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        List<String> leftJoinsSub = relationTree.getChildren().stream().flatMap(child -> getSqlJoinRecursion(child).stream()).collect(Collectors.toList());
        leftJoins.addAll(leftJoinsSub);
        return leftJoins;
    }

    public String getSqlWhere() {
        // 4. 解析 where条件语句片段
        //    where user_staff.state = 0
        //      and user_staff_address.del_flag = 0
        List<String> sqlWheres = relationsAndMain.stream().map(curr -> {
            MultiWrapperCommonInner<?, ?> wrapperInner = null;
            if (curr instanceof MultiWrapperMainInner) {
                wrapperInner = (MultiWrapperMainInner<?, ?>) curr;
            } else if (curr instanceof MultiWrapperSubAndRelation) {
                wrapperInner = ((MultiWrapperSubAndRelation<?>) curr).getWrapperSub();
            }
            String relationCode = curr.getRelationCode();
            assert wrapperInner != null;
            return wrapperInner.getSqlWhereProps(curr.getTableClassThis(), relationCode, this.extendParamMap.get(relationCode));
        }).collect(Collectors.toList());

        log.debug("getSqlWhere {}", JSONUtils.toString(sqlWheres));
        String sqlWhereAppend = sqlWheres.stream().filter(s -> !MultiUtil.isEmpty(s)).collect(Collectors.joining("\n and "));
        return MultiUtil.isEmpty(sqlWhereAppend) ? MultiConstant.Strings.EMPTY : "where 1=1\n and " + sqlWhereAppend;
    }

//    public List<String> getSqlMainWhMerePropsMainRecursion(MultiTreeNode<IMultiWrapperSubAndRelationTreeNode> treeNode, Map<String, List<WhereDataUnit>> extendParamMap) {
//        List<String> sqlChildren = treeNode.getChildren().stream().map(treeNodeChild -> {
//            IMultiWrapperSubAndRelationTreeNode curr = treeNodeChild.getCurr();
//            if (curr instanceof MultiWrapperSubAndRelation) {
//                MultiWrapperSubMainWhereInner<?> mainWhere = ((MultiWrapperSubAndRelation<?>) curr).getWrapperSub().getMainWhere();
//                if (mainWhere != null) {
//                    return mainWhere.getSqlWhereProps(curr.getTableClassThis(), curr.getRelationCode());
//                }
//            }
//            return null;
//        }).filter(Objects::nonNull).collect(Collectors.toList());
//        List<String> sqlGrandChildren = treeNode.getChildren().stream().flatMap(treeNodeChild -> getSqlMainWhMerePropsMainRecursion(treeNodeChild, extendParamMap).stream()).filter(Objects::nonNull).collect(Collectors.toList());
//        sqlChildren.addAll(sqlGrandChildren);
//        return sqlChildren;
//    }

    public void loadRelations() {
        if (relationTree == null) {
            // 1.1 解析 主表和副表的关系树
            relationTree = this.reloadRelations(wrapperMain, this.wrapperSubAndRelations);

            // 1.2 关系统一按树自顶向下排列
            this.wrapperSubAndRelationSort = new ArrayList<>();
            relationTree.consumerTopToBottom(relationNode -> {
                if (relationNode instanceof MultiWrapperSubAndRelation) {
                    wrapperSubAndRelationSort.add((MultiWrapperSubAndRelation<?>) relationNode);
                }
            });

            relationsAndMain = new ArrayList<>(wrapperSubAndRelationSort);
            relationsAndMain.add(0, wrapperMain);
        }
    }

    public MultiTreeNode<IMultiWrapperSubAndRelationTreeNode> reloadRelations(MultiWrapperMainInner<MAIN, ?> wrapperMain, List<MultiWrapperSubAndRelation<?>> wrapperSubAndRelations) {
        //relationCode可能缺省,去关系表中加载
        this.fillRelationCodeAndTableThisOther(wrapperMain, wrapperSubAndRelations);

        List<IMultiWrapperSubAndRelationTreeNode> relationsAndMain = new ArrayList<>(wrapperSubAndRelations);
        relationsAndMain.add(wrapperMain);

        //构建关系树  将用在
        // 1.在按顺序生成left join语句(否则会报错)
        // 2.按顺序映射查询结果到实体类上
        return MultiTreeNode.buildTree(relationsAndMain, o -> o, o -> o, o -> o instanceof MultiWrapperMainInner);
    }

    public void fillRelationCodeAndTableThisOther(MultiWrapperMainInner<MAIN, ?> wrapperMain, List<MultiWrapperSubAndRelation<?>> wrapperSubAndRelations) {
        String mainClassName = wrapperMain.getClassName();

        List<MultiWrapperSubAndRelation<?>> hasCodeRelations = wrapperSubAndRelations.stream().filter(relation -> null != relation.getRelationCode()).collect(Collectors.toList());
        List<MultiWrapperSubAndRelation<?>> noCodeRelations = wrapperSubAndRelations.stream().filter(relation -> null == relation.getRelationCode()).collect(Collectors.toList());

        //已经确定关系的表
        Set<String> relationClassNames = new HashSet<>();
        relationClassNames.add(mainClassName);
        hasCodeRelations.forEach(relationHasCode -> {
            String subClassName = relationHasCode.getWrapperSub().getClassName();
            relationClassNames.add(relationHasCode.getWrapperSub().getClassName());
            IMultiClassRelation relation = MultiClassRelationFactory.INSTANCE.getRelationCodeMap().get(relationHasCode.getRelationCode());
            this.fillTableThisAndOther(relationHasCode, subClassName, relation);
        });

        noCodeRelations.forEach(noCodeRelation ->
                {
                    String subClassName = noCodeRelation.getWrapperSub().getClassName();
                    boolean hasRelation = false;
                    for (String relationClassName1 : relationClassNames) {
                        List<IMultiClassRelation> relations = MultiClassRelationFactory.INSTANCE.getRelation2ClassNameMap()
                                .getOrDefault(subClassName, Collections.emptyMap())
                                .getOrDefault(relationClassName1, Collections.emptyList());
                        if (relations.size() > 1) {
                            //有多种关系,需要重新确定
                            throw new MultiException(relationClassName1 + "和" + subClassName + "存在多种关系,需要手动指定relationCode");
                        }
                        if (relations.size() < 1) {
                            continue;
                        }
                        IMultiClassRelation relation = relations.get(0);

                        noCodeRelation.setRelationCode(relation.getCode());
                        this.fillTableThisAndOther(noCodeRelation, subClassName, relation);

                        hasRelation = true;
                        break;
                    }
                    if (hasRelation) {
                        relationClassNames.add(subClassName);
                    } else {
                        throw new MultiException(subClassName + "和[" + String.join(",", relationClassNames) + "]没有存在表关系,无法关联");
                    }
                }
        );
    }

    public void fillTableThisAndOther(MultiWrapperSubAndRelation<?> noCodeRelation, String subClassName, IMultiClassRelation multiTableRelation) {
        String className1 = multiTableRelation.getClassName1();
        String className2 = multiTableRelation.getClassName2();
        if (className1.equals(subClassName)) {
            noCodeRelation.setClassNameThis(className1);
            noCodeRelation.setClassNameThisOneOrMany(multiTableRelation.getClass1OneOrMany());
            noCodeRelation.setClassNameOtherRequire(multiTableRelation.getClass1Require());
            noCodeRelation.setClassNameOther(className2);
        } else if (className2.equals(subClassName)) {
            noCodeRelation.setClassNameThis(className2);
            noCodeRelation.setClassNameThisOneOrMany(multiTableRelation.getClass2OneOrMany());
            noCodeRelation.setClassNameOtherRequire(multiTableRelation.getClass2Require());
            noCodeRelation.setClassNameOther(className1);
        } else {
            throw new MultiException("表关系" + multiTableRelation.getCode() + "(" + className1 + "," + className2 + ")其中之一必须和当前查询的表" + subClassName);
        }
    }

    /**
     *
     */
    public void addToListNew(List<MultiWrapperSubAndRelation<?>> wrapperSubAndRelationsNew, Set<String> parentClassNames, List<MultiWrapperSubAndRelation<?>> wrapperSubAndRelations) {
        ArrayList<MultiWrapperSubAndRelation<?>> multiWrapperSubAndRelationsTemp = new ArrayList<>(wrapperSubAndRelations);

        List<MultiWrapperSubAndRelation<?>> subRelations = wrapperSubAndRelations.stream().filter(relation ->
                {
                    IMultiClassRelation relation1Now = getRelationByCode(relation.getRelationCode());
                    return parentClassNames.stream().anyMatch(parentClass -> relation1Now.getClassNames().contains(parentClass));
                }
        ).collect(Collectors.toList());
        wrapperSubAndRelationsNew.addAll(subRelations);
        multiWrapperSubAndRelationsTemp.removeAll(subRelations);

        //添加父节点表名
        subRelations.forEach(subRelation -> parentClassNames.addAll(getRelationByCode(subRelation.getRelationCode()).getClassNames()));

        if (multiWrapperSubAndRelationsTemp.size() > 0) {
            //递归添加
            addToListNew(wrapperSubAndRelationsNew, parentClassNames, multiWrapperSubAndRelationsTemp);
        }
    }

    public IMultiClassRelation getRelationByCode(String relationCode) {
        return MultiClassRelationFactory.INSTANCE.getRelationCodeMap().get(relationCode);
    }

    public MultiWrapperMainInner<MAIN, ?> getWrapperMain() {
        return wrapperMain;
    }

    public MultiTreeNode<IMultiWrapperSubAndRelationTreeNode> getRelationTree() {
        return relationTree;
    }
}
