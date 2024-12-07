package com.pro.framework.mtq.service.multiwrapper.sqlsegment;

import com.pro.framework.api.database.wheredata.WhereAndOrEnum;
import com.pro.framework.api.database.wheredata.WhereDataTree;
import com.pro.framework.api.database.wheredata.WhereDataUnit;
import com.pro.framework.api.database.wheredata.WhereOptEnum;
import com.pro.framework.mtq.service.multiwrapper.util.MultiRelationCaches;
import com.pro.framework.mtq.service.multiwrapper.util.MultiUtil;
import com.pro.framework.mtq.service.multiwrapper.util.mybatisplus.MultiFunction;
import com.pro.framework.mtq.service.multiwrapper.util.mybatisplus.SerializedLambda;
import com.pro.framework.mtq.service.multiwrapper.util.mybatisplus.SerializedLambdaData;
import lombok.SneakyThrows;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@SuppressWarnings({"unused", "unchecked", "AlibabaAbstractMethodOrInterfaceMethodMustUseJavadoc"})
public interface MultiWrapperWhere<T, Wrapper extends MultiWrapperWhere<T, Wrapper>> {

    String getClassName();

    void setClassName(String className);

    WhereDataTree getWhereTree();

    @SneakyThrows
    default <VAL> Wrapper and(Consumer<Wrapper> andContent) {
        // noinspection deprecation
        Wrapper wrapper = (Wrapper) this.getClass().newInstance();
        andContent.accept(wrapper);
        getWhereTree().getWhereDatas().add(wrapper.getWhereTree());
        return (Wrapper) this;
    }

    /***
     * 一个层级上只要有一个or条件,认定本层所有条件都是or关系
     */
    default <VAL> Wrapper or() {
        getWhereTree().setAndOr(WhereAndOrEnum.or);
        return (Wrapper) this;
    }

    default <VAL extends Serializable> Wrapper eq(String propName, Object value) {
        return eq(true, propName, value);
    }

    default <VAL> Wrapper eq(MultiFunction<T, VAL> prop, VAL value) {
        return eq(true, prop, value);
    }

    default <VAL> Wrapper ne(MultiFunction<T, VAL> prop, VAL value) {
        return ne(true, prop, value);
    }

    default <VAL> Wrapper isNull(MultiFunction<T, VAL> prop) {
        return isNull(true, prop);
    }

    default <VAL> Wrapper isNotNull(MultiFunction<T, VAL> prop) {
        return isNotNull(true, prop);
    }


    default <VAL> Wrapper gt(MultiFunction<T, VAL> prop, VAL value) {
        return gt(true, prop, value);
    }

    default <VAL> Wrapper gt(String prop, VAL value) {
        return gt(true, prop, value);
    }

    default <VAL> Wrapper ge(String propName, VAL value) {
        return ge(true, propName, value);
    }

    default <VAL> Wrapper ge(MultiFunction<T, VAL> prop, VAL value) {
        return ge(true, prop, value);
    }

    default <VAL> Wrapper lt(MultiFunction<T, VAL> prop, VAL value) {
        return lt(true, prop, value);
    }

    default <VAL> Wrapper lt(String prop, VAL value) {
        return lt(true, prop, value);
    }

    default <VAL> Wrapper le(String propName, VAL value) {
        return le(true, propName, value);
    }

    default <VAL> Wrapper le(MultiFunction<T, VAL> prop, VAL value) {
        return le(true, prop, value);
    }

    default <VAL> Wrapper in(MultiFunction<T, VAL> prop, Collection<VAL> values) {
        return in(true, prop, values);
    }

    default <VAL> Wrapper in(MultiFunction<T, VAL> prop, VAL... values) {
        return in(true, prop, values);
    }

    default <VAL> Wrapper notIn(MultiFunction<T, VAL> prop, VAL... values) {
        return notIn(true, prop, values);
    }

    default <VAL> Wrapper likeAll(MultiFunction<T, VAL> prop, VAL value) {
        return likeAll(true, prop, value);
    }

    default <VAL> Wrapper likeLeft(MultiFunction<T, VAL> prop, VAL value) {
        return likeLeft(true, prop, value);
    }

    default <VAL> Wrapper eq(Boolean condition, String propName, VAL value) {
        this.addWhereTreeData(condition, propName, value, WhereOptEnum.eq);
        return (Wrapper) this;
    }

    default <VAL> Wrapper eq(Boolean condition, MultiFunction<T, VAL> prop, VAL value) {
        this.addWhereTreeData(condition, prop, value, WhereOptEnum.eq);
        return (Wrapper) this;
    }

    default <VAL> Wrapper ne(Boolean condition, MultiFunction<T, VAL> prop, VAL value) {
        this.addWhereTreeData(condition, prop, value, WhereOptEnum.ne);
        return (Wrapper) this;
    }

    default <VAL> Wrapper isNull(Boolean condition, MultiFunction<T, VAL> prop) {
        this.addWhereTreeData(condition, prop, null, WhereOptEnum.isNull);
        return (Wrapper) this;
    }

    default <VAL> Wrapper isNotNull(Boolean condition, MultiFunction<T, VAL> prop) {
        this.addWhereTreeData(condition, prop, null, WhereOptEnum.isNotNull);
        return (Wrapper) this;
    }

