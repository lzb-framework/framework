package com.pro.framework.mtq.service.multiwrapper.dto;

import com.pro.framework.api.database.AggregateResult;
import com.pro.framework.mtq.service.multiwrapper.entity.IMultiPageResult;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 分页信息对象
 *
 * @author Administrator
 */
@Data
public class MultiPageResult<T> implements IMultiPageResult<T> {
    public static final MultiPageResult<?> EMPTY = new MultiPageResult<>();

    /* ------------- 返回 ------------- */
    /**
     * 分页列表内容 <必有>
     */
    private List<T> records = Collections.emptyList();
    /**
     * 总条数 <必有>
     */
    private Long total;
    /**
     * 总页数
     */
    private Long totalPages;
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
}
