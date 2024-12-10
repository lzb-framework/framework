package com.pro.framework.mtq.service.multiwrapper.entity;

import java.util.Collection;
import java.util.List;

/**
 * @author Administrator
 */
public interface IMultiClassRelationService<T extends IMultiClassRelation> {
    /**
     * 加载表和表的关系
     *
     * @return 加载表和表的关系
     */
    List<T> loadRelation();

    /**
     * 所有实体类
     */
    Collection<Class<?>> loadClasses();

    Class<?> getClass(String entityClassName);
}
