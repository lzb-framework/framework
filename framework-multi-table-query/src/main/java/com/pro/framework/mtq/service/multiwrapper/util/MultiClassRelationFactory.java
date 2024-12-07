package com.pro.framework.mtq.service.multiwrapper.util;

import com.pro.framework.api.IReloadService;
import com.pro.framework.api.util.AssertUtil;
import com.pro.framework.mtq.service.multiwrapper.entity.IMultiClassRelationService;
import com.pro.framework.mtq.service.multiwrapper.entity.IMultiClassRelation;
import lombok.Getter;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Getter
public class MultiClassRelationFactory implements IReloadService {
    private static IMultiClassRelationService<?> tableRelationService;
    public static MultiClassRelationFactory INSTANCE;
    private List<IMultiClassRelation> relations;
    private Map<String, IMultiClassRelation> relationCodeMap;
    private Map<String, Map<String, List<IMultiClassRelation>>> relation2ClassNameMap;
    private Map<String, Class<?>> classMap;

    public MultiClassRelationFactory(IMultiClassRelationService<?> tableRelationService) {
        load(tableRelationService);

        INSTANCE = this;
    }

    public void load(IMultiClassRelationService<?> tableRelationService) {
        MultiClassRelationFactory.tableRelationService = tableRelationService;
        this.relations = Collections.unmodifiableList(tableRelationService.loadRelation());

        this.relationCodeMap = relations.stream().collect(Collectors.toMap(IMultiClassRelation::getCode, o -> o));
        this.relationCodeMap = Collections.unmodifiableMap(relationCodeMap);

        Map<String, Map<String, List<IMultiClassRelation>>> map1 = relations.stream().collect(Collectors.groupingBy(IMultiClassRelation::getClassName1)).entrySet()
                .stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream().collect(Collectors.groupingBy(IMultiClassRelation::getClassName2))));
        Map<String, Map<String, List<IMultiClassRelation>>> map2 = relations.stream().collect(Collectors.groupingBy(IMultiClassRelation::getClassName2)).entrySet()
                .stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream().collect(Collectors.groupingBy(IMultiClassRelation::getClassName1))));
        this.relation2ClassNameMap = new HashMap<>(256);
        this.relation2ClassNameMap.putAll(map1);
        this.relation2ClassNameMap.putAll(map2);
        this.relation2ClassNameMap = Collections.unmodifiableMap(relation2ClassNameMap);

        this.classMap = tableRelationService.loadClasses().stream().filter(c->!c.isInterface() && !Modifier.isAbstract(c.getModifiers())).collect(Collectors.toMap(c -> MultiUtil.firstToLowerCase(c.getSimpleName()), c -> c));
        this.classMap = Collections.unmodifiableMap(classMap);
    }

    public void reload() {
        load(tableRelationService);
    }

    public Class<?> getEntityClass(String entityClassName) {
        Class<?> aClass = classMap.get(entityClassName);
        AssertUtil.notEmpty(aClass, "无效请求");
        return aClass;
    }
}
