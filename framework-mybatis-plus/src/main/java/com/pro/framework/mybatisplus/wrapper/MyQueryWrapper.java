package com.pro.framework.mybatisplus.wrapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

public class MyQueryWrapper<T> extends QueryWrapper<T> {
    public MyQueryWrapper() {
          super();
      }
    @Override
    public LambdaQueryWrapper<T> lambda() {
        return new MyLambdaQueryWrapper<>(getEntity(), getEntityClass(), paramNameSeq, paramNameValuePairs,
                expression, paramAlias, lastSql, sqlComment, sqlFirst);
    }
}
