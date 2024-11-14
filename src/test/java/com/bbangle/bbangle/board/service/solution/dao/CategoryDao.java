package com.bbangle.bbangle.board.service.solution.dao;

import com.bbangle.bbangle.board.domain.Category;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryDao implements Dao {
    Category category;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false; // null 또는 다른 클래스면 false

        CategoryDao other = (CategoryDao) obj; // 타입 캐스팅
        return category != null ? category.equals(other.category) : other.category == null; // category 비교
    }

    @Override
    public int hashCode() {
        return category != null ? category.hashCode() : 0;
    }
}
