package com.bbangle.bbangle.store.dto;

import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.TagEnum;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;

@Builder
public record StoreDetailProductDto(
    Long boardId,
    Long productId,
    Boolean glutenFreeTag,
    Boolean highProteinTag,
    Boolean sugarFreeTag,
    Boolean veganTag,
    Boolean ketogenicTag,
    Category category
) {

    public List<String> toTags() {
        List<String> tags = new ArrayList<>(5);
        if (this.glutenFreeTag) {
            tags.add(TagEnum.GLUTEN_FREE.label());
        }
        if (this.highProteinTag) {
            tags.add(TagEnum.HIGH_PROTEIN.label());
        }
        if (this.sugarFreeTag) {
            tags.add(TagEnum.SUGAR_FREE.label());
        }
        if (this.veganTag) {
            tags.add(TagEnum.VEGAN.label());
        }
        if (this.ketogenicTag) {
            tags.add(TagEnum.KETOGENIC.label());
        }

        return tags;
    }
}