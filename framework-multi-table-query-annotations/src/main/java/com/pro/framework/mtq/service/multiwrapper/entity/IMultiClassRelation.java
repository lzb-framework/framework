package com.pro.framework.mtq.service.multiwrapper.entity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 类和类的关系
 * 可以在数据库中储存(推荐),也可以从枚举中读取
 * 可以,统一按表名字排序(class1和class2,不同的Relation和Relation)
 *
 * @author Administrator
 */
public interface IMultiClassRelation {

    String getCode();

    String getClassName1();
    String getClassName2();

    ClassRelationOneOrManyEnum getClass1OneOrMany();
    ClassRelationOneOrManyEnum getClass2OneOrMany();

    Boolean getClass1Require() ;
    Boolean getClass2Require();

    String getClass1KeyProp();
    String getClass2KeyProp();

    default Set<String> getClassNames() {
        return new HashSet<>(Arrays.asList(getClassName1(), getClassName2()));
    }
}
