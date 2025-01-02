package com.pro.framework.mtq.service.multiwrapper.executor;

import com.pro.framework.api.database.AggregateResult;
import com.pro.framework.api.database.OrderItem;
import com.pro.framework.api.database.page.IPageInput;
import com.pro.framework.api.structure.FirstHashMap;
import com.pro.framework.api.util.JSONUtils;
import com.pro.framework.jdbc.sqlexecutor.DbAdaptor;
import com.pro.framework.mtq.service.multiwrapper.config.BaseMultiConfig;
import com.pro.framework.mtq.service.multiwrapper.constant.MultiConstant;
import com.pro.framework.mtq.service.multiwrapper.dto.MultiAggregateResultMap;
import com.pro.framework.mtq.service.multiwrapper.dto.MultiPageResult;
import com.pro.framework.mtq.service.multiwrapper.entity.ClassRelationOneOrManyEnum;
import com.pro.framework.mtq.service.multiwrapper.entity.IMultiPageResult;
import com.pro.framework.mtq.service.multiwrapper.sqlsegment.MultiWrapperAggregate;
import com.pro.framework.mtq.service.multiwrapper.util.*;
import com.pro.framework.mtq.service.multiwrapper.wrapper.inner.IMultiWrapperSubAndRelationTreeNode;
import com.pro.framework.mtq.service.multiwrapper.wrapper.inner.MultiWrapperInner;
import com.pro.framework.mtq.service.multiwrapper.wrapper.inner.MultiWrapperMainInner;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Slf4j
@Component
public class MultiExecutorInner {

    @SneakyThrows
    public static <MAIN> List<MAIN> list(MultiWrapperInner<MAIN> wrapper) {
        List<OrderItem> orderInfos = wrapper.getWrapperMain().getOrderInfos();
        if (MultiUtil.isEmpty(orderInfos)) {
            wrapper.getWrapperMain().setOrderInfos(defaultOrders(wrapper));
        }
        // --- 组装sql ---
        String sql = wrapper.computeSql();
        //执行sql
        Map<String, Object> relationIdObjectMap = new HashMap<>(2048);
        List<MAIN> mains = BaseMultiConfig.multiDbAdaptor.select(sql, (resultSet) -> {
            MultiTuple2<MAIN, Boolean> mainAndIsNew = null;
            try {
                mainAndIsNew = buildReturnRecursion(MultiConstant.Strings.EMPTY, null, wrapper.getRelationTree(), resultSet, relationIdObjectMap, false);
            } catch (Exception e) {
                log.error("buildReturnRecursion \n{}\n", sql, e);
                throw new RuntimeException(e);
            }
            MAIN dto = mainAndIsNew.getT1();
            return mainAndIsNew.getT2() ? dto : null;
        }).stream().filter(Objects::nonNull).collect(Collectors.toList());


//        log.info("Multi 查询Sql:\n {} \n 查询结果:{}条", sql, mains.size());

        if (null != BaseMultiConfig.multiProperties && BaseMultiConfig.multiProperties.getCheckRelationRequire()) {
            //检查表关系中,一方有数据,另一方必须有数据,是否有异常数据(测试环境可以开启)
            checkRequireRecursion(wrapper.getWrapperMain().getClass(), mains, wrapper.getRelationTree().getChildren());
        }
        return mains;
    }

    @SneakyThrows
    public static <MAIN, VAL> MAIN getById(Class<MAIN> mainClass, Serializable id) {
        Field idField = MultiRelationCaches.getTableIdField(mainClass);
        String idFieldName = MultiUtil.camelToUnderline(idField.getName());
        MultiWrapperMainInner<MAIN, ?> mainInner = new MultiWrapperMainInner<>(mainClass);
        mainInner.eq(idFieldName, id);
        MultiWrapperInner<MAIN> wrapper = new MultiWrapperInner<>(mainInner);
        List<MAIN> list = list(wrapper);
        if (list.size() > 1) {
            log.warn("getOne查询出两条数据:" + wrapper.computeSql());
        }
        return list.isEmpty() ? null : list.get(0);
    }

