package com.bbangle.bbangle.board.service.solution.factory;

import com.bbangle.bbangle.board.service.solution.domain.SolutionEnum;
import com.bbangle.bbangle.board.service.solution.resolver.CategoryResolver;
import com.bbangle.bbangle.board.service.solution.resolver.Resolver;
import com.bbangle.bbangle.board.service.solution.resolver.SoldOutResolver;
import com.bbangle.bbangle.board.service.solution.resolver.TagResolver;
import java.util.EnumMap;
import java.util.Map;

public class SolutionResolverFactory {
    private static final Map<SolutionEnum, Resolver> processableMap = new EnumMap<>(SolutionEnum.class);

    static {
        processableMap.put(SolutionEnum.태그, new TagResolver());
        processableMap.put(SolutionEnum.묶음상품, new CategoryResolver());
        processableMap.put(SolutionEnum.품절여부, new SoldOutResolver());
    }

    public static Resolver getResolver(SolutionEnum type) {
        return processableMap.get(type);
    }
}
