package com.bbangle.bbangle.common.redis.repository;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public interface RedisRepository {

    List<Long> get(String namespace, String key);

    String getString(String namespace, String key);

    List<String> getStringList(String namespace, String key);

    Map<Object, Object> getMap(String namespace, String key);

    void set(String namespace, String key, String... values);

    void setStringList(String namespace, Map<String, List<String>> values);

    void setFromString(String namespace, String key, String value);

    void setFromString(String namespace, String key, String value, Duration ttl);

    void setFromMap(String namespace, String key, Map<String, Object> values, Duration ttl);

    void delete(String namespace, String key);

    void deleteAll();

}
