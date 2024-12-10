package com.pro.framework.mtq.service.multiwrapper;

import com.pro.framework.api.database.AggregateResult;
import com.pro.framework.api.database.page.IPageInput;
import com.pro.framework.mtq.service.multiwrapper.entity.IMultiPageResult;
import com.pro.framework.mtq.service.multiwrapper.executor.MultiExecutorInner;
import com.pro.framework.mtq.service.multiwrapper.util.MultiException;
import com.pro.framework.mtq.service.multiwrapper.wrapper.MultiWrapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

/**
 * @author Administrator
 */
@Slf4j
@Component
public class MultiExecutor {

    @SneakyThrows
    public static <MAIN> List<MAIN> list(MultiWrapper<MAIN> wrapper) {
        return MultiExecutorInner.list(wrapper.getWrapperInner());
    }

    @SneakyThrows
    public static <MAIN> MAIN getById(Class<MAIN> mainClass, Serializable id) {
        return MultiExecutorInner.getById(mainClass, id);
    }

    @SneakyThrows
    public static <MAIN> MAIN getOne(MultiWrapper<MAIN> wrapper) {
        return MultiExecutorInner.getOne(wrapper.getWrapperInner());
    }

    /**
     * 执行分页查询
     *
     * @param pageInput    分页信息
     * @param wrapper 表信息,聚合信息,过滤条件...
     * @return 聚合查询结果 例如 {"sum":{"userAndUserStaff.balance":"100.00"}}
     */
    @SneakyThrows
    public static <MAIN> IMultiPageResult<MAIN> page(IPageInput pageInput, MultiWrapper<MAIN> wrapper) {
//        MultiPage<MAIN> page = new MultiPage<>();
//        page.setCurPage(pageInput.getCurPage());
//        page.setPageSize(pageInput.getPageSize());
//        page.setOrders(pageInput.getOrders());

        // 分页查询
        IMultiPageResult<MAIN> pageRs;
        try {
            pageRs = MultiExecutorInner.page(pageInput, wrapper.getWrapperInner());
        } catch (Exception e) {
            if (e instanceof NullPointerException) {
                log.error("", e);
            } else {
                String sql = wrapper.getWrapperInner().computeSql();
//            log.error(MultiUtil.logThrowable(e));
                log.error("sql =\n {}", sql, e);
            }
            Throwable t = null == e.getCause() ? e : e.getCause();
            throw new MultiException(t);

        }
        return pageRs;
    }


//    private static String camelToUnderline(String column) {
//        String[] split = column.split("\\.");
//        if (split.length == 1) {
//            return MultiUtil.camelToUnderline(column);
//        }
//        return split[0] + "." + MultiUtil.camelToUnderline(split[1]);
//    }

    /**
     * 执行聚合查询
     *
     * @param wrapper 表信息,聚合信息,过滤条件...
     * @return 聚合查询结果 例如 {"sum":{"userAndUserStaff.balance":"100.00"}}
     */
    @SneakyThrows
    public static <MAIN> AggregateResult aggregate(MultiWrapper<MAIN> wrapper) {
        return MultiExecutorInner.aggregate(wrapper.getWrapperInner());
    }

    /**
     * 执行聚合查询
     *
     * @param wrapper 表信息,聚合信息,过滤条件...
     * @return 聚合查询结果 例如 {"sum":{"userAndUserStaff.balance":"100.00"}}
     */
    @SneakyThrows
    public static <MAIN> List<AggregateResult> aggregateList(MultiWrapper<MAIN> wrapper) {
        return MultiExecutorInner.aggregateList(wrapper.getWrapperInner());
    }
}
