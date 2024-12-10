package com.pro.framework.api.database.page;

import com.pro.framework.api.database.AggregateResult;
import com.pro.framework.api.database.OrderItem;

import java.util.List;

/**
 * 分页信息对象
 *
 * @author Administrator
 */
public interface IPage<T> {
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

    /* ------------- 返回 ------------- */

    /**
     * 分页列表内容 <必有>
     *
     * @return 分页列表内容
     */
    List<T> getRecords();

    /**
     * 总条数 <必有>
     *
     * @return 总条数
     */
    Long getTotal();

    /**
     * 统计信息
     * 例如 {'user__userWallet.enableBalance':"1000"}
     *
     * @return 统计信息
     */
    AggregateResult getAggregateResult();

    /**
     * 附加信息
     *
     * @return 附加信息
     */
    Object getAttach();

    /**
     * setPageSize
     * @param pageSize pageSize
     */
    void setPageSize(Long pageSize);
    /**
     * setCurrPage
     * @param curPage currPage
     */
    void setCurPage(Long curPage);
    /**
     * setRecords
     * @param records records
     */
    void setRecords(List<T> records);
    /**
     * @param total total
     * setAttach
     */
    void setTotal(Long total);
    /**
     * @param aggregateResult aggregateResult
     * setAttach
     */
    void setAggregateResult(AggregateResult aggregateResult);
    /**
     * @param attach attach
     * setAttach
     */
    void setAttach(Object attach);


    void setOrders(List<OrderItem> items);

}
