package com.pro.framework.mybatisplus.wrapper;


import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 仅仅为了 重写 lambda() { return new MyLambdaUpdateWrapper }
 * 为了兼容不得不复制那么多代码
 *
 * @param <T>
 */
public class MyUpdateWrapper<T> extends UpdateWrapper<T> {
    /**
     * SQL 更新字段内容，例如：name='1', age=2
     */
    private List<String> sqlSet;

    public MyUpdateWrapper() {
        // 如果无参构造函数，请注意实体 NULL 情况 SET 必须有否则 SQL 异常
        this(null);
    }

    public MyUpdateWrapper(T entity) {
        super(entity);
        this.sqlSet = new ArrayList<>();
    }

    /**
     * 返回一个支持 lambda 函数写法的 wrapper
     */
    @Override
    public LambdaUpdateWrapper<T> lambda() {
        return new MyLambdaUpdateWrapper<>(getEntity(), getEntityClass(), sqlSet, paramNameSeq, paramNameValuePairs,
                expression, paramAlias, lastSql, sqlComment, sqlFirst);
    }

    @Override
    public String getSqlSet() {
        if (CollectionUtils.isEmpty(sqlSet)) {
            return null;
        }
        return String.join(Constants.COMMA, sqlSet);
    }

    @Override
    public UpdateWrapper<T> set(boolean condition, String column, Object val, String mapping) {
        return maybeDo(condition, () -> {
            String sql = formatParam(mapping, val);
            sqlSet.add(column + Constants.EQUALS + sql);
        });
    }

    @Override
    public UpdateWrapper<T> setSql(boolean condition, String setSql, Object... params) {
        if (condition && StringUtils.isNotBlank(setSql)) {
            sqlSet.add(formatSqlMaybeWithParam(setSql, params));
        }
        return typedThis;
    }

    @Override
    public void clear() {
        super.clear();
        sqlSet.clear();
    }
}
