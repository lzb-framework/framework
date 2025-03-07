//package com.pro.framework.db;
//
//
//import cn.hutool.core.util.StrUtil;
//import com.baomidou.mybatisplus.extension.service.IService;
//import com.pro.framework.api.db.TimeQuery;
//import com.pro.framework.api.util.ClassUtils;
//import com.pro.framework.db.service.DBBaseService;
//import com.pro.framework.mtq.service.wrapper.Executor;
//import com.pro.framework.mtq.service.wrapper.executor.service.IDBBaseService;
//import com.pro.framework.mtq.service.wrapper.executor.service.DBBaseService;
//import com.pro.framework.mtq.service.wrapper.executor.service.DBBaseServiceIServiceAdapter;
//import com.pro.framework.mtq.service.wrapper.sqlsegment.wheredata.WhereDataUnit;
//import com.pro.framework.mtq.service.wrapper.util.json.jackson.JsonUtil;
//import com.pro.framework.mtq.service.wrapper.wrapper.Wrapper;
//import com.pro.framework.mtq.service.wrapper.wrapper.inner.WrapperMainInner;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationContext;
//import org.springframework.stereotype.Service;
//import org.springframework.web.bind.annotation.RequestParam;
//
//import java.io.Serializable;
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
///**
// * R: Read（读取）
// * C: Create（创建）
// * U: Update（更新）
// * D: Delete（删除）
// */
//@SuppressWarnings("unchecked")
//@Component
//public class CudUtil {
//    private static ApplicationContext applicationContext;
//    private static DBBaseService<?> dbBaseService;
//
//    @Autowired
//    public void setAdaptor(
//            ApplicationContext applicationContext,
//            DBBaseService<?> DBBaseService
//    ) {
//        CudUtil.applicationContext = applicationContext;
//        CudUtil.dbBaseService = DBBaseService;
//    }
//
////    public static <T> IPage<T> selectPages(String entityClassNames, IPageInput page, @RequestParam(required = false) Map<String, Object> paramMap, TimeQuery timeQuery) {
////        List<String> entityClassNameList = Arrays.stream(entityClassNames.split(",")).collect(Collectors.toList());
////        Wrapper<T> wrapper = new Wrapper<>(entityClassNameList);
////
////        WrapperMainInner<T, ?> wrapperMain = wrapper.getWrapperInner().getWrapperMain();
////        wrapperMain.ge(!Util.isEmpty(timeQuery.getStart()), timeQuery.getTimeColumn(), timeQuery.getStart());
////        if (!Util.isEmpty(timeQuery.getEnd())) {
////            wrapperMain.le(!Util.isEmpty(timeQuery.getEnd()), timeQuery.getTimeColumn(), LocalDateTime.of(LocalDate.parse(timeQuery.getEnd()), LocalTime.MAX));
////        }
////        wrapperMain.setSelectFields(getSelectFields(wrapperMain.getClazz()));
////
////        List<WhereDataUnit> whereDataUnits = page.getWhereDataUnits();
////        if (whereDataUnits != null) {
////            whereDataUnits.forEach(w -> {
////                String propName = w.getPropName();
////                if (StrUtils.isNotBlank(propName) && null != w.getOpt() && null != w.getValues()) {
////                    wrapperMain.addWhereTreeData(propName, w.getOpt(), w.getValues());
////                }
////            });
////        }
////        return Executor.page(page, wrapper.extendParams(paramMap));
////    }
//
//    public static <T> List<T> selectLists(String entityClassNames, Map<String, Object> paramMap, TimeQuery timeQuery) {
//        return list(entityClassNames, paramMap, timeQuery, 1000L);
//    }
//
//    public static <T> List<T> selectLists(String entityClassNames, Map<String, Object> paramMap, TimeQuery timeQuery, Long limit) {
//        return list(entityClassNames, paramMap, timeQuery, limit);
//    }
//
////    /**
////     * 分组求和
////     */
////    public static List<AggregateResult> selectCountSum(String entityClassNames, Map<String, Object> paramMap, TimeQuery timeQuery, GroupBy groupBy) {
////        Wrapper<?> wrapper = new Wrapper<>(entityClassNames);
////        wrapper.extendParams(paramMap);
////        WrapperMainInner<?, ?> wrapperMain = wrapper.getWrapperInner().getWrapperMain();
////        wrapperMain.groupBy(groupBy.getGroupBys().toArray(String[]::new));
////        wrapperMain.count();
////        wrapperMain.countDistinct();
////        wrapperMain.sumAll();
////        wrapperMain
////                .ge(!Util.isEmpty(timeQuery.getStart()), timeQuery.getTimeColumn(), timeQuery.getStart())
////                .le(!Util.isEmpty(timeQuery.getEnd()), timeQuery.getTimeColumn(), timeQuery.getEnd());
////        return Executor.aggregateList(wrapper);
////    }
//
//    public static <T> T selectOneById(String entityClassName, Serializable id) {
//        ClassUtils.getAllFields()
//        Class<T> beanClass = (Class<T>) ClassRelationFactory.INSTANCE.getEntityClass(entityClassName);
//        return Executor.getById(beanClass, id);
//    }
//
//    public static <T> List<T> list(String entityClassNames, Map<String, Object> paramMap, TimeQuery timeQuery) {
//        return list(entityClassNames, paramMap, timeQuery, 1000L);
//    }
//
//    public static <T> List<T> list(String entityClassNames, Map<String, Object> paramMap, TimeQuery timeQuery, Long limit) {
//        List<String> entityClassNameList = Arrays.stream(entityClassNames.split(",")).collect(Collectors.toList());
//        if (!Util.isEmpty(timeQuery.getStart())) {
//            paramMap.put(timeQuery.getTimeColumn(), "#ge#" + timeQuery.getStart());
//        }
//        if (!Util.isEmpty(timeQuery.getEnd())) {
//            paramMap.put(timeQuery.getTimeColumn(), "#le#" + LocalDateTime.of(LocalDate.parse(timeQuery.getEnd()), LocalTime.MAX));
//        }
//        Wrapper<T> wrapper = new Wrapper<>(entityClassNameList);
//        wrapper.getWrapperInner().getWrapperMain().limit(0, limit);
//        return Executor.list(wrapper.extendParams(paramMap));
//    }
//
//    public static <T> void insert(String entityClassName, T entity) {
//        getService(entityClassName).save(entity);
//    }
//
//    public static <T> T insert(String entityClassName, String entityStr) {
//        T entity = strToEntity(entityClassName, entityStr);
//        getService(entityClassName).save(entity);
//        return entity;
//    }
//
//    public static <T> T insertOrUpdate(String entityClassName, String entityStr) {
//        T entity = strToEntity(entityClassName, entityStr);
//        getService(entityClassName).saveOrUpdate(entity);
//        return entity;
//    }
//
//    public static <T> Boolean insertOrUpdate(String entityClassName, T entity) {
//        return getService(entityClassName).saveOrUpdate(entity);
//    }
//
//
//    public static <T> void updateById(String entityClassName, T entity) {
//        getService(entityClassName).updateById(entity);
//    }
//
//    public static void updateById(String entityClassName, String entityStr) {
//        getService(entityClassName).updateById(strToEntity(entityClassName, entityStr));
//    }
//
//
//    public static void delete(String entityClassName, Long id) {
//        getService(entityClassName).removeById(ClassRelationFactory.INSTANCE.getEntityClass(entityClassName), id);
//    }
//
//
//    /**
//     * 执行更新sql
//     */
//    public static boolean execute(String sql) {
//        return dbBaseService.executeSql(sql);
//    }
//
//    // 获取服务
//    private static <T> IDBBaseService<T> getService(String entityClassName) {
//        String beanName = entityClassName + "Service";
//        boolean hasService = applicationContext.containsBean(beanName);
//        if (hasService) {
//            Object service = applicationContext.getBean(beanName);
//            if (service instanceof IDBBaseService) {
//                return (IDBBaseService<T>) service;
//            } else if (service instanceof IService) {
//                return new DBBaseServiceIServiceAdapter<>((IService<T>) service);
//            }
//        }
//        return (IDBBaseService<T>) dbBaseService;
//    }
//
//    // 字符串转实体
//    private static <T> T strToEntity(String entityClassName, String entityStr) {
//        Class<T> beanClass = (Class<T>) ClassRelationFactory.INSTANCE.getEntityClass(entityClassName);
//        return JsonUtil.fromString(entityStr, beanClass);
//    }
//
//    private static <T> List<String> getSelectFields(Class<T> clazz) {
//        Map<String, Tuple3<Field, Method, Method>> classInfos = RelationCaches.getClassInfos(clazz);
//        return classInfos.values().stream().map(Tuple3::getT1).map(Field::getName).collect(Collectors.toList());
//    }
//
//    /**
//     * 分页查询(执行)并处理
//     */
//    public static <T> List<T> pageDeal(Function<Long, List<T>> pageFunction) {
//        return pageDeal(1L, pageFunction);
//    }
//
//    /**
//     * 分页查询(执行)并处理
//     */
//    public static <T> List<T> pageDeal(long fromrPage, Function<Long, List<T>> pageFunction) {
//        List<T> combinedList = new ArrayList<>();
//        Long currPage = fromrPage; // Start from the first page
//        Class clazz = null;
//        while (true) {
//            List<T> pageResult = pageFunction.apply(currPage);
//            if (pageResult != null && !pageResult.isEmpty()) {
//                if (clazz == null) {
//                    clazz = pageResult.get(0).getClass();
//                }
//                System.out.println("已完成" + clazz + "第 " + currPage + " 页的处理");
//                combinedList.addAll(pageResult);
//                currPage++;
//            } else {
//                break;
//            }
//        }
//        return combinedList;
//    }
//}
