package com.bbangle.bbangle.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.testcontainers.containers.GenericContainer;

@Slf4j
@Configuration
@EnableRedisRepositories
@Profile("test")
public class RedisTestConfig {

    // 컨테이너 Bean 주입 → 포트 직접 사용
    // GenericContainer<?> redisContainer 파라미터 주입:
    // 스프링이 TestContainersConfig의 @Bean redisContainer()를 먼저 만들고 여기를 주입합니다.
    @Bean
    public RedisConnectionFactory redisConnectionFactory(GenericContainer<?> redisContainer) {
        String host = redisContainer.getHost();
        int port = redisContainer.getMappedPort(6379);
        log.info("✅ Redis CF created (host={}, port={})", host, port);
        return new LettuceConnectionFactory(host, port);
    }

    /**
     * 기본 RedisTemplate Key, Value 모두 String 직렬화 redis-cli를 통해 직접 데이터를 조회할 수 있도록 설정
     */

    @Bean(name = "defaultRedisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }


    /**
     * 업데이트용 RedisTemplate Value를 JSON으로 직렬화
     */

    @Bean(name = "updateRedisTemplate")
    public RedisTemplate<String, Object> updateRedisTemplate(
        RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }


    /**
     * 랜덤용 RedisTemplate Integer 값을 JSON으로 직렬화
     */

    @Bean(name = "randomRedisTemplate")
    public RedisTemplate<String, Integer> randomRedisTemplate(
        RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Integer> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }
}

