package com.pro.framework.message;

import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

//@AllArgsConstructor
public class BaseApplicationMessageSerializer<T> implements IApplicationMessageSerializer<T> {
    private JdkSerializationRedisSerializer jdkSerializationRedisSerializer = new JdkSerializationRedisSerializer();
//    private GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer;
//    private Class<T> entityClass;

//    @Override
//    public <T> T deserialize(byte[] body, Class<T> dataClass) {
//        return genericJackson2JsonRedisSerializer.deserialize(body, dataClass);
//    }
//
//    @Override
//    public <T> byte[] serialize(T t) {
//        return genericJackson2JsonRedisSerializer.serialize(t);
//    }

    @Override
    public byte[] serialize(Object t) throws SerializationException {
        return jdkSerializationRedisSerializer.serialize(t);
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        return (T) jdkSerializationRedisSerializer.deserialize(bytes);
    }
}
