package com.team4.moin.common.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CachingConfig {

    @Bean
    public RedisCacheManager cacheManager(@Qualifier("crewCaching") RedisConnectionFactory connectionFactory){

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
//                .entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                );

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("popularCrews",      defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("crewRecommendation",defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigs.put("crewSoaring",       defaultConfig.entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig.entryTtl(Duration.ofMinutes(10))) // 기본값
                .withInitialCacheConfigurations(cacheConfigs) // 개별 설정
                .build();
    }
}
