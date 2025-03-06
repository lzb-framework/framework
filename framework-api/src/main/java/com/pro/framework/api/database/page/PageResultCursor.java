package com.pro.framework.api.database.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResultCursor<T> {
    List<T> list;
//    Integer total;
    String nextPageToken;
}
