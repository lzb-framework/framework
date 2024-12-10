package com.pro.framework.mtq.service.multiwrapper.entity;

import com.pro.framework.api.database.AggregateResult;

import java.util.List;

/**
 * 分页信息对象
 *
 * @author Administrator
 */
public interface IMultiPageResult<T> {
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

    void setTotalPages(Long totalPages);
    Long getTotalPages();

}
