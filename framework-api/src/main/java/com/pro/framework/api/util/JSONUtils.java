package com.pro.framework.api.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.SneakyThrows;

/**
 * hutool toBean 会忽视 transient的属性  比如  transient private String tkPassword;
 */
public class JSONUtils {

    private static ObjectMapper objectMapper;
    private static ObjectReader objectReader;
    private static ObjectWriter objectWriter;

    public static void init(ObjectMapper objectMapper) {
        JSONUtils.objectMapper = objectMapper;
        JSONUtils.objectReader = objectMapper.reader();
        JSONUtils.objectWriter = objectMapper.writer();
    }

    @SneakyThrows
    public static String toString(Object o) {
        return objectWriter.writeValueAsString(o);
    }

    @SneakyThrows
    public static <T> T fromString(String str, Class<T> clazz) {
        return objectMapper.readerFor(clazz).readValue(str);
    }

    @SneakyThrows
    public static <T> T fromString(String str, TypeReference<T> typeReference) {
        return objectMapper.readerFor(typeReference).readValue(str);
    }
}
