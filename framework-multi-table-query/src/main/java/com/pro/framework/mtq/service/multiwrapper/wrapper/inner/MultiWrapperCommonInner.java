package com.pro.framework.mtq.service.multiwrapper.wrapper.inner;

import com.pro.framework.mtq.service.multiwrapper.sqlsegment.MultiWrapperAggregate;
import com.pro.framework.mtq.service.multiwrapper.sqlsegment.MultiWrapperSelect;
import com.pro.framework.mtq.service.multiwrapper.sqlsegment.MultiWrapperWhere;
import com.pro.framework.mtq.service.multiwrapper.sqlsegment.aggregate.MultiAggregateInfo;
import com.pro.framework.api.database.wheredata.WhereDataTree;
import com.pro.framework.mtq.service.multiwrapper.util.MultiUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 */
@Data
@NoArgsConstructor
@SuppressWarnings("unused")
public class MultiWrapperCommonInner<ENTITY, Wrapper extends MultiWrapperCommonInner<ENTITY, Wrapper>> implements
        MultiWrapperWhere<ENTITY, MultiWrapperCommonInner<ENTITY, Wrapper>>,
        MultiWrapperSelect<ENTITY, MultiWrapperCommonInner<ENTITY, Wrapper>>,
        MultiWrapperAggregate<ENTITY, MultiWrapperCommonInner<ENTITY, Wrapper>> {

    /**
     * 类为了生成List<SUB>
     */
    private Class<ENTITY> clazz;
    /**
     * 下划线表名
     */
    private String className;

    /**
     * where条件
     */
    private WhereDataTree whereTree = new WhereDataTree();

    /**
     * select属性列表
     */
    private List<String> selectFields;
    private List<String> selectMores = new ArrayList<>();
    private List<String> selectLess = new ArrayList<>();

    /**
      * 分组属性列表
      */
     private List<String> groupByFields = new ArrayList<>();
    public MultiWrapperCommonInner(Class<ENTITY> clazz) {
        this.clazz = clazz;
        MultiUtil.assertNoNull(clazz, "class not exist");
        this.className = MultiUtil.firstToLowerCase(clazz.getSimpleName());
    }

//    private MultiWrapperSubMainWhereInner<ENTITY> mainWhere;

    /**
     * 聚合函数信息 执行MultiExecutor.page()/MultiExecutor.aggregate()时,才会使用到
     */
    protected List<MultiAggregateInfo> aggregateInfos = new ArrayList<>(8);


//    public static <SUB, MAIN_WHERE extends MultiWrapperSubMainWhereInner<SUB>> MultiWrapperCommonInner<SUB> lambda(Class<SUB> clazz) {
//        String className = MultiUtil.firstToLowerCase(clazz.getSimpleName());
//        MultiWrapperCommonInner<SUB> wrapperSub = new MultiWrapperCommonInner<>();
//        wrapperSub.setClassName(className);
//        wrapperSub.setClazz(clazz);
//        clazz.newInstance()
//        return this;
//    }

//    @SafeVarargs
//    @Override
//    public final <VAL> Wrapper<ENTITY> select(MultiFunction<ENTITY, VAL>... propFuncs) {
//        return MultiWrapperSelect.super.select(propFuncs);
//    }


//    public <VAL, MAIN_WHERE extends MultiWrapperSubMainWhereInner<ENTITY>> MultiWrapperCommonInner<ENTITY> mainWhere(Consumer<MAIN_WHERE> mainWhereConsumer) {
//        if (this.mainWhere == null) {
//            this.mainWhere = new MultiWrapperSubMainWhereInner<>();
//        }
//        //noinspection unchecked
//        mainWhereConsumer.accept((MAIN_WHERE) mainWhere);
//        return this;
//    }
}