    default <VAL> Wrapper gt(Boolean condition, MultiFunction<T, VAL> prop, VAL value) {
        this.addWhereTreeData(condition, prop, value, WhereOptEnum.gt);
        return (Wrapper) this;
    }

    default <VAL> Wrapper gt(Boolean condition, String prop, VAL value) {
        this.addWhereTreeData(condition, prop, value, WhereOptEnum.gt);
        return (Wrapper) this;
    }

    default <VAL> Wrapper ge(Boolean condition, String propName, VAL value) {
        this.addWhereTreeData(condition, propName, value, WhereOptEnum.ge);
        return (Wrapper) this;
    }

    default <VAL> Wrapper ge(Boolean condition, MultiFunction<T, VAL> prop, VAL value) {
        this.addWhereTreeData(condition, prop, value, WhereOptEnum.ge);
        return (Wrapper) this;
    }

    default <VAL> Wrapper lt(Boolean condition, MultiFunction<T, VAL> prop, VAL value) {
        this.addWhereTreeData(condition, prop, value, WhereOptEnum.lt);
        return (Wrapper) this;
    }

    default <VAL> Wrapper lt(Boolean condition, String prop, VAL value) {
        this.addWhereTreeData(condition, prop, value, WhereOptEnum.lt);
        return (Wrapper) this;
    }

    default <VAL> Wrapper le(Boolean condition, String propName, VAL value) {
        this.addWhereTreeData(condition, propName, value, WhereOptEnum.le);
        return (Wrapper) this;
    }

    default <VAL> Wrapper le(Boolean condition, MultiFunction<T, VAL> prop, VAL value) {
        this.addWhereTreeData(condition, prop, value, WhereOptEnum.le);
        return (Wrapper) this;
    }

    default <VAL> Wrapper in(Boolean condition, MultiFunction<T, VAL> prop, VAL... values) {
        this.addWhereTreeData(condition, prop, values, WhereOptEnum.in);
        return (Wrapper) this;
    }

    default <VAL> Wrapper in(Boolean condition, MultiFunction<T, VAL> prop, Collection<VAL> values) {
        this.addWhereTreeData(condition, prop, values, WhereOptEnum.in);
        return (Wrapper) this;
    }

    default <VAL> Wrapper notIn(Boolean condition, MultiFunction<T, VAL> prop, VAL... values) {
        this.addWhereTreeData(condition, prop, values, WhereOptEnum.not_in);
        return (Wrapper) this;
    }

    default <VAL> Wrapper likeAll(Boolean condition, MultiFunction<T, VAL> prop, VAL value) {
        this.addWhereTreeData(condition, prop, value, WhereOptEnum.likeAll);
        return (Wrapper) this;
    }

    default <VAL> Wrapper likeLeft(Boolean condition, MultiFunction<T, VAL> prop, VAL value) {
        this.addWhereTreeData(condition, prop, value, WhereOptEnum.likeLeft);
        return (Wrapper) this;
    }

    /**
     * 递归 getSqlWhereProps 拼接最后结果
     *
     * @return where条件中的字段条件信息(拼接后)
     */
    default String getSqlWhereProps(Class<?> tableClassThis, String relationCode, List<WhereDataUnit> extendParams) {
        List<String> sqlWheres = new ArrayList<>(2);
        String sqlWhereProps = this.getWhereTree().getSqlWhereProps(relationCode);
        if (!MultiUtil.isEmpty(sqlWhereProps)) {
            sqlWheres.add(sqlWhereProps);
        }
        String tableLogicFieldName = MultiRelationCaches.getTableLogicFieldName(tableClassThis);
        if (tableLogicFieldName != null) {
            sqlWheres.add("`" + relationCode + "." + tableLogicFieldName + "` = 0 ");
        }
        if (!MultiUtil.isEmpty(extendParams)) {
            sqlWheres.addAll(extendParams.stream().map(p -> p.getSqlWhereProps(relationCode)).collect(Collectors.toList()));
        }
        return String.join("    \nand ", sqlWheres);
    }


    /**
     * 添加过滤条件
     *
     * @param condition 方便开发,false忽略此次添加
     * @param prop      字段名
     * @param values    字段值
     * @param opt       等于/大于 等条件判断
     * @param <VAL>     字段的泛型
     */
    default <VAL> void addWhereTreeData(Boolean condition, MultiFunction<T, VAL> prop, Object values, WhereOptEnum opt) {
        if (!condition) {
            return;
        }
        SerializedLambdaData resolve = SerializedLambda.resolveCache(prop);
        if (null == getClassName()) {
            setClassName(MultiUtil.firstToLowerCase(resolve.getClazz().getSimpleName()));
        }
        addWhereTreeData(resolve.getPropName(), opt, values);
    }


    /**
     * 添加过滤条件
     *
     * @param condition 方便开发,false忽略此次添加
     * @param propName  字段名
     * @param values    字段值
     * @param opt       等于/大于 等条件判断
     * @param <VAL>     字段的泛型
     */
    default <VAL> void addWhereTreeData(Boolean condition, String propName, Object values, WhereOptEnum opt) {
        if (!condition) {
            return;
        }
        addWhereTreeData(propName, opt, values);
    }

    default void addWhereTreeData(String propName, WhereOptEnum opt, Object values) {
        getWhereTree().getWhereDatas().add(new WhereDataUnit(propName, opt, values));
    }
}
