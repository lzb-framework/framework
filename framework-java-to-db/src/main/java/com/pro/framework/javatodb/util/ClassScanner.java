package com.pro.framework.javatodb.util;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.util.HashSet;
import java.util.Set;

public class ClassScanner {
    public static Set<Class<?>> scanClasses(String packageName) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(Object.class));

        Set<Class<?>> classes = new HashSet<>();
        for (BeanDefinition beanDefinition : scanner.findCandidateComponents(packageName)) {
            try {
                Class<?> clazz = Class.forName(beanDefinition.getBeanClassName());
                classes.add(clazz);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return classes;
    }
}
