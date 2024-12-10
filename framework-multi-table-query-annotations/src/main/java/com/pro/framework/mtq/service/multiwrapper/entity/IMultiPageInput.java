package com.pro.framework.mtq.service.multiwrapper.entity;

import com.pro.framework.api.database.OrderItem;
import com.pro.framework.api.database.wheredata.WhereDataUnit;

import java.util.List;

/**
 * 分页信息对象
 *
 * @author Administrator
 */
public interface IMultiPageInput {
    /* ------------- 入参 ------------- */

    /**
     * 每页大小
     *
     * @return 每页大小
     */
    Long getPageSize();

    /**
     * 当前第几页
     *
     * @return 当前第几页
     */
    Long getCurPage();

    /**
     * 排序
     */
    List<OrderItem> getOrders();

    /**
     * 自定义过滤
     */
    List<WhereDataUnit> getWhereDataUnits();
}
