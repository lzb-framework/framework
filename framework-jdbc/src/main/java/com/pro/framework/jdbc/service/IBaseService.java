package com.pro.framework.jdbc.service;

import com.pro.framework.api.database.page.IPageInput;
import com.pro.framework.api.database.page.Page;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @author Administrator
 */
public interface IBaseService<T> {
    List<T> getList(T entity);

    List<T> getList(T entity, IPageInput pageInput);

    Page<T> getPage(T entity, IPageInput pageInput);

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
}
