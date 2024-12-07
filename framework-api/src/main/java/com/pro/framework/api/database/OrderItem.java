package com.pro.framework.api.database;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 排序元素载体
 *
 * @author HCL
 * Create at 2019/5/27
 */
@Data

@ToString
@NoArgsConstructor
public class OrderItem implements Serializable {

//    private static final long serialVersionUID = 1L;
    /**
     * 需要进行排序的字段
     */
    private String column;

    /**
     * 是否正序排列，默认 true
     */
    private Boolean asc = true;


    public static OrderItem asc(String column) {
        return new OrderItem(column, true);
    }

    public static OrderItem desc(String column) {
        return new OrderItem(column, false);
    }

    public static List<OrderItem> ascs(String... columns) {
        return Arrays.stream(columns).map(OrderItem::asc).collect(Collectors.toList());
    }

    public OrderItem(String column, Boolean asc) {
        this.asc = asc;
        this.column = column;
    }

    public static List<OrderItem> descs(String... columns) {
        return Arrays.stream(columns).map(OrderItem::desc).collect(Collectors.toList());
    }

//    private static OrderItem build(String column, boolean asc) {
//        OrderItem item = new OrderItem();
//        item.setColumn(column);
//        item.setAsc(asc);
//        return item;
//    }

    public String toSql() {
        return column + (asc ? " asc" : " desc");
    }
}
