package com.pro.framework.mybatisplus;


import com.pro.framework.api.IReloadService;
import com.pro.framework.api.database.AggregateResult;
import com.pro.framework.api.database.GroupBy;
import com.pro.framework.api.database.OrderItem;
import com.pro.framework.api.database.TimeQuery;
import com.pro.framework.api.database.page.IPageInput;
import com.pro.framework.api.model.IModel;
import com.pro.framework.api.util.JSONUtils;
import com.pro.framework.mtq.service.multiwrapper.entity.IMultiBaseService;
import com.pro.framework.mtq.service.multiwrapper.entity.IMultiClassRelationService;
import com.pro.framework.mtq.service.multiwrapper.entity.IMultiPageResult;
import com.pro.framework.mtq.service.multiwrapper.entity.IMultiService;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * R: Read（读取）
 * C: Create（创建）
 * U: Update（更新）
 * D: Delete（删除）
 */
@SuppressWarnings("unchecked")
@Component
@AllArgsConstructor
public class CRUDService<T extends IModel> implements IReloadService {
    public static final Map<String, IMultiService<?>> serverMap = new ConcurrentHashMap<>(1024);

    private ApplicationContext applicationContext;
    private IMultiBaseService<?> multiBaseService;
    private IMultiClassRelationService<?> multiClassRelationService;

    public IMultiPageResult<T> selectPage(String entityClassName, IPageInput pageInput, Map<String, Object> paramMap, TimeQuery timeQuery) {
        return (IMultiPageResult<T>) this.getService(entityClassName).selectPage(entityClassName, pageInput, paramMap, timeQuery);
    }

    public List<AggregateResult> selectCountSum(String entityClassName, Map<String, Object> paramMap, TimeQuery timeQuery, GroupBy groupBy) {
        return this.getService(entityClassName).selectCountSum(entityClassName, paramMap, timeQuery, groupBy);
    }

    public List<T> selectList(String entityClassName, Map<String, Object> paramMap, TimeQuery timeQuery, Long limit, List<String> selects, List<String> selectMores, List<String> selectLess, List<OrderItem> orderInfos) {
        return (List<T>) this.getService(entityClassName).selectList(entityClassName, paramMap, timeQuery, limit, selects, selectMores, selectLess, orderInfos);
    }

    public T selectOne(String entityClassName, IPageInput pageInput, Map<String, Object> paramMap, TimeQuery timeQuery) {
        return (T) this.getService(entityClassName).selectOne(entityClassName, pageInput, paramMap, timeQuery);
    }

    public T selectOneById(String entityClassName, Serializable id) {
        return (T) this.getService(entityClassName).selectOneById(entityClassName, id);
    }


    public void insert(String entityClassName, T entity) {
        getService(entityClassName).save(entity);
    }

    public T insert(String entityClassName, String entityStr) {
        T entity = this.strToEntity(entityClassName, entityStr);
        this.getService(entityClassName).save(entity);
        return entity;
    }

    public T insertOrUpdate(String entityClassName, String entityStr) {
        T entity = this.strToEntity(entityClassName, entityStr);
        this.getService(entityClassName).saveOrUpdate(entity);
        return entity;
    }

    public Boolean insertOrUpdate(String entityClassName, T entity) {
        return getService(entityClassName).saveOrUpdate(entity);
    }


    public void updateById(String entityClassName, T entity) {
        getService(entityClassName).updateById(entity);
    }

    public void updateById(String entityClassName, String entityStr) {
        getService(entityClassName).updateById(this.strToEntity(entityClassName, entityStr));
    }


    public void delete(String entityClassName, Long id) {
        getService(entityClassName).removeById(multiClassRelationService.getClass(entityClassName), id);
    }

    // 获取服务
    private <T extends IModel> IMultiService<T> getService(String entityClassName) {
        return (IMultiService<T>) serverMap.computeIfAbsent(entityClassName + "Service", this::getBaseService);
    }

    private <T extends IModel> IMultiService<T> getBaseService(String beanName) {
        boolean hasService = applicationContext.containsBean(beanName);
        if (hasService) {
            return (IMultiService<T>) applicationContext.getBean(beanName);
        } else {
            return (IMultiService<T>) multiBaseService;
        }
    }

    // 字符串转实体
    private T strToEntity(String entityClassName, String entityStr) {
        return (T) JSONUtils.fromString(entityStr, multiClassRelationService.getClass(entityClassName));
    }

    @Override
    public void reload() {
        CRUDService.serverMap.clear();
    }
}
