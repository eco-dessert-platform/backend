package com.bbangle.bbangle.common.redis.repository;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisRepositoryImpl implements RedisRepository {

    @Qualifier("defaultRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;

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