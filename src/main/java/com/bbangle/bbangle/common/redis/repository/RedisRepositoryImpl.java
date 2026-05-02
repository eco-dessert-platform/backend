package com.bbangle.bbangle.common.redis.repository;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisRepositoryImpl implements RedisRepository {

    @Qualifier("defaultRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;

    @Qualifier("dtoRedisTemplate")
    private final RedisTemplate<String, Object> dtoRedisTemplate;

    @Override
    public List<Long> get(String namespace, String key) {
        String multiKey = namespace + ":" + key;
        return redisTemplate.opsForList()
            .range(multiKey, 0, -1)
            .stream()
            .map(object -> Long.parseLong(object.toString()))
            .toList();
    }

    @Override
    public List<String> getStringList(String namespace, String key) {
        String multiKey = namespace + ":" + key;
        return redisTemplate.opsForList()
            .range(multiKey, 0, -1)
            .stream()
            .map(Object::toString)
            .toList();
    }

    @Override
    public Map<Object, Object> getMap(String namespace, String key) {
        String multiKey = namespace + ":" + key;
        return redisTemplate.opsForHash().entries(multiKey);
    }

    @Override
    public <T> T getDTO(String namespace, String key, Class<T> type) {
        String multiKey = namespace + ":" + key;
        Object value = dtoRedisTemplate.opsForValue().get(multiKey);

        if (value == null) return null;

        if (!type.isInstance(value)) {
            log.error("Redis 타입 불일치 - 기댓값={} 실제값={}", type, value.getClass());
            return null;
        }

        return type.cast(value);
    }

    @Override
    public <T> T getDTOAndDelete(String namespace, String key, Class<T> type) {
        String multiKey = namespace + ":" + key;
        Object value = dtoRedisTemplate.opsForValue().getAndDelete(multiKey);

        if (value == null) return null;

        if (!type.isInstance(value)) {
            log.error("Redis 타입 불일치 - 기댓값={} 실제값={}", type, value.getClass());
            return null;
        }

        return type.cast(value);
    }

    @Override
    public String getString(String namespace, String key) {
        String multiKey = namespace + ":" + key;
        Object value = redisTemplate.opsForValue().get(multiKey);
        return Optional.ofNullable(value)
            .map(Object::toString)
            .orElse("");
    }

    @Override
    public void set(String namespace, String key, String... values) {
        String multiKey = namespace + ":" + key;
        redisTemplate.opsForList()
            .rightPushAll(multiKey, values);
    }

    @Override
    public void setStringList(String namespace, Map<String, List<String>> values) {
        for (Entry<String, List<String>> valueList : values.entrySet()) {
            String multiKey = namespace + ":" + valueList.getKey();

            for (String value : valueList.getValue()) {
                redisTemplate.opsForList().rightPush(multiKey, value);
            }
        }
    }

    @Override
    public void setFromString(String namespace, String key, String value) {
        String multiKey = namespace + ":" + key;
        redisTemplate.opsForValue()
            .set(multiKey, value);
    }

    @Override
    public void setFromString(String namespace, String key, String value, Duration ttl) {
        String multiKey = namespace + ":" + key;
        redisTemplate.opsForValue()
                .set(multiKey, value, ttl);
    }

    @Override
    public void setFromMap(String namespace, String key, Map<String, Object> value, Duration ttl) {
        String multiKey = namespace + ":" + key;
        redisTemplate.opsForHash()
            .putAll(multiKey, value);
        redisTemplate.expire(multiKey, ttl);
    }

    @Override
    public <T> void setFromDTO(String namespace, String key, T value, Duration ttl) {
        String multiKey = namespace + ":" + key;

        dtoRedisTemplate.opsForValue().set(multiKey, value, ttl);
    }

    @Override
    public void delete(String namespace, String key) {
        String multiKey = namespace + ":" + key;
        redisTemplate.delete(multiKey);
    }

    @Override
    public void deleteAll() {
        Set<String> keys = redisTemplate.keys("*");
        redisTemplate.delete(keys);
    }
}