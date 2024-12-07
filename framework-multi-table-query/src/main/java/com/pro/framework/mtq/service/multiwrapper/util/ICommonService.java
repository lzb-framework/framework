package com.pro.framework.mtq.service.multiwrapper.util;

public interface ICommonService<T> {
    Integer insertOrUpdate(T entity);
}
