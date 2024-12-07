package com.pro.framework.api.database.page;

import com.pro.framework.api.database.OrderItem;
import com.pro.framework.api.database.wheredata.WhereDataUnit;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页信息对象
 *
 * @author Administrator
 */
@Data
public class PageInput implements IPageInput {
    public PageInput() {
    }

    public PageInput(long currPage, long pageSize) {
        this.pageSize = pageSize;
        this.curPage = currPage;
    }

    /* ------------- 入参 ------------- */
    /**
     * 每页大小
     */
    private Long pageSize = 20L;
    /**
     * 当前第几页
     */
    private Long curPage = 1L;


    //排序
    private List<OrderItem> orders = new ArrayList<>();


    /**
     * 自定义过滤条件
     */
    private List<WhereDataUnit> whereDataUnits = new ArrayList<>();
}