    @SneakyThrows
    public static <MAIN> MAIN getOne(MultiWrapperInner<MAIN> wrapper) {
        List<MAIN> list = list(wrapper);
        if (list.size() > 1) {
            log.warn("getOne查询出两条数据:" + wrapper.computeSql());
        }
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 执行分页查询
     *
     * @param pageInput 分页信息
     * @param wrapper   表信息,聚合信息,过滤条件...
     * @return 聚合查询结果 例如 {"sum":{"userAndUserStaff.balance":"100.00"}}
     */
    @SneakyThrows
    public static <MAIN> IMultiPageResult<MAIN> page(IPageInput pageInput, MultiWrapperInner<MAIN> wrapper) {
        MultiWrapperMainInner<MAIN, ?> wrapperMain = wrapper.getWrapperMain();
        boolean containsAggregate = wrapperMain.getAggregateAllTypes().size() + wrapperMain.getAggregateInfos().size() > 0;

        Long count;
        AggregateResult aggregateResult;
        List<MAIN> list = Collections.emptyList();


        //查询聚合
        wrapperMain.count();
        aggregateResult = aggregate(wrapper);
        count = aggregateResult.getCount();

        if (count > 0) {
            //清理聚合信息(才能执行普通列表查询)
            wrapperMain.aggregateBackupAndClear();
            //填充分页信息
            wrapperMain.limit((pageInput.getCurPage() - 1) * pageInput.getPageSize(), pageInput.getPageSize());
            //查询补充排序
            wrapper.getWrapperMain().getOrderInfos().addAll(pageInput.getOrders());

            // --- limit 列表查询
            list = list(wrapper);

            //恢复聚合信息为了不更改源对象
            wrapperMain.aggregateRestore();
        }

        IMultiPageResult<MAIN> pageOutput = new MultiPageResult<>();
        //独立 count (如果wrapper里面有可以直接取)
        pageOutput.setTotal(count);
        pageOutput.setTotalPages((count + pageInput.getPageSize() - 1) / pageInput.getPageSize());
        pageOutput.setAggregateResult(containsAggregate ? aggregateResult : null);
        pageOutput.setRecords(list);
        return pageOutput;
    }

    /**
     * 执行聚合查询
     *
     * @param wrapper 表信息,聚合信息,过滤条件...
     * @return 聚合查询结果 例如 {"sum":{"userAndUserStaff.balance":"100.00"}}
     */
    @SneakyThrows
    public static <MAIN> AggregateResult aggregate(MultiWrapperInner<MAIN> wrapper) {
        String aggregateSql = wrapper.computeAggregateSql();

        Map<String, ?> objectMap = BaseMultiConfig.multiDbAdaptor.selectFirstRow(aggregateSql);

        AggregateResult aggregateResult = getResult(objectMap);

//        log.info("Multi 查询Sql:\n {} \n 查询结果:{}", aggregateSql, aggregateResult);

        return aggregateResult;
    }

    private static AggregateResult getResult(Map<String, ?> objectMap) {
        AggregateResult aggregateResult = new AggregateResult();
        if (objectMap == null) {
            return aggregateResult;
        }
        Map<MultiAggregateResultMap, ? extends Map.Entry<String, ?>> map = MultiUtil.listToMap(objectMap.entrySet(), e -> new MultiAggregateResultMap(e.getKey()));
        map.entrySet().stream().filter(e -> null != e.getKey().getAggregateType()).collect(Collectors.groupingBy(e -> e.getKey().getAggregateType())).forEach((aggregateType, list) -> {
            FirstHashMap<String, Object> keyValueMap = list.stream().collect(Collectors.toMap(e -> (null == e.getKey().getRelationCode() ? "" : (e.getKey().getRelationCode() + ".")) + e.getKey().getPropName(), e -> e.getValue().getValue(), (v1, v2) -> v1, FirstHashMap::new));
            //聚合字段重命名
            list.stream().filter(e -> MultiWrapperAggregate.RELATION_CODE_TEMP_ALIAS_ALIAS.equals(e.getKey().getRelationCode())).forEach(e -> keyValueMap.put(e.getKey().getPropName(), e.getValue().getValue()));
            //noinspection RedundantSuppression
            switch (aggregateType) {
                case SUM:
                    aggregateResult.setSum(keyValueMap);
                    break;
                case AVG:
                    aggregateResult.setAvg(keyValueMap);
                    break;
                case COUNT:
                    aggregateResult.setCount(Long.parseLong(list.get(0).getValue().getValue().toString()));
                    break;
                case COUNT_DISTINCT:
                    aggregateResult.setCountDistinct(keyValueMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> null == e.getValue() ? 0 : Long.parseLong(e.getValue().toString()), (v1, v2) -> v1, FirstHashMap::new)));
                    break;
                case MAX:
                    aggregateResult.setMax(keyValueMap);
                    break;
                case MIN:
                    aggregateResult.setMin(keyValueMap);
                    break;
                case GROUP_CONCAT:
                    //noinspection unchecked,rawtypes
                    aggregateResult.setGroupConcat((FirstHashMap) keyValueMap);
                    break;
                case PROPS:
                    //noinspection unchecked,rawtypes
                    aggregateResult.setProps(keyValueMap);
                    break;
                default:
                    throw new MultiException(aggregateType + "该聚合方法待实现");
            }
        });
        return aggregateResult;
    }

    /**
     * 执行聚合查询
     *
     * @param wrapper 表信息,聚合信息,过滤条件...
     * @return 聚合查询结果 例如 {"sum":{"userAndUserStaff.balance":"100.00"}}
     */
    @SneakyThrows
    public static <MAIN> List<AggregateResult> aggregateList(MultiWrapperInner<MAIN> wrapper) {
        String aggregateSql = wrapper.computeAggregateSql();

        List<Map<String, Object>> list = BaseMultiConfig.multiDbAdaptor.select(aggregateSql, DbAdaptor::rsToMap);
        List<AggregateResult> aggregateResult = list.stream().map(MultiExecutorInner::getResult).collect(Collectors.toList());
//        log.info("Multi 查询Sql:\n {} \n 查询结果:{}", aggregateSql, aggregateResult);
        return aggregateResult;
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    private static <MAIN_OR_SUB> void checkRequireRecursion(Class<?> currClass, List<MAIN_OR_SUB> currDatas, List<MultiTreeNode<IMultiWrapperSubAndRelationTreeNode>> subRelationNodes) {
        if (MultiUtil.isEmpty(currDatas)) {
            return;
        }
        for (MultiTreeNode<IMultiWrapperSubAndRelationTreeNode> relationTreeNode : subRelationNodes) {
            IMultiWrapperSubAndRelationTreeNode curr = relationTreeNode.getCurr();
            String relationCode = curr.getRelationCode();
            Boolean subTableRequire = curr.getClassNameOtherRequire();

            for (MAIN_OR_SUB currData : currDatas) {
                Method getMethod = MultiRelationCaches.getTableWithTable_getSetMethod(currClass, relationCode).getT1();
                if (getMethod == null) {
                    return;
                }
                Object subValues = getMethod.invoke(currData);
                if (subTableRequire) {
                    //检查当前
                    if (subValues == null) {
                        throwRequireException(currClass.getSimpleName(), relationCode, currData);
                    }
                    if (subValues instanceof List) {
                        if (((List<?>) subValues).size() == 0) {
                            throwRequireException(currClass.getSimpleName(), relationCode, currData);
                        }
                    }
                }
                if (subValues == null) {
                    //为空子表没数据检查,跳过
                    continue;
                }
                List<MAIN_OR_SUB> subs;
                if (subValues instanceof List && ((List<?>) subValues).size() > 0) {
                    subs = (List<MAIN_OR_SUB>) subValues;
                } else {
                    subs = (List<MAIN_OR_SUB>) Collections.singletonList(subValues);
                }
                //递归检查
                checkRequireRecursion(relationTreeNode.getCurr().getTableClassThis(), subs, relationTreeNode.getChildren());
            }
        }
    }

    private static <MAIN_OR_SUB> void throwRequireException(String classNameThis, String relationCode, MAIN_OR_SUB currData) {
        throw new MultiException(classNameThis + "表在" + relationCode + "关系中,需要另一张表一定有数据,但没有|" + classNameThis + "表数据:" + JSONUtils.toString(currData));
    }

    /**
     * 递归构造子表对象
     *
     * @param <MAIN_MAIN_SUB>     父表对象的泛型
     * @param parentCodeAppendId  父实体的唯一code+ID(多层级)
     * @param parentEntity        父表对象
     * @param relationTreeNode    父表和子表当前的关系(.chlidren()是子表和他的子表的关系)
     * @param resultSet           sql执行后的每一行结果集
     * @param relationIdObjectMap 已经构造好的对象,如果有id,需要按id合并子表信息到list中
     * @param parentIsNew         父节点是不是新增节点
     * @return 新的父表对象信息(旧的副表对象信息)
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows
    private static <MAIN_MAIN_SUB> MultiTuple2<MAIN_MAIN_SUB, Boolean> buildReturnRecursion(
            String parentCodeAppendId,
            MAIN_MAIN_SUB parentEntity,
            MultiTreeNode<IMultiWrapperSubAndRelationTreeNode> relationTreeNode,
            ResultSet resultSet,
            Map<String, Object> relationIdObjectMap,
            boolean parentIsNew
    ) {
        IMultiWrapperSubAndRelationTreeNode currNode = relationTreeNode.getCurr();
        Class<?> currTableClass = currNode.getTableClassThis();
        Class<?> currTableMAINClass = currNode instanceof MultiWrapperMainInner ? ((MultiWrapperMainInner<?, ?>) currNode).getClazz() : currTableClass;
        String currRelationCode = currNode.getRelationCode();
        Field currTableIdField = MultiRelationCaches.getTableIdField(currTableClass);


        // 最外层对象的上级必定为null 里层的必定不为null
        MultiTuple2<Method, Method> tableWithTableGetSetMethod = null;
        if (parentEntity != null) {
            tableWithTableGetSetMethod = MultiRelationCaches.getTableWithTable_getSetMethod(parentEntity.getClass(), currNode.getRelationCode());
            if (null == tableWithTableGetSetMethod) {
                return new MultiTuple2<>(null, false);
            }
        }

        String idFieldName = currRelationCode + "." + currTableIdField.getName();
        Object id = getValue(idFieldName, currTableIdField.getType(), resultSet);
        String relationIdObjectMapKey = parentCodeAppendId + "_" + idFieldName + "_" + id;
        MAIN_MAIN_SUB currEntity = (MAIN_MAIN_SUB) relationIdObjectMap.get(relationIdObjectMapKey);

        //要新增元素
        boolean isNew = parentIsNew || currEntity == null;
        if (isNew) {
            //重复则不再生成
            currEntity = (MAIN_MAIN_SUB) currTableMAINClass.newInstance();

//            Map<String, MultiTuple2<Field, Method>> classInfoMap = MultiRelationCaches.getClassInfos(currTableMAINClass);
            Map<String, MultiTuple3<Field, Method, Method>> classInfoFullMap = MultiRelationCaches.getClassInfosFull(currTableMAINClass);
//            MultiUtil.assertNoNull(classInfoMap, "找不到{0}对应的类", currTableMAINClass);

            List<String> selectFieldNames = currNode.getMultiWrapperSelectInfo().getSelectFields();
            List<String> selectMores = currNode.getMultiWrapperSelectInfo().getSelectMores();
            List<String> selectLess = currNode.getMultiWrapperSelectInfo().getSelectLess();
            List<String> selectProps = new ArrayList<>();
            selectProps.addAll(selectFieldNames);
            if (selectLess != null) {
                selectProps.removeAll(selectLess);
            }
            if (selectMores != null) {
                selectProps.addAll(selectMores.stream().map(row -> {
                    row = row.trim();
                    String[] result;
                    if (row.contains(" as ")) {
                        result = row.split("\\s* as \\s*");
                    } else if (row.contains(" AS ")) {
                        result = row.split("\\s* AS \\s*");
                    } else {
                        result = row.split("\\s+");
                    }
                    String prop = result[result.length - 1];
                    if (prop.startsWith("`") && prop.endsWith("`")) {
                        prop = prop.substring(1, prop.length() - 1);
                    }
                    if (prop.contains(".")) {
                        prop = prop.split("\\.")[1];
                    }
                    return prop;
                }).collect(Collectors.toList()));
            }
            for (String selectFieldName : selectProps) {
//                if ("openRate".equals(selectFieldName)) {
//                    int i = 0;
//                }
                MultiTuple3<Field, Method, Method> filedInfos = classInfoFullMap.get(selectFieldName);
                if (filedInfos == null) {
                    continue;
                }
//                MultiUtil.assertNoNull(filedInfos, "找不到{0}对应的属性{1}", currTableClass, selectFieldName);

                Class<?> fieldReturnType = filedInfos.getT1().getType();
                Method fieldSetMethod = filedInfos.getT2();
                Object value = getValue(currRelationCode + "." + selectFieldName, fieldReturnType, resultSet);
                fieldSetMethod.invoke(currEntity, value);
            }

            //顶层节点为null,不用出setSubEntity(subEntity);
            if (parentEntity != null) {
                setCurrEntityInToParent(currNode, parentEntity, currTableClass, currRelationCode, currEntity, tableWithTableGetSetMethod);
            }
        }

        relationIdObjectMap.put(relationIdObjectMapKey, currEntity);
        //副表信息,要递推填充下去
        MAIN_MAIN_SUB finalCurrEntity = currEntity;
        relationTreeNode.getChildren().forEach(subNode -> buildReturnRecursion(relationIdObjectMapKey, finalCurrEntity, subNode, resultSet, relationIdObjectMap, isNew));
        return new MultiTuple2<>(currEntity, isNew);
    }

    @SneakyThrows
    private static <MAIN_OR_SUB> void setCurrEntityInToParent(IMultiWrapperSubAndRelationTreeNode currNode, MAIN_OR_SUB parentEntity, Class<?> currTableClass, String currRelationCode, MAIN_OR_SUB currEntity, MultiTuple2<Method, Method> tableWithTableGetSetMethod) {
//        if (getSetMethods == null) {
//            return;
//        }
        Method getMethod = tableWithTableGetSetMethod.getT1();
        Method setMethod = tableWithTableGetSetMethod.getT2();
        Object subEntityExists = getMethod.invoke(parentEntity);
        Class<?> returnType = getMethod.getReturnType();

        //列表
        String classNameParent = currNode.getClassNameOther();
        String classNameThis = currNode.getClassNameThis();
        if (List.class.isAssignableFrom(returnType)) {
            if (!ClassRelationOneOrManyEnum.MANY.equals(currNode.getClassNameThisOneOrMany())) {
                log.warn(classNameParent + "与" + classNameThis + "不是1对多(或者多对多)关系,但" + currTableClass + "中" + currRelationCode + "为数组,定义不一致");
            }
            if (subEntityExists == null) {
                subEntityExists = new ArrayList<>(8);
                setMethod.invoke(parentEntity, subEntityExists);
            }
            //noinspection unchecked
            ((Collection<MAIN_OR_SUB>) subEntityExists).add(currEntity);
        } else if (returnType.isArray()) {
            throw new MultiException("暂时不支持array类型参数:" + getMethod);
        } else {
            if (BaseMultiConfig.multiProperties.getCheckRelationOneOrMany() && !ClassRelationOneOrManyEnum.ONE.equals(currNode.getClassNameThisOneOrMany())) {
                throw new MultiException(classNameParent + "与" + classNameThis + "不是1对1(或者多对多)关系,但" + currTableClass + "中" + currRelationCode + "为对象,定义不一致");
            }
            //一对一元素
            if (subEntityExists == null) {
                subEntityExists = currEntity;
                setMethod.invoke(parentEntity, subEntityExists);
            } else {
                log.warn("relationCode=" + currRelationCode + "|一对一,但查询出多个id不同的元素");
            }
        }
    }

    @SneakyThrows
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object getValue(String fieldName, Class type, ResultSet resultSet) {
        if (Long.class.equals(type)) {
            String value = resultSet.getString(fieldName);
            return value == null ? null : Long.valueOf(value);
        } else if (String.class.equals(type)) {
            return resultSet.getString(fieldName);
        } else if (Integer.class.equals(type)) {
            return resultSet.getInt(fieldName);
        } else if (BigDecimal.class.equals(type)) {
            return resultSet.getBigDecimal(fieldName);
        } else if (Date.class.equals(type)) {
            return resultSet.getDate(fieldName);
        } else if (Boolean.class.equals(type)) {
            return resultSet.getBoolean(fieldName);
        } else if (LocalDateTime.class.equals(type)) {
            return MultiUtil.date2LocalDateTime(resultSet.getTimestamp(fieldName));
        } else if (LocalDate.class.equals(type)) {
            return resultSet.getDate(fieldName).toLocalDate();
        } else if (LocalTime.class.equals(type)) {
            return resultSet.getTime(fieldName).toLocalTime();
        } else if (Enum.class.isAssignableFrom(type)) {
            String value = resultSet.getString(fieldName);
            return null == value || value.isEmpty() ? null : Enum.valueOf(type, value);
//            return MultiUtil.getEnumByName((Class<Enum>) type, value);
//            if (IEnum.class.isAssignableFrom(type)) {
//                String value = resultSet.getString(fieldName);
//                return MultiUtil.getEnumByValue((Class<IEnum>) type, value);
//            } else {
//                //默认用枚举的name存取
//                String value = resultSet.getString(fieldName);
//                //noinspection unchecked
//                return MultiUtil.getEnumByName((Class<Enum>) type, value);
//            }
        } else if (Float.class.equals(type)) {
            return resultSet.getFloat(fieldName);
        } else if (Double.class.equals(type)) {
            return resultSet.getDouble(fieldName);
        } else if (Blob.class.equals(type)) {
            return resultSet.getBlob(fieldName);
        }
        throw new MultiException("未知的数据类型|" + fieldName + "|" + type);
    }

    public static final List<MultiTuple2<String, Boolean>> propAscs = List.of(
            new MultiTuple2<>("sort", true),
            new MultiTuple2<>("id", false)
    );

    private static <MAIN> List<OrderItem> defaultOrders(MultiWrapperInner<MAIN> wrapper) {
        List<OrderItem> orders = new ArrayList<>(1);
        MultiWrapperMainInner<MAIN, ?> wrapperMain = wrapper.getWrapperMain();
        String className = wrapperMain.getClassName();
        for (MultiTuple2<String, Boolean> prop : propAscs) {
            String propName = prop.getT1();
            boolean hasProp = MultiRelationCaches.getFieldNamesByClass(wrapperMain.getClazz()).contains(propName);
            if (hasProp) {
                Boolean isAsc = prop.getT2();
                orders.add(new OrderItem("`" + className + "." + propName + "`", isAsc));
//                break;
            }
        }
        return orders;
    }

}
