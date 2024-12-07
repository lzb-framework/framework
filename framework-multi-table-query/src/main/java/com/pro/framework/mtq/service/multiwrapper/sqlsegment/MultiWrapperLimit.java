package com.pro.framework.mtq.service.multiwrapper.sqlsegment;


import com.pro.framework.mtq.service.multiwrapper.constant.MultiConstant;
import com.pro.framework.api.database.OrderItem;
import com.pro.framework.mtq.service.multiwrapper.util.MultiTuple2;
import com.pro.framework.mtq.service.multiwrapper.util.MultiUtil;
import com.pro.framework.mtq.service.multiwrapper.util.mybatisplus.MultiFunction;
import com.pro.framework.mtq.service.multiwrapper.util.mybatisplus.SerializedLambdaData;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@SuppressWarnings({"unchecked", "AlibabaAbstractMethodOrInterfaceMethodMustUseJavadoc"})
public interface MultiWrapperLimit<T, Wrapper extends MultiWrapperLimit<T, Wrapper>> {

    String getClassName();

    void setClassName(String className);

    /***/
    void setLimitOffset(Long limitOffset);

    Long getLimitOffset();

    void setLimitSize(Long limitSize);

    Long getLimitSize();

    void setOrderInfos(List<OrderItem> orderInfos);

    List<OrderItem> getOrderInfos();

    default Wrapper limit(long offset, long size) {
        setLimitOffset(offset);
        setLimitSize(size);
        return (Wrapper) this;
    }

    default Wrapper limit(long offset, long size, List<OrderItem> orderInfos) {
        setLimitOffset(offset);
        setLimitSize(size);
        setOrderInfos(orderInfos);
        return (Wrapper) this;
    }

    default <VAL> Wrapper asc(MultiFunction<T, VAL>... propFuncs) {
        this.fillOrder(true, propFuncs);
        return (Wrapper) this;
    }

    default <VAL> Wrapper desc(MultiFunction<T, VAL>... propFuncs) {
        this.fillOrder(false, propFuncs);
        return (Wrapper) this;
    }


    default <VAL> void fillOrder(Boolean asc, MultiFunction<T, VAL>[] propFuncs) {
        MultiTuple2<String, List<String>> result = MultiUtil.calcMultiFunctions(SerializedLambdaData::getPropName, propFuncs);
        if (null == getClassName()) {
            this.setClassName(result.getT1());
        }
        this.setOrderInfos(result.getT2().stream().map(prop -> new OrderItem(prop,asc)).collect(Collectors.toList()));
    }

    default String getSqlFrom(String className) {
        // String orderByMain = "";
        // String orderByMain = String.join(",", getOrderInfos());
        // orderByMain = orderByMain.length() == 0 ? orderByMain : " order by " + orderByMain;
        // goods_order goodsOrder
        String mainInfo = MultiUtil.camelToUnderline(className) + " " + className;
        return mainInfo;
    }

    default String getSqlLimit() {
        if (null == getLimitSize()) {
            return "";
        }
        return " limit " + valToStr(getLimitOffset(), ",") + valToStr(getLimitSize(), MultiConstant.Strings.EMPTY);
    }

    /**
     * long转字符串
     */
    default String valToStr(Long l, String appendLast) {
        return l == null ? MultiConstant.Strings.EMPTY : l + appendLast;
    }


}
