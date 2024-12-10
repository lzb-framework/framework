package com.pro.framework.api.database.page;

import com.pro.framework.api.database.AggregateResult;
import com.pro.framework.api.database.OrderItem;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页信息对象
 *
 * @author Administrator
 */
@Data
public class Page<T> implements IPage<T> {
    public static final Page<?> EMPTY = new Page<>();
    public Page() {
    }

    public Page(long currPage, long pageSize) {
        this.pageSize = pageSize;
        this.curPage = currPage;
    }

    /* ------------- 入参 ------------- */
    /**
     * 每页大小
     */
    private Long pageSize = 10L;
    /**
     * 当前第几页
     */
    private Long curPage = 1L;


    /* ------------- 返回 ------------- */
    /**
     * 分页列表内容 <必有>
     */
    private List<T> records;
    /**
     * 总条数 <必有>
     */
    private Long total;
    /**
     * 统计信息
     * 例如
     * {    "sum":{"user__userWallet.enableBalance":"1000"}   }
     */
    private AggregateResult aggregateResult;
    /**
     * 附加信息
     */
    private Object attach;

    //排序
    private List<OrderItem> orders = new ArrayList<>();
}
