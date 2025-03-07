package com.pro.framework.mtq.service.multiwrapper.wrapper;

import com.pro.framework.api.util.AssertUtil;
import com.pro.framework.mtq.service.multiwrapper.constant.MultiConstant;
import com.pro.framework.mtq.service.multiwrapper.entity.IMultiClassRelation;
import com.pro.framework.mtq.service.multiwrapper.util.MultiClassRelationFactory;
import com.pro.framework.mtq.service.multiwrapper.wrapper.inner.MultiWrapperInner;
import com.pro.framework.mtq.service.multiwrapper.wrapper.inner.MultiWrapperSubInner;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 多表联查器
 * 举例说明 主表:user_staff 副表:user_staff_address
 *
 * @param <MAIN>
 * @author Administrator
 */
@NoArgsConstructor
@Slf4j
@Data
@SuppressWarnings("unused")
public class MultiWrapper<MAIN> {
    public MultiWrapper(MultiWrapperInner<MAIN> wrapperInner) {
        this.wrapperInner = wrapperInner;
    }

    /**
     * 主表信息
     */
    private MultiWrapperInner<MAIN> wrapperInner;


    public MultiWrapper(MultiWrapperMain<MAIN> wrapperMain) {
        this(wrapperMain, new MultiWrapperSub<?>[]{});
    }

    public MultiWrapper(List<String> classes) {
        this(
                classes.get(0),
                classes.stream().skip(1L).toArray(String[]::new)
        );
    }

    public MultiWrapper(String mainClass, String... subClass) {
        //noinspection unchecked
        this(
                MultiWrapperMain.lambda(AssertUtil.notEmpty(MultiClassRelationFactory.INSTANCE.getEntityClass(mainClass), "class not exit:"+mainClass)),
                Arrays.stream(subClass).map(c -> MultiClassRelationFactory.INSTANCE.getEntityClass(c)).toArray(Class<?>[]::new)
        );
    }

    public MultiWrapper(MultiWrapperMain<MAIN> wrapperMain, Class<?>... subTableClasses) {
        this(wrapperMain, Arrays.stream(subTableClasses).map(MultiWrapperSub::lambda).toArray(MultiWrapperSub<?>[]::new));
    }

    public MultiWrapper(MultiWrapperMain<MAIN> wrapperMain, MultiWrapperSub<?>... subTableWrappers) {
        wrapperInner = new MultiWrapperInner<>(wrapperMain.getWrapperMainInner(), Arrays.stream(subTableWrappers).map(MultiWrapperSub::getWrapperSubInner).toArray(MultiWrapperSubInner<?, ?>[]::new));
    }

    //
    // private Class<MAIN> getMainClass(String[] classes) {
    //     if (classes == null || classes.length <= 0) {
    //         throw new MultiException("please input classes");
    //     }
    //     //noinspection unchecked
    //     return (Class<MAIN>) MultiClassRelationFactory.INSTANCE.getEntityClass(classes[0]);
    // }
    //
    // private Class<?>[] getSubClass(String[] classes) {
    //
    //     for (int i = 1; i < classes.length; i++) {
    //
    //     }
    //     return MultiClassRelationFactory.INSTANCE.getEntityClass(classes[0]);
    // }


    /**
     * 主表信息
     * 例如 select * from user_staff
     *
     * @return MultiWrapper
     */
    public static <MAIN> MultiWrapper<MAIN> main(MultiWrapperMain<MAIN> wrapperMain) {
        MultiWrapper<MAIN> wrapper = new MultiWrapper<>(new MultiWrapperInner<>());
        wrapper.wrapperInner.wrapperMain = wrapperMain.getWrapperMainInner();
        return wrapper;
    }

    /***
     * join 副表信息
     *
     * @param subTableWrapper subTableWrapper
     * @return MultiWrapper
     */
    public MultiWrapper<MAIN> leftJoin(MultiWrapperSub<?> subTableWrapper) {
        return leftJoin(null, subTableWrapper);
    }

    /***
     * join是有顺序的,前后两张表,必须有直接关联
     *
     * @param subTableWrapper subTableWrapper
     * @return MultiWrapper
     */
    public MultiWrapper<MAIN> innerJoin(MultiWrapperSub<?> subTableWrapper) {
        return innerJoin(null, subTableWrapper);
    }

    /***
     * join是有顺序的,前后两张表,必须有直接关联
     *
     * @param relationCode      {@link IMultiClassRelation#getCode()}
     * @param subTableWrapper 副表的select和 on内条件信息
     * @return MultiWrapper
     */
    public MultiWrapper<MAIN> leftJoin(String relationCode, MultiWrapperSub<?> subTableWrapper) {
        MultiConstant.JoinTypeEnum joinType = MultiConstant.JoinTypeEnum.left_join;
        wrapperInner.getMainMultiWrapper(joinType, relationCode, subTableWrapper.getWrapperSubInner());
        return this;
    }

    public MultiWrapper<MAIN> innerJoin(String relationCode, MultiWrapperSub<?> subTableWrapper) {
        MultiConstant.JoinTypeEnum joinType = MultiConstant.JoinTypeEnum.inner_join;
        wrapperInner.getMainMultiWrapper(joinType, relationCode, subTableWrapper.getWrapperSubInner());
        return this;
    }


    /**
     * @param extendParams 参数Map例如:
     *                     {
     *                     "userAndUserStaff_balance":"100#%#", //其中userAndUserStaff是relationCode,  like '张三%' 才能走索引
     *                     "userStaff_sex":"1"
     *                     }
     */
    public MultiWrapper<MAIN> extendParams(Map<String, ?> extendParams) {
        wrapperInner.extendParams = extendParams;
//        wrapperInner.extendParams(extendParams);
        return this;
    }

//    public MultiWrapper<MAIN> selectMores(List<String> selectMores) {
//        wrapperInner.wrapperMain.setSelectMores(selectMores);
//        return this;
//    }
}
