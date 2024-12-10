package com.pro.framework.api.database;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupBy {
    /**
     * 按哪些属性分组a
     */
    private List<String> groupBys = new ArrayList<>();
}
