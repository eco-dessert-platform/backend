package com.bbangle.bbangle.repository;

import java.util.List;

public interface RedisRepository {
    List<Long> get(String namespace, String key);
    List<String> getStringList(String namespace, String key);
    void set(String namespace, String key, String... values);
    void delete(String namespace, String key);
}
