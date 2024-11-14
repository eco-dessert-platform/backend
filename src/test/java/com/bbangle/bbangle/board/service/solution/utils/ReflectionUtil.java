package com.bbangle.bbangle.board.service.solution.utils;

public class ReflectionUtil {
    public static <T, U> T getField(U instance, String name, Class<T> t) {
        try {
            Class<?> dtoClass = instance.getClass();
            var field = dtoClass.getDeclaredField(name);
            field.setAccessible(true);

            return t.cast(field.get(instance));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
