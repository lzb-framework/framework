package com.pro.framework.message;

import com.pro.framework.api.util.AssertUtil;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.lang.NonNull;

import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
@AllArgsConstructor
public class BaseApplicationMessageListener implements MessageListener {
    private Map<String, IApplicationMessageReceiveService> topicServiceMap;
    private IApplicationMessageSerializer serializeService;
//    private ObjectMapper objectMapper;
//    private static final IMessageSerializeService jackson2Serializer = new GenericJackson2JsonRedisSerializer();

    /**
     * 1.通过redis广播到各个订阅端(game-user)
     * 2.各个订阅端(game-user)发送socket消息给客户浏览器
     */
    @Override
    @SneakyThrows
    public void onMessage(@NonNull Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        // 第1级 项目编号 例如
        // 第2级 应用
        // 第3级 实体类和操作
        String[] split = channel.split("_");
        AssertUtil.isTrue(split.length >= 3, "无效topic", channel);
        String messageReceiveServiceName = split[2];

        // 执行
        this.execute(message, messageReceiveServiceName);
    }

    private <T> void execute(Message message, String topicAppend) {
        IApplicationMessageReceiveService<T> service = null == topicServiceMap ? null : topicServiceMap.get(topicAppend);
        if (service != null) {
            // 执行服务 例如 toSocket 对应 toSocketService
            service.doReceive((T) serializeService.deserialize(message.getBody()), message);
        }
    }
}
