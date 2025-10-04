package com.bbangle.bbangle.review.customer.dto;

import com.bbangle.bbangle.image.dto.ImageDto;
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
    private List<ImageDto> images;
    private String nickname;
    private BigDecimal rating;
    private List<String> tags;
    private Integer like;
    private Boolean isLiked;
    private String comment;
    private LocalDateTime date;
    private Boolean isBest;
    private Long boardId;
    private Boolean isMine;

    public static List<ReviewInfoResponse> createList(
            List<ReviewSingleDto> reviewSingleList,
            Map<Long, List<ImageDto>> imageMap,
            Map<Long, List<String>> tagMap,
            Map<Long, List<Long>> likeMap,
            final Long memberId) {
       return reviewSingleList.stream()
                .map(reviewSingle -> toReviewInfo(reviewSingle, imageMap, tagMap, likeMap, memberId))
                .toList();
    }

    public static ReviewInfoResponse create(
            ReviewSingleDto reviewSingle,
            Map<Long, List<ImageDto>> imageMap,
            Map<Long, List<String>> tagMap,
            Map<Long, List<Long>> likeMap,
            final Long memberId) {
        return toReviewInfo(reviewSingle, imageMap, tagMap, likeMap, memberId);
    }

    private static ReviewInfoResponse toReviewInfo(ReviewSingleDto reviewSingleDto,
                                                   Map<Long, List<ImageDto>> imageMap,
                                                   Map<Long, List<String>> tagMap,
                                                   Map<Long, List<Long>> likeMap,
                                                   final Long memberId){
        List<ImageDto> imageDtos = imageMap.get(reviewSingleDto.id()) != null ?
                imageMap.get(reviewSingleDto.id()) : null;
        return ReviewInfoResponse.builder()
                .id(reviewSingleDto.id())
                .images(imageDtos)
                .nickname(reviewSingleDto.nickname())
                .rating(reviewSingleDto.rate())
                .isBest(reviewSingleDto.isBest())
                .like(getLikeCount(reviewSingleDto, likeMap))
                .isLiked(isContainMember(reviewSingleDto, likeMap, memberId))
                .tags(tagMap.get(reviewSingleDto.id()))
                .comment(reviewSingleDto.content())
                .date(reviewSingleDto.createdAt())
                .boardId(reviewSingleDto.boardId())
                .isMine(reviewSingleDto.memberId().equals(memberId))
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
