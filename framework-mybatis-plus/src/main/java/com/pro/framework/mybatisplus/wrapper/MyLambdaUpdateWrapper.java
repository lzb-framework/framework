package com.pro.framework.mybatisplus.wrapper;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.support.ColumnCache;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.pro.framework.api.util.StrUtils;
import org.apache.ibatis.reflection.property.PropertyNamer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 只为了重写 getColumnCache() 方法让他查不到实体也不报错
 *
 * @param <T>
 */
public class MyLambdaUpdateWrapper<T> extends LambdaUpdateWrapper<T> {
    private Map<String, ColumnCache> columnMap = new HashMap<>();
    private boolean initColumnMap = false;
    /**
     * SQL 更新字段内容，例如：name='1', age=2
     */
    private List<String> sqlSet;

    public MyLambdaUpdateWrapper() {
        this((T) null);
    }

    public MyLambdaUpdateWrapper(T entity) {
        super.setEntity(entity);
        super.initNeed();
        this.sqlSet = new ArrayList<>();
    }

    public MyLambdaUpdateWrapper(Class<T> entityClass) {
        super.setEntityClass(entityClass);
        super.initNeed();
        this.sqlSet = new ArrayList<>();
    }

    MyLambdaUpdateWrapper(T entity, Class<T> entityClass, List<String> sqlSet, AtomicInteger paramNameSeq,
                           Map<String, Object> paramNameValuePairs, MergeSegments mergeSegments, SharedString paramAlias,
                           SharedString lastSql, SharedString sqlComment, SharedString sqlFirst) {
           super.setEntity(entity);
           super.setEntityClass(entityClass);
           this.sqlSet = sqlSet;
           this.paramNameSeq = paramNameSeq;
           this.paramNameValuePairs = paramNameValuePairs;
           this.expression = mergeSegments;
           this.paramAlias = paramAlias;
           this.lastSql = lastSql;
           this.sqlComment = sqlComment;
           this.sqlFirst = sqlFirst;
       }

    @Override
    protected ColumnCache getColumnCache(SFunction<T, ?> column) {
        LambdaMeta meta = com.baomidou.mybatisplus.core.toolkit.LambdaUtils.extract(column);
        String fieldName = PropertyNamer.methodToProperty(meta.getImplMethodName());
        Class<?> instantiatedClass = meta.getInstantiatedClass();
        /**
         * 整个系列列,仅仅为了重写 LambdaUtils.getColumnMap 达到效果: 在userServiceAi调用userService的方法时,不报错!
         */
        tryInitCache(instantiatedClass);
        return getColumnCache(fieldName, instantiatedClass);
    }

    private void tryInitCache(Class<?> lambdaClass) {
        if (!initColumnMap) {
            final Class<T> entityClass = getEntityClass();
            if (entityClass != null) {
                lambdaClass = entityClass;
            }
            columnMap = ObjectUtil.defaultIfNull(LambdaUtils.getColumnMap(lambdaClass), new HashMap<>());
            //            Assert.notNull(columnMap, "can not find lambda cache for this entity [%s]", lambdaClass.getName());
            initColumnMap = true;
        }
    }

    private ColumnCache getColumnCache(String fieldName, Class<?> lambdaClass) {
        //        Assert.notNull(columnCache, "can not find lambda cache for this property [%s] of entity [%s]", fieldName, lambdaClass.getName());
//        if (columnCache == null) {
//            String fieldNameUnderLine = StrUtils.camelToUnderline(fieldName);
//            columnCache = new ColumnCache(fieldNameUnderLine, fieldNameUnderLine, fieldNameUnderLine);
//        }
        return columnMap.computeIfAbsent(LambdaUtils.formatKey(fieldName), (f) -> {
            String fieldNameUnderLine = StrUtils.camelToUnderline(fieldName);
            return new ColumnCache(fieldNameUnderLine, fieldNameUnderLine, fieldNameUnderLine);
        });
    }


    @Override
    public LambdaUpdateWrapper<T> set(boolean condition, SFunction<T, ?> column, Object val, String mapping) {
        return maybeDo(condition, () -> {
            String sql = formatParam(mapping, val);
            sqlSet.add(columnToString(column) + Constants.EQUALS + sql);
        });
    }

    @Override
     public LambdaUpdateWrapper<T> setSql(boolean condition, String setSql, Object... params) {
         if (condition && StringUtils.isNotBlank(setSql)) {
             sqlSet.add(formatSqlMaybeWithParam(setSql, params));
         }
         return typedThis;
     }
    @Override
    public String getSqlSet() {
        if (CollectionUtils.isEmpty(sqlSet)) {
            return null;
        }
        return String.join(Constants.COMMA, sqlSet);
    }

//    @Override
//    protected LambdaUpdateWrapper<T> instance() {
//        return new MyLambdaUpdateWrapper<>(getEntity(), getEntityClass(), null, paramNameSeq, paramNameValuePairs,
//            new MergeSegments(), paramAlias, SharedString.emptyString(), SharedString.emptyString(), SharedString.emptyString());
//    }

    @Override
    public void clear() {
        super.clear();
        sqlSet.clear();
    }
}

