package com.bbangle.bbangle.board.service.solution.generator;

import com.bbangle.bbangle.board.service.solution.dao.CategoryDao;
import java.util.List;

public class CategoryResolver implements Resolver<List<CategoryDao>, Boolean> {

    private static final int ONE_CATEGORY = 1;

    @Override
    public Boolean resolve(List<CategoryDao> categories) {
        return categories.stream().distinct().count() > ONE_CATEGORY;
    }
}
