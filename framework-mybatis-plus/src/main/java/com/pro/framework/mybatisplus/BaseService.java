package com.pro.framework.mybatisplus;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ClassUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pro.framework.api.database.AggregateResult;
import com.pro.framework.api.database.GroupBy;
import com.pro.framework.api.database.OrderItem;
import com.pro.framework.api.database.TimeQuery;
import com.pro.framework.api.database.page.IPageInput;
import com.pro.framework.api.database.page.PageInput;
import com.pro.framework.api.model.IModel;
import com.pro.framework.api.structure.Tuple2;
import com.pro.framework.api.util.CollUtils;
import com.pro.framework.api.util.StrUtils;
import com.pro.framework.mtq.service.multiwrapper.entity.IMultiPageResult;
import com.pro.framework.mtq.service.multiwrapper.entity.IMultiReadService;
import com.pro.framework.mtq.service.multiwrapper.entity.IMultiService;
import com.pro.framework.mybatisplus.wrapper.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 全量定制 MyXXXWrapper,主要为了UserAi,User等定制实体的ServiceImpl,不会报错
 */
public abstract class BaseService<M extends BaseMapper<T>, T extends IModel> extends ServiceImpl<M, T> implements IMultiService<T> {

    protected final String entityName = StrUtils.firstToLowerCase(getEntityClass().getSimpleName());
    @Autowired
    protected IMultiReadService<T> readService;

    @Override
    public LambdaQueryChainWrapper<T> lambdaQuery() {
        return new MyLambdaQueryChainWrapper<>(baseMapper);
    }

    @Override
    public LambdaUpdateChainWrapper<T> lambdaUpdate() {
        return new MyLambdaUpdateChainWrapper<>(baseMapper);
    }

    // 查询器
    public QueryWrapper<T> qw() {
        return new MyQueryWrapper<>();
    }

    // 更新器
    public UpdateWrapper<T> uw() {
        return new MyUpdateWrapper<>();
    }

    public T getFirst(T t, SFunction<T, Object> prop) {
        return this.lambdaQuery().setEntity(t).orderByAsc(prop).last("limit 1").one();
    }

    public T getOne(T t) {
        return this.lambdaQuery().setEntity(t).last("limit 1").one();
    }

    public List<T> list(T t) {
        return this.lambdaQuery().setEntity(t).list();
    }

    public Map<Long, T> idMap(Collection<? extends Serializable> ids) {
        return ids.isEmpty() ? Collections.emptyMap() : this.listByIds(ids).stream().collect(Collectors.toMap(T::getId, o -> o));
    }


    /**
     * 加减某些字段
     * update user set login_times = login_times + 1
     */
    @SuppressWarnings("unchecked")
    public boolean addIncreaseField(Serializable id, SFunction<T, ?> prop, Number number) {
        return this.addIncreaseField((query) -> query.eq(T::getId, id), new Tuple2<>(prop, number));
    }

    /**
     * 加减某些字段
     * update user set login_times = login_times + 1
     */
    public boolean addIncreaseField(
            Consumer<LambdaUpdateChainWrapper<T>> query,
            Tuple2<SFunction<T, ?>, Number>... addNums
    ) {
        // 查询条件
        LambdaUpdateChainWrapper<T> uw = lambdaUpdate();
        query.accept(uw);
        // 累加属性
        uw.setSql(
                Arrays.stream(addNums).map(addNum -> {
                    String fieldName = MybatisPlusUtil.getFieldName(addNum.getT1());
                    Number number = addNum.getT2();
                    return fieldName + " = " + fieldName + " + " + number;

                }).collect(Collectors.joining(",")));
        return uw.update();
    }

    /**
     * 加减某个字段
     * update user set login_times = login_times + 1
     *
     * @return
     */
    public boolean addIncreaseField(Serializable id, String fieldName, Number number) {
        if (StrUtils.isBlank(fieldName) || null == number) {
            return false;
        }
        fieldName = StrUtils.camelToUnderline(fieldName);
        TableInfo tableInfo = TableInfoHelper.getTableInfo(entityClass);
        Assert.notNull(tableInfo, "error: can not execute. because can not find cache of TableInfo for entity!");
        String keyProperty = tableInfo.getKeyProperty();
        return addIncreaseField(keyProperty, id, fieldName, number);
    }

    private boolean addIncreaseField(String keyField, Serializable keyValue, String fieldName, Number number) {
        return this.update().setSql(fieldName + " = " + fieldName + " + " + number).eq(keyField, keyValue).update();
    }

    public List<T> count(T params) {
        return this.count(params, null, null, null, null);
    }

