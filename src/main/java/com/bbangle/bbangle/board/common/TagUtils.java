package com.bbangle.bbangle.board.common;

import com.bbangle.bbangle.board.dao.TagsDao;
import com.bbangle.bbangle.board.domain.TagEnum;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TagUtils {

    public static List<String> convertToStrings(List<TagsDao> tags) {
        Set<String> tagStrings = new HashSet<>();

        Set<TagsDao> duplicationTags = tags.stream()
            .collect(Collectors.toSet()); // 아예 똑같은 태그 DAO 중복 제거

        for (TagsDao tag : duplicationTags) {
            if (tag.glutenFreeTag()) {
                tagStrings.add(TagEnum.GLUTEN_FREE.label());
            }

            if (tag.sugarFreeTag()) {
                tagStrings.add(TagEnum.SUGAR_FREE.label());
            }

            if (tag.highProteinTag()) {
                tagStrings.add(TagEnum.HIGH_PROTEIN.label());
            }

            if (tag.veganTag()) {
                tagStrings.add(TagEnum.VEGAN.label());
            }

            if (tag.ketogenicTag()) {
                tagStrings.add(TagEnum.KETOGENIC.label());
            }
        }

        return tagStrings.stream().toList();
    }
}

