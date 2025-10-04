package com.bbangle.bbangle.review.domain;

import com.bbangle.bbangle.review.customer.dto.ReviewSingleDto;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Component
public class ReviewManager {
    public Map<Long, List<String>> getTagMap(ReviewSingleDto reviewSingle,
                                             Map<Long, List<String>> tagMap) {
        String tasteAction = reviewSingle.badgeTaste().action();
        String brixAction = reviewSingle.badgeBrix().action();
        String textureAction = reviewSingle.badgeTexture().action();
        List<String> tags = List.of(tasteAction, brixAction, textureAction);
        tags = tags.stream()
                .filter(tag -> !tag.equals(Badge.NULL.action()))
                .toList();
        tagMap.put(reviewSingle.id(), tags);
        return tagMap;
    }

    public Map<Long, List<Long>> getLikeMap(List<ReviewLike> likeList) {
        return likeList
                .stream()
                .collect(Collectors.toMap(
                        ReviewLike::getReviewId,
                        like -> {
                            List<Long> members = new ArrayList<>();
                            members.add(like.getMemberId());
                            return members;
                        },
                        (existMembers, newMembers) -> {
                            existMembers.addAll(newMembers);
                            return existMembers;
                        }
                ));
    }
}
