package com.tracker.AIService.config;

import com.tracker.AIService.models.dto.GoalDraft;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;


@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public RedisTemplate<String, GoalDraft> redisTemplate(
            RedisConnectionFactory factory) {

        RedisTemplate<String, GoalDraft> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        RedisSerializer<GoalDraft> jsonSerializer = new RedisSerializer<>() {

            @Override
            public byte[] serialize(GoalDraft draft) throws SerializationException {
                return objectMapper.writeValueAsBytes(draft);
            }

            @Override
            public GoalDraft deserialize(byte[] bytes) throws SerializationException {
                if (bytes == null) return null;
                return objectMapper.readValue(bytes, GoalDraft.class);
            }
        };

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jsonSerializer);

        return template;
    }
}