package com.pro.framework.message;

import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class BaseApplicationMessageSender implements IApplicationMessageService {

//    private IApplicationMessageSerializer applicationMessageSerializer;
    private RedisTemplate<String, Object> redisTemplate;

    public void sendMessageToServers(String topic, Object data) {
        // redisTemplate.convertAndSend 内部已经会对他进行一次serialize了
        redisTemplate.convertAndSend(topic, data);
    }
}
