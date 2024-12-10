package com.pro.framework.mtq.service.multiwrapper.entity;

import com.pro.framework.api.database.AggregateResult;
import com.pro.framework.api.database.GroupBy;
import com.pro.framework.api.database.OrderItem;
import com.pro.framework.api.database.TimeQuery;
import com.pro.framework.api.database.page.IPageInput;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface IMultiReadService<T> {
    IMultiPageResult<T> selectPage(String entityClassName, IPageInput page, Map<String, Object> paramMap, TimeQuery timeQuery);

//    List<T> selectList(String entityClassName, Map<String, Object> paramMap, TimeQuery timeQuery);
    List<T> selectList(String entityClassName, Map<String, Object> paramMap, TimeQuery timeQuery, Long limit, List<String> selects, List<String> selectMores, List<String> selectLess, List<OrderItem> orderInfos);

    List<AggregateResult> selectCountSum(String entityClassName, Map<String, Object> paramMap, TimeQuery timeQuery, GroupBy groupBy);

    T selectOneById(String entityClassName, Serializable id);
    T selectOne(String entityClassName, IPageInput pageInput, Map<String, Object> paramMap, TimeQuery timeQuery);

}
