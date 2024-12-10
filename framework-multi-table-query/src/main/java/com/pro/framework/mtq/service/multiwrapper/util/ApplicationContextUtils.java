//package com.pro.framework.mtq.service.multiwrapper.util;
//
//import cn.hutool.core.util.ClassUtil;
//import com.pro.base.utils.ClassUtils;
//import org.springframework.beans.BeansException;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//import org.springframework.stereotype.Component;
//
//import java.util.Arrays;
//
///**
// * 获取bean 上下文
// */
//@Component
//public class ApplicationContextUtils implements ApplicationContextAware {
//
//    public static ApplicationContext applicationContext;
//
////    private static boolean isProd;
////    private static boolean isDev;
//
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        ApplicationContextUtils.applicationContext = applicationContext;
////        String[] profiles = applicationContext.getEnvironment().getActiveProfiles();
////        isProd = Arrays.stream(profiles).anyMatch(p -> p.contains("prod"));
////        isDev = Arrays.stream(profiles).anyMatch(p -> p.contains("dev"));
////        isDev = profiles.length == 1 && "dev".equals(profiles[0]);
//    }
//
////    public static Object getBean(String name) {
////        return applicationContext.getBean(name);
////    }
//
////    public static <T> T getBean(String name, Class<T> c) {
////        return applicationContext.getBean(name, c);
////    }
//
////    public static <T> T checkGetBean(String name, Class<T> c) {
////        if (applicationContext.containsBean(name)) {
////            Object bean = applicationContext.getBean(name);
////            if (ClassUtils.checkImplement(bean.getClass(), c)) {
////                return (T) bean;
////            }
////        }
////        return null;
////    }
//
////    public static <T> T getBean(Class<T> c) {
////        return applicationContext.getBean(c);
////    }
//
//
////    public static boolean isDev() {
////        return isDev;
////    }
////
////    public static boolean isProd() {
////        return isProd;
////    }
//}
