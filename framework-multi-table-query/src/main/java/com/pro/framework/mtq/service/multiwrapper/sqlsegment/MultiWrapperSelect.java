package com.pro.framework.mtq.service.multiwrapper.sqlsegment;

import com.pro.framework.mtq.service.multiwrapper.util.MultiRelationCaches;
import com.pro.framework.mtq.service.multiwrapper.util.MultiTuple2;
import com.pro.framework.mtq.service.multiwrapper.util.MultiUtil;
import com.pro.framework.mtq.service.multiwrapper.util.mybatisplus.MultiFunction;
import com.pro.framework.mtq.service.multiwrapper.util.mybatisplus.SerializedLambdaData;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author Administrator
 */
@SuppressWarnings({"unused", "unchecked", "AlibabaAbstractMethodOrInterfaceMethodMustUseJavadoc"})
public interface MultiWrapperSelect<T, Wrapper extends MultiWrapperSelect<T, Wrapper>> {
    String getClassName();

    void setClassName(String className);

    List<String> getSelectFields();

    default List<String> getSelectMores() {
        return Collections.EMPTY_LIST;
    }

    default List<String> getSelectLess() {
        return Collections.EMPTY_LIST;
    }

    void setSelectFields(List<String> list);

    default void setSelectMores(List<String> list) {
    }

    default void setSelectLess(List<String> list) {
    }

    Class<T> getClazz();

    /***
     * 设置查询字段列表,不设置则默认*(全查询)
     *
     * @param propFuncs 多个字段
     * @param <VAL> 字段泛型
     * @return 当前wrapper
     */
    default <VAL> Wrapper select(MultiFunction<T, VAL>... propFuncs) {
        MultiTuple2<String, List<String>> result = MultiUtil.calcMultiFunctions(SerializedLambdaData::getPropName, propFuncs);
        if (null == getClassName()) {
            setClassName(result.getT1());
        }
        this.setSelectFields(result.getT2());
        return (Wrapper) this;
    }

    default <VAL> Wrapper select(List<String> propFuncs) {
        this.setSelectFields(propFuncs);
        return (Wrapper) this;
    }

    default <VAL> Wrapper selectMores(List<String> selectMores) {
        this.setSelectMores(selectMores);
        return (Wrapper) this;
    }

    default <VAL> Wrapper selectLess(List<String> selectLess) {
        this.setSelectLess(selectLess);
        return (Wrapper) this;
    }

    /**
     * 拼接出select字段列表
     *
     * @param tableClassParent
     * @param relationCode     当前关系的code
     * @return sqlSelectProps
     */
    default String getSqlSelectProps(Class<?> tableClassParent, String relationCode) {

        loadSelectFields(tableClassParent, relationCode);
        List<String> selectFields = getSelectFields();
        List<String> selectLess = getSelectLess();
        if (selectLess != null) {
            selectFields.removeAll(selectLess);
        }
        List<String> selects = selectFields.stream().map(fieldName -> "  " + relationCode + "." + MultiUtil.camelToUnderline(fieldName) + " as " + "`" + relationCode + "." + fieldName + "`").collect(Collectors.toList());
        List<String> selectMores = getSelectMores();
        if (selectMores != null) {
            selects.addAll(selectMores);
        }

        return selects.isEmpty() ? null : String.join(",\n", selects);
    }

    default List<String> loadSelectFields(Class<?> tableClassParent, String relationCode) {
        List<String> selectFieldNames = getSelectFields();
        //默认用全字段去查询,不用*,方便后续多表字段对应
        if (null == selectFieldNames) {
            // 主表 或者有 setMethod的副表
            selectFieldNames = MultiRelationCaches.getFieldNamesByClass(getClazz());
            setSelectFields(selectFieldNames);
        } else {
            String tableLogicFieldName = MultiRelationCaches.getTableLogicFieldName(getClazz());
            if (tableLogicFieldName != null && !selectFieldNames.contains(tableLogicFieldName)) {
                selectFieldNames.add(tableLogicFieldName);
            }
        }
        return selectFieldNames;
    }

    /**
     * 获取select的全字段
     *
     * @return sqlSelectProps
     */
    default List<String> getSelectFieldNames() {
        List<String> selectFieldNames = getSelectFields();
        //默认用全字段去查询,不用*,方便后续多表字段对应
        if (null == selectFieldNames) {
            selectFieldNames = MultiRelationCaches.getFieldNamesByClass(getClazz());
            setSelectFields(selectFieldNames);
        }
        return selectFieldNames;
    }
}
