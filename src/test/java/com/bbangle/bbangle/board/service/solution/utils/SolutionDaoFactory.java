package com.bbangle.bbangle.board.service.solution.utils;

import com.bbangle.bbangle.board.service.domain.SolutionEnum;
import com.bbangle.bbangle.board.service.solution.dao.CategoryDao;
import com.bbangle.bbangle.board.service.solution.dao.SoldOutDao;
import com.bbangle.bbangle.board.service.solution.dao.TagsDao;
import java.util.EnumMap;
import java.util.Map;

public class SolutionDaoFactory {
    private static final Map<SolutionEnum, Map<Class<?>, String>> processableMap = new EnumMap<>(SolutionEnum.class);
    static {
        processableMap.put(SolutionEnum.태그, Map.of(TagsDao.class, "tagsDao"));
        processableMap.put(SolutionEnum.품절여부, Map.of(SoldOutDao.class, "soldOutDao"));
        processableMap.put(SolutionEnum.묶음상품, Map.of(CategoryDao.class, "categoryDao"));
    }

    public static Map<Class<?>, String> getResolver(SolutionEnum type) {
        return processableMap.get(type);
    }
}
