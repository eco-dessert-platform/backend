package com.bbangle.bbangle.review.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Builder
@Getter
public class ReviewInfoResponse {
    private Long id;
    private List<String> images;
    private String nickname;
    private BigDecimal rating;
    private Boolean isBest;
    private List<String> tags;
    private Integer like;
    private Boolean isLiked;
    private String comment;
    private LocalDateTime date;

    public static List<ReviewInfoResponse> createList(
            List<ReviewSingleDto> reviewSingleList,
            Map<Long, List<String>> imageMap,
            Map<Long, List<String>> tagMap,
            Map<Long, List<Long>> likeMap,
            final Long memberId) {
       return reviewSingleList.stream()
                .map(reviewSingle -> toReviewInfo(reviewSingle, imageMap, tagMap, likeMap, memberId))
                .toList();
    }

    public static ReviewInfoResponse create(
            ReviewSingleDto reviewSingle,
            Map<Long, List<String>> imageMap,
            Map<Long, List<String>> tagMap,
            Map<Long, List<Long>> likeMap,
            final Long memberId) {
        return toReviewInfo(reviewSingle, imageMap, tagMap, likeMap, memberId);
    }

    private static ReviewInfoResponse toReviewInfo(ReviewSingleDto reviewSingleDto,
                                                   Map<Long, List<String>> imageMap,
                                                   Map<Long, List<String>> tagMap,
                                                   Map<Long, List<Long>> likeMap,
                                                   final Long memberId){
        return ReviewInfoResponse.builder()
                .id(reviewSingleDto.id())
                .images(imageMap.get(reviewSingleDto.id()))
                .nickname(reviewSingleDto.nickname())
                .rating(reviewSingleDto.rate())
                //FIXME 베스트 리뷰 추가하기
                .isBest(false)
                .like(getLikeCount(reviewSingleDto, likeMap))
                .isLiked(isContainMember(reviewSingleDto, likeMap, memberId))
                .tags(tagMap.get(reviewSingleDto.id()))
                .comment(reviewSingleDto.content())
                .date(reviewSingleDto.createdAt())
                .build();
    }

    private static boolean isContainMember(ReviewSingleDto reviewSingleDto, Map<Long, List<Long>> likeMap, Long memberId) {
        List<Long> likeMapValue = likeMap.get(reviewSingleDto.id());
        return likeMapValue != null && likeMapValue.contains(memberId);
    }

    private static int getLikeCount(ReviewSingleDto reviewSingleDto, Map<Long, List<Long>> likeMap) {
        List<Long> likeMapValue = likeMap.get(reviewSingleDto.id());
        return likeMapValue != null ? likeMapValue.size() : 0;
    }
}
