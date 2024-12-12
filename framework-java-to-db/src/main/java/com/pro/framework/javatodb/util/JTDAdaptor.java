package com.pro.framework.javatodb.util;

import lombok.SneakyThrows;

import java.sql.ResultSet;
import java.util.List;
import java.util.function.Function;

public interface JTDAdaptor {
    @SneakyThrows
    <T> List<T> executeQuery(String sql, Function<ResultSet, T> function);

    @SneakyThrows
    boolean execute(String sql);

    boolean createDatabase();
}
