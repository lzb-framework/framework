package com.pro.framework.mybatisplus.wrapper;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.ColumnCache;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.pro.framework.api.util.StrUtils;
import org.apache.ibatis.reflection.property.PropertyNamer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MyLambdaQueryWrapper<T> extends LambdaQueryWrapper<T> {
    public MyLambdaQueryWrapper() {
        super();
    }

    MyLambdaQueryWrapper(T entity, Class<T> entityClass, AtomicInteger paramNameSeq,
                         Map<String, Object> paramNameValuePairs, MergeSegments mergeSegments, SharedString paramAlias,
                         SharedString lastSql, SharedString sqlComment, SharedString sqlFirst) {
        super.setEntity(entity);
        super.setEntityClass(entityClass);
        this.paramNameSeq = paramNameSeq;
        this.paramNameValuePairs = paramNameValuePairs;
        this.expression = mergeSegments;
//            this.sqlSelect = sqlSelect;
        this.paramAlias = paramAlias;
        this.lastSql = lastSql;
        this.sqlComment = sqlComment;
        this.sqlFirst = sqlFirst;
    }

    private Map<String, ColumnCache> columnMap;
    private boolean initColumnMap = false;

    @Override
    protected ColumnCache getColumnCache(SFunction<T, ?> column) {
        LambdaMeta meta = LambdaUtils.extract(column);
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
}
