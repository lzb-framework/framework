package com.pro.framework.message;


import org.springframework.data.redis.serializer.RedisSerializer;

public interface IApplicationMessageSerializer<T> extends RedisSerializer<T> {
}
