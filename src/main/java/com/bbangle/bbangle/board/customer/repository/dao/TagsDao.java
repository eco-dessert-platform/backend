package com.bbangle.bbangle.board.customer.repository.dao;

import java.util.Objects;
import lombok.Builder;

@Builder
public record TagsDao(
    boolean glutenFreeTag,
    boolean highProteinTag,
    boolean sugarFreeTag,
    boolean veganTag,
    boolean ketogenicTag
) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TagsDao that = (TagsDao) o;
        return Objects.equals(glutenFreeTag, that.glutenFreeTag) &&
            Objects.equals(highProteinTag, that.highProteinTag) &&
            Objects.equals(sugarFreeTag, that.sugarFreeTag) &&
            Objects.equals(veganTag, that.veganTag) &&
            Objects.equals(ketogenicTag, that.ketogenicTag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(glutenFreeTag, highProteinTag, sugarFreeTag, veganTag, ketogenicTag);
    }
}
