package com.bbangle.bbangle.board.service.solution.dao;

import com.bbangle.bbangle.board.domain.Category;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryDao implements Dao {
    Category category;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (Objects.isNull(obj) || getClass() != obj.getClass()) return false;

        CategoryDao other = (CategoryDao) obj;
        return Objects.nonNull(category) ? category.equals(other.category) : Objects.isNull(other.category);
    }

    @Override
    public int hashCode() {
        return Objects.nonNull(category) ? category.hashCode() : 0;
    }
}
