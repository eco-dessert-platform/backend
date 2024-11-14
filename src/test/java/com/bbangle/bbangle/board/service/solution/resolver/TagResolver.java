package com.bbangle.bbangle.board.service.solution.resolver;

import com.bbangle.bbangle.board.domain.TagEnum;
import com.bbangle.bbangle.board.service.solution.dao.TagsDao;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TagResolver implements Resolver<List<TagsDao>, List<String>> {
    @Override
    public List<String> resolve(List<TagsDao> tags) {
        Set<String> tagStrings = new HashSet<>();

        Set<TagsDao> duplicationTags = tags.stream()
            .collect(Collectors.toSet());

        for (TagsDao tag : duplicationTags) {
            if (tag.isGlutenFreeTag()) {
                tagStrings.add(TagEnum.GLUTEN_FREE.label());
            }

            if (tag.isSugarFreeTag()) {
                tagStrings.add(TagEnum.SUGAR_FREE.label());
            }

            if (tag.isHighProteinTag()) {
                tagStrings.add(TagEnum.HIGH_PROTEIN.label());
            }

            if (tag.isVeganTag()) {
                tagStrings.add(TagEnum.VEGAN.label());
            }

            if (tag.isKetogenicTag()) {
                tagStrings.add(TagEnum.KETOGENIC.label());
            }
        }

        return tagStrings.stream().toList();
    }
}

