package com.pro.framework.api.message;

public interface IBaseMessageService {
    void sendMessageToServers(String topic, Object message);

//    default void sendMessage(String topic, Object message) {
//        sendMessage(topic, JSONUtils.toJsonStr(message));
//    }
}
