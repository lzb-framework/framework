package com.pro.framework.mtq.service.multiwrapper.entity;

import com.pro.framework.api.database.AggregateResult;
import com.pro.framework.api.database.GroupBy;
import com.pro.framework.api.database.OrderItem;
import com.pro.framework.api.database.TimeQuery;
import com.pro.framework.api.database.page.IPageInput;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */
public interface IMultiService<T> {
    boolean save(T entity);

    // 保存或更新实体
    boolean saveOrUpdate(T entity) ;
    boolean updateById(T entity) ;
    // 根据主键删除实体
    boolean removeById(Class<?> clazz, Serializable id);

    // 根据主键删除实体
    boolean saveBatch(Collection<T> entityList);

    boolean removeBatchByIds(Collection<?> list);

    boolean updateBatchById(Collection<T> list);

    IMultiPageResult<T> selectPage(String entityClassName, IPageInput pageInput, Map<String, Object> paramMap, TimeQuery timeQuery);

    T selectOneById(String entityClassName, Serializable id);

//    List<T> selectList(String entityClassName, Map<String, Object> paramMap, TimeQuery timeQuery, List<String> selects, List<String> selectMores, List<String> selectLess);
    List<T> selectList(String entityClassName, Map<String, Object> paramMap, TimeQuery timeQuery, Long limit, List<String> selects, List<String> selectMores, List<String> selectLess, List<OrderItem> orderInfos);

    List<AggregateResult> selectCountSum(String entityClassName, Map<String, Object> paramMap, TimeQuery timeQuery, GroupBy groupBy);

    T selectOne(String entityClassName, IPageInput pageInput, Map<String, Object> paramMap, TimeQuery timeQuery);
}