    public List<T> count(T params, List<String> amountFields, List<String> groupByFields, String lastSql, TimeQuery timeQuery) {
        QueryWrapper<T> wrapper = MybatisPlusUtil.create(params, timeQuery);
        if (amountFields == null) {
            //默认查询所有可聚合属性
            amountFields = Arrays.stream(ClassUtil.getDeclaredFields(params.getClass()))
                    .filter(f -> {
                        Class<?> type = f.getType();
                        return (!Modifier.isTransient(f.getModifiers()))
                                && (Integer.class.equals(type) || (Long.class.equals(type) && !f.getName().toLowerCase().endsWith("id")) || BigDecimal.class.equals(type))
                                ;
                    }).map(f -> {
                        String field = StrUtils.camelToUnderline(f.getName());
                        return "sum(" + field + ") " + field;
                    }).collect(Collectors.toList());
        } else {
            amountFields = new ArrayList<>(amountFields);
        }

        amountFields.add("count(1) count");
        wrapper.select(amountFields.toArray(String[]::new));
        if (CollUtil.isNotEmpty(groupByFields)) {
            amountFields.addAll(groupByFields);
            wrapper.groupBy(groupByFields);
        }
        wrapper.last(StrUtils.isNotBlank(lastSql), lastSql);
        return this.list(wrapper).stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    public IMultiPageResult<T> selectPage(IPageInput pageInput, Map<String, Object> paramMap, TimeQuery timeQuery) {
        return this.selectPage(entityName, pageInput, paramMap, timeQuery);
    }

    public T selectOneById(Serializable id) {
        return this.selectOneById(entityName, id);
    }

    public T selectOne(String entityClassName, IPageInput pageInput, Map<String, Object> paramMap, TimeQuery timeQuery) {
        if (pageInput == null) {
            pageInput = new PageInput();
        }
        pageInput.setPageSize(1L);
        IMultiPageResult<T> pageRs = this.selectPage(entityClassName, pageInput, paramMap, timeQuery);
        return pageRs.getRecords().size() > 0 ? pageRs.getRecords().get(0) : null;
    }

    public List<T> selectList(Map<String, Object> paramMap, TimeQuery timeQuery) {
        return this.selectList(entityName, paramMap, timeQuery, null, null, null, null, null);
    }

    public List<AggregateResult> selectCountSum(Map<String, Object> paramMap, TimeQuery timeQuery, GroupBy groupBy) {
        return this.selectCountSum(entityName, paramMap, timeQuery, groupBy);
    }

    /**
     * 查询并,按唯一属性,转成Map <key,Entity> map
     *
     * @param queryProp 查询条件
     * @param keyFun    唯一属性
     * @param <KEY>     唯一属性类型泛型
     * @return <key,Entity> map
     */
    public <PROP, KEY> Map<KEY, T> getMap(SFunction<T, PROP> queryProp, Collection<PROP> propValues, Function<T, KEY> keyFun) {
        return getMap(qw -> qw.in(queryProp, propValues), keyFun);
    }

    public <PROP, KEY> Map<KEY, T> getMap(String queryProp, Collection<PROP> propValues, Function<T, KEY> keyFun) {
        QueryWrapper<T> qw = this.qw();
        qw.in(queryProp, propValues);
        // 查询并分组
        return CollUtils.listToMap(this.list(qw), keyFun);
    }

    public <KEY> Map<KEY, T> getMap(Consumer<LambdaQueryWrapper<T>> qwConsumer, Function<T, KEY> keyFun) {
        LambdaQueryWrapper<T> qw = MyWrappers.lambdaQuery();
//        LambdaQueryChainWrapper<T> qw = this.lambdaQuery();
        qwConsumer.accept(qw);
        // 查询并分组
        return CollUtils.listToMap(this.list(qw), keyFun);
    }

    @Override
    public boolean removeById(Class<?> clazz, Serializable id) {
        return this.removeById(id);
    }

    @Override
    public IMultiPageResult<T> selectPage(String entityClassName, IPageInput pageInput, Map<String, Object> paramMap, TimeQuery timeQuery) {
        return readService.selectPage(entityName, pageInput, paramMap, timeQuery);
    }

    @Override
    public T selectOneById(String entityClassName, Serializable id) {
        return readService.selectOneById(entityName, id);
    }

    @Override
    public List<T> selectList(String entityClassName, Map<String, Object> paramMap, TimeQuery timeQuery, Long limit, List<String> selects, List<String> selectMores, List<String> selectLess, List<OrderItem> orderInfos) {
        return readService.selectList(entityName, paramMap, timeQuery, limit, selects, selectMores, selectLess, orderInfos);
    }

    @Override
    public List<AggregateResult> selectCountSum(String entityClassName, Map<String, Object> paramMap, TimeQuery timeQuery, GroupBy groupBy) {
        return readService.selectCountSum(entityName, paramMap, timeQuery, groupBy);
    }

    @Override
    public boolean save(T entity) {
        return super.save(entity);
    }

    @Override
    public boolean saveBatch(Collection<T> entityList) {
        return super.saveBatch(entityList);
    }

    @Override
    public boolean removeBatchByIds(Collection<?> list) {
        return super.removeBatchByIds(list);
    }

    @Override
    public boolean updateById(T entity) {
        return super.updateById(entity);
    }

    @Override
    public boolean updateBatchById(Collection<T> entityList) {
        return super.updateBatchById(entityList);
    }
}
