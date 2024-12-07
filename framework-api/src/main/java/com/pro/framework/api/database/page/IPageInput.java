package com.pro.framework.api.database.page;

import com.pro.framework.api.database.OrderItem;
import com.pro.framework.api.database.wheredata.WhereDataUnit;

import java.util.List;

/**
 * 分页信息对象
 *
 * @author Administrator
 */
public interface IPageInput {
    /* ------------- 入参 ------------- */

    /**
     * 每页大小
     *
     * @return 每页大小
     */
    Long getPageSize();
    void setPageSize(Long pageSize);

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
    void setOrders(List<OrderItem> orders);
    /**
     * 自定义过滤
     */
    List<WhereDataUnit> getWhereDataUnits();
}
