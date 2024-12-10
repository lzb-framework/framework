package com.pro.framework.message;

import org.springframework.data.redis.connection.Message;

public interface IApplicationMessageReceiveService<T> {
    void doReceive(T object, Message message);
    String getTopicAppend();
    Class<T> getDataClass();
}
