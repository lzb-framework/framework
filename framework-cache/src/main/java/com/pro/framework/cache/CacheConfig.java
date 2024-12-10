package com.pro.framework.cache;

import com.pro.framework.api.message.EnumTopicFramework;
import com.pro.framework.api.message.IBaseMessageService;
import com.pro.framework.message.IApplicationMessageSerializer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 1级本地缓存
     */
    @Bean
    public CaffeineCacheManager cacheManagerLocal() {
        return new CaffeineCacheManager();
    }

    /**
     * 2级redis中央缓存
     */
    @Bean
    public RedisCacheManager cacheManagerCenter(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
//                .serializeValuesWith(
//                        RedisSerializationContext.SerializationPair.fromSerializer(serializer())
//                )
                .entryTtl(Duration.ofHours(12));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfiguration)
                .build();
    }

    /**
     * 合并1级+2级缓存的缓存管理器
     */
    @Bean
    @Primary
    public CombinedCacheManager cacheManager(CacheManager cacheManagerLocal, CacheManager cacheManagerCenter, IBaseMessageService messageService) {
        return new CombinedCacheManager(Arrays.asList(cacheManagerLocal, cacheManagerCenter), messageService, new ConcurrentHashMap<>());
    }

    /**
     * 监听redis消息(缓存增删改事件)
     */
    @Bean
    public RedisMessageListenerContainer redisContainer(CombinedCacheManager cacheManager, RedisTemplate<String, ?> redisTemplate, IApplicationMessageSerializer serializer) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisTemplate.getConnectionFactory());
        CacheMessageListener listener = new CacheMessageListener(cacheManager.getCacheManagers());
        listener.setSerializer(serializer);
        container.addMessageListener(listener, new ChannelTopic(EnumTopicFramework.framework_cache.name()));
        return container;
    }

//    @Bean
//    @ConditionalOnMissingBean
//    public RedisSerializer<?> serializer() {
////        return new BaseApplicationMessageSerializeService();
//        return new JdkSerializationRedisSerializer();
//    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        final RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
//        redisTemplate.setValueSerializer(serializer());
        return redisTemplate;
    }
}
