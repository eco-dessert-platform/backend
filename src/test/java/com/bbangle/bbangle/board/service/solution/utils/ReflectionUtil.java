package com.bbangle.bbangle.board.service.solution.utils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectionUtil {
    private static final Map<Class<?>, Map<String, Field>> fieldCache = new ConcurrentHashMap<>();

    public static <T, U> T getField(U instance, String name, Class<T> t) {
        try {
            Field field = getCachedField(instance.getClass(), name);
            field.setAccessible(true);

            return t.cast(field.get(instance));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Field getCachedField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Map<String, Field> classFieldMap = fieldCache.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>());

        return classFieldMap.computeIfAbsent(fieldName, name -> {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
