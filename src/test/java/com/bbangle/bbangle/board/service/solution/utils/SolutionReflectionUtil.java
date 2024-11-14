package com.bbangle.bbangle.board.service.solution.utils;

import com.bbangle.bbangle.board.service.solution.domain.SolutionEnum;
import com.bbangle.bbangle.board.service.solution.factory.SolutionDaoFactory;
import java.util.Map;
import java.util.Map.Entry;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SolutionReflectionUtil {
    public static  <T> Object getField(T data, SolutionEnum solutionEnum) {
        Map<Class<?>, String> result = SolutionDaoFactory.getResolver(solutionEnum);
        Entry<Class<?>, String> item = result.entrySet().iterator().next();

        Class<?> daoClass = item.getKey();
        String reflectionData = item.getValue();

        return ReflectionUtil.getField(data, reflectionData, daoClass);
    }
}
