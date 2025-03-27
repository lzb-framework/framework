package com.pro.framework.mtq.service.multiwrapper;


import com.pro.framework.api.database.AggregateResult;
import com.pro.framework.api.database.GroupBy;
import com.pro.framework.api.database.OrderItem;
import com.pro.framework.api.database.TimeQuery;
import com.pro.framework.api.database.page.IPageInput;
import com.pro.framework.api.database.wheredata.WhereDataUnit;
import com.pro.framework.api.entity.IEntityProperties;
import com.pro.framework.api.util.DateUtils;
import com.pro.framework.api.util.StrUtils;
import com.pro.framework.mtq.service.multiwrapper.entity.IMultiPageResult;
import com.pro.framework.mtq.service.multiwrapper.entity.IMultiReadService;
import com.pro.framework.mtq.service.multiwrapper.util.MultiClassRelationFactory;
import com.pro.framework.mtq.service.multiwrapper.util.MultiRelationCaches;
import com.pro.framework.mtq.service.multiwrapper.util.MultiTuple3;
import com.pro.framework.mtq.service.multiwrapper.util.MultiUtil;
import com.pro.framework.mtq.service.multiwrapper.wrapper.MultiWrapper;
import com.pro.framework.mtq.service.multiwrapper.wrapper.inner.MultiWrapperMainInner;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * R: Read（读取）
 * C: Create（创建）
 * U: Update（更新）
 * D: Delete（删除）
 */
@SuppressWarnings("unchecked")
public abstract class MultiBaseReadService<T> implements IMultiReadService<T> {
    @Autowired
    private IEntityProperties entityProperties;

    @Override
    public IMultiPageResult<T> selectPage(String entityClassName, IPageInput page, Map<String, Object> paramMap, TimeQuery timeQuery) {
        entityClassName = entityNameFilter(entityClassName);
        List<String> entityClassNameList = Arrays.stream(entityClassName.split(",")).collect(Collectors.toList());
        MultiWrapper<T> wrapper = new MultiWrapper<>(entityClassNameList);

        MultiWrapperMainInner<T, ?> wrapperMain = wrapper.getWrapperInner().getWrapperMain();
        if (timeQuery != null) {
            wrapperMain.ge(!MultiUtil.isEmpty(timeQuery.getStart()), timeQuery.getTimeColumn(), DateUtils.parseDateTime(timeQuery.getStart(), true));
            if (!MultiUtil.isEmpty(timeQuery.getEnd())) {
                wrapperMain.le(!MultiUtil.isEmpty(timeQuery.getEnd()), timeQuery.getTimeColumn(), DateUtils.parseDateTime(timeQuery.getEnd(), false));
            }
        }
        wrapperMain.setSelectFields(getSelectFields(wrapperMain.getClazz()));

        List<WhereDataUnit> whereDataUnits = page.getWhereDataUnits();
        if (whereDataUnits != null) {
            whereDataUnits.forEach(w -> {
                String propName = w.getPropName();
                if (null != propName && !propName.isEmpty() && null != w.getOpt() && null != w.getValues()) {
                    wrapperMain.addWhereTreeData(propName, w.getOpt(), w.getValues());
                }
            });
        }
        return MultiExecutor.page(page, wrapper.extendParams(paramMap));
    }

    /**
     * 分组求和
     */
    @Override
    public List<AggregateResult> selectCountSum(String entityClassName, Map<String, Object> paramMap, TimeQuery timeQuery, GroupBy groupBy) {
        entityClassName = entityNameFilter(entityClassName);
        MultiWrapper<?> wrapper = new MultiWrapper<>(entityClassName);
        wrapper.extendParams(paramMap);
        MultiWrapperMainInner<?, ?> wrapperMain = wrapper.getWrapperInner().getWrapperMain();
        wrapperMain.groupBy(groupBy.getGroupBys().toArray(String[]::new));
        wrapperMain.count();
        wrapperMain.countDistinct();
        wrapperMain.sumAll();
        wrapperMain
                .ge(!MultiUtil.isEmpty(timeQuery.getStart()), timeQuery.getTimeColumn(), DateUtils.parseDateTime(timeQuery.getStart(), true))
                .le(!MultiUtil.isEmpty(timeQuery.getEnd()), timeQuery.getTimeColumn(), DateUtils.parseDateTime(timeQuery.getEnd(), false));
        return MultiExecutor.aggregateList(wrapper);
    }

    @Override
    public T selectOneById(String entityClassName, Serializable id) {
        entityClassName = entityNameFilter(entityClassName);
        Class<T> beanClass = (Class<T>) MultiClassRelationFactory.INSTANCE.getEntityClass(entityClassName);
        return MultiExecutor.getById(beanClass, id);
    }

    @Override
    public T selectOne(String entityClassName, IPageInput pageInput, Map<String, Object> paramMap, TimeQuery timeQuery) {
        pageInput.setPageSize(1L);
        IMultiPageResult<T> pageRs = selectPage(entityClassName, pageInput, paramMap, timeQuery);
        return pageRs.getRecords().size() > 0 ? pageRs.getRecords().get(0) : null;
    }

    @Override
    public List<T> selectList(String entityClassName, Map<String, Object> paramMap, TimeQuery timeQuery, Long limit, List<String> selects, List<String> selectMores, List<String> selectLess, List<OrderItem> orderInfos) {
        entityClassName = entityNameFilter(entityClassName);
        List<String> entityClassNameList = Arrays.stream(entityClassName.split(",")).collect(Collectors.toList());
        if (!MultiUtil.isEmpty(timeQuery.getStart())) {
            paramMap.put(timeQuery.getTimeColumn(), "#ge#" + DateUtils.parseDateTime(timeQuery.getStart(), true));
        }
        if (!MultiUtil.isEmpty(timeQuery.getEnd())) {
            paramMap.put(timeQuery.getTimeColumn(), "#le#" + DateUtils.parseDateTime(timeQuery.getEnd(), false));
        }
        MultiWrapper<T> wrapper = new MultiWrapper<>(entityClassNameList);
        MultiWrapperMainInner<T, ?> wrapperMain = wrapper.getWrapperInner().getWrapperMain();
        if (limit != null) {
            wrapperMain.limit(0, limit);
        }
        wrapperMain.select(selects);
        wrapperMain.selectMores(selectMores);
        wrapperMain.selectLess(selectLess);
        wrapperMain.setOrderInfos(orderInfos);
        return MultiExecutor.list(wrapper.extendParams(paramMap));
    }

    private List<String> getSelectFields(Class<T> clazz) {
        Map<String, MultiTuple3<Field, Method, Method>> classInfos = MultiRelationCaches.getClassInfos(clazz);
        return classInfos.values().stream().map(MultiTuple3::getT1).map(Field::getName).collect(Collectors.toList());
    }

    private String entityNameFilter(String entityClassName) {
        return Arrays.stream(entityClassName.split(",")).map(c -> {
            Class<?> aClass = entityProperties.getEntityClassReplaceMap().get(StrUtils.firstToUpperCase(entityClassName));
            return null == aClass ? c : StrUtils.firstToLowerCase(aClass.getSimpleName());
        }).collect(Collectors.joining(","));
    }
}
