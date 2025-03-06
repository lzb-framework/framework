package com.pro.framework.api.util;

import com.pro.framework.api.database.page.PageResultCursor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PageUtil {
    /**
     * 游标分页 一直下一页直到查询完 拼接一起返回
     */
    public static <T> List<T> loadAllCursorPage(Function<String, PageResultCursor<T>> pageFun, Function<PageResultCursor<T>, Boolean> breakFun) {
        // 初始化
        List<T> list = new ArrayList<>();
        String nextPageToken = null;
        while (true) {
            // 获取当前页数据
            PageResultCursor<T> pageResult = pageFun.apply(nextPageToken);
            List<T> subList = pageResult.getList();
            nextPageToken = pageResult.getNextPageToken();
            // 添加当前页数据到结果列表
            list.addAll(subList);
            if (breakFun.apply(pageResult)) {
                break;
            }
        }
        return list;
    }

    public static <T> List<T> loadAllCursorPage(Function<String, PageResultCursor<T>> pageFun, Integer pageSize) {
        return loadAllCursorPage(pageFun, rs -> rs.getList().size() < pageSize || rs.getNextPageToken() == null);
    }
}
