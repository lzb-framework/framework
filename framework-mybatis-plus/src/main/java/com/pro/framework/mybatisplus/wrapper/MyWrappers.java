/*
 * Copyright (c) 2011-2022, baomidou (jobob@qq.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pro.framework.mybatisplus.wrapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

/**
 * Wrapper 条件构造
 *
 * @author Caratacus
 */
public final class MyWrappers {

    /**
     * 获取 QueryWrapper&lt;T&gt;
     *
     * @param <T> 实体类泛型
     * @return QueryWrapper&lt;T&gt;
     */
    public static <T> QueryWrapper<T> query() {
        return new MyQueryWrapper<>();
    }

    /**
     * 获取 QueryWrapper&lt;T&gt;
     *
     * @param entity 实体类
     * @param <T>    实体类泛型
     * @return QueryWrapper&lt;T&gt;
     */
    public static <T> QueryWrapper<T> query(T entity) {
        MyQueryWrapper<T> wrapper = new MyQueryWrapper<>();
        wrapper.setEntity(entity);
        return wrapper;
    }

    /**
     * 获取 LambdaQueryWrapper&lt;T&gt;
     *
     * @param <T> 实体类泛型
     * @return LambdaQueryWrapper&lt;T&gt;
     */
    public static <T> LambdaQueryWrapper<T> lambdaQuery() {
        return new MyLambdaQueryWrapper<>();
    }

    /**
     * 获取 LambdaQueryWrapper&lt;T&gt;
     *
     * @param entity 实体类
     * @param <T>    实体类泛型
     * @return LambdaQueryWrapper&lt;T&gt;
     */
    public static <T> LambdaQueryWrapper<T> lambdaQuery(T entity) {
        MyLambdaQueryWrapper wrapper = new MyLambdaQueryWrapper<>();
        wrapper.setEntity(entity);
        return wrapper;
    }

//    /**
//     * 获取 LambdaQueryWrapper&lt;T&gt;
//     *
//     * @param entityClass 实体类class
//     * @param <T>         实体类泛型
//     * @return LambdaQueryWrapper&lt;T&gt;
//     * @since 3.3.1
//     */
//    public static <T> LambdaQueryWrapper<T> lambdaQuery(Class<T> entityClass) {
//        return new MyLambdaQueryWrapper<>(entityClass);
//    }

    /**
     * 获取 UpdateWrapper&lt;T&gt;
     *
     * @param <T> 实体类泛型
     * @return UpdateWrapper&lt;T&gt;
     */
    public static <T> UpdateWrapper<T> update() {
        return new MyUpdateWrapper<>();
    }

    /**
     * 获取 UpdateWrapper&lt;T&gt;
     *
     * @param entity 实体类
     * @param <T>    实体类泛型
     * @return UpdateWrapper&lt;T&gt;
     */
    public static <T> UpdateWrapper<T> update(T entity) {
        MyUpdateWrapper<T> wrapper = new MyUpdateWrapper<>();
        wrapper.setEntity(entity);
        return wrapper;
    }

    /**
     * 获取 LambdaUpdateWrapper&lt;T&gt;
     *
     * @param <T> 实体类泛型
     * @return LambdaUpdateWrapper&lt;T&gt;
     */
    public static <T> LambdaUpdateWrapper<T> lambdaUpdate() {
        return new MyLambdaUpdateWrapper<>();
    }

//    /**
//     * 获取 LambdaUpdateWrapper&lt;T&gt;
//     *
//     * @param entity 实体类
//     * @param <T>    实体类泛型
//     * @return LambdaUpdateWrapper&lt;T&gt;
//     */
//    public static <T> LambdaUpdateWrapper<T> lambdaUpdate(T entity) {
//        return new MyLambdaUpdateWrapper<>(entity);
//    }

//    /**
//     * 获取 LambdaUpdateWrapper&lt;T&gt;
//     *
//     * @param entityClass 实体类class
//     * @param <T>         实体类泛型
//     * @return LambdaUpdateWrapper&lt;T&gt;
//     * @since 3.3.1
//     */
//    public static <T> LambdaUpdateWrapper<T> lambdaUpdate(Class<T> entityClass) {
//        return new MyLambdaUpdateWrapper<>(entityClass);
//    }
}
