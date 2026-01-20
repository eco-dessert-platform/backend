package com.bbangle.bbangle.common.redis.repository;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public interface RedisRepository {

    List<Long> get(String namespace, String key);

    String getString(String namespace, String key);

    List<String> getStringList(String namespace, String key);

    /**
     * Map(Hash) 형태의 데이터를 Redis에서 조회하는 유틸 메서드
     */
    Map<Object, Object> getMap(String namespace, String key);

    /**
     * Redis Value(String) 영역에서 DTO 형태로 데이터를 조회하는 유틸 메서드
     */
    <T> T getDTO(String namespace, String key, Class<T> type);

    /**
     * Redis Value(String) 영역에서 DTO를 조회한 후 즉시 삭제하는 유틸 메서드
     * (GETDEL 기반, 1회성 데이터 소비용)
     */
    <T> T getDTOAndDelete(String namespace, String key, Class<T> type);

    void set(String namespace, String key, String... values);

    void setStringList(String namespace, Map<String, List<String>> values);

    void setFromString(String namespace, String key, String value);

    /**
     * 문자열(String)을 Redis Value 영역에 TTL과 함께 저장하는 유틸 메서드
     */
    void setFromString(String namespace, String key, String value, Duration ttl);

    /**
     * Map<String, Object> 데이터를 Redis Hash 구조로 저장하는 유틸 메서드
     */
    void setFromMap(String namespace, String key, Map<String, Object> values, Duration ttl);

    /**
     * DTO(Record, Map 등)를 JSON으로 직렬화하여
     * Redis Value(String) 영역에 TTL과 함께 저장하는 유틸 메서드
     */
    <T> void setFromDTO(String namespace, String key, T value, Duration ttl);

    void delete(String namespace, String key);

    void deleteAll();

}
