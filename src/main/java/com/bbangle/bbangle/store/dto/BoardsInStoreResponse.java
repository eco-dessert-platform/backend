package com.bbangle.bbangle.store.dto;

import com.bbangle.bbangle.board.common.TagUtils;
import com.bbangle.bbangle.board.dao.TagsDao;
import com.bbangle.bbangle.board.domain.Category;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;
import lombok.Builder;

import java.util.List;
import lombok.Getter;

@Getter
@Builder
public class BoardsInStoreResponse {

    private Long boardId;
    private String thumbnail;
    private String title;
    private Integer price;
    private int reviewCount;
    private BigDecimal reviewGrade;
    private Boolean isWished;
    private Boolean isBundled;
    private List<String> tags;

    public static BoardsInStoreResponse from(
        BoardsInStoreDto boardDto,
        List<TagsDao> tags,
        Set<Category> categories
    ) {
        return BoardsInStoreResponse.builder()
            .boardId(boardDto.getBoardId())
            .thumbnail(boardDto.getBoardProfile())
            .title(boardDto.getBoardTitle())
            .price(boardDto.getBoardPrice())
            .reviewCount(boardDto.getReviewCount())
            .reviewGrade(boardDto.getReviewGrade())
            .isWished(boardDto.getIsWished())
            .isBundled(checkMoreThenOne(categories))
            .tags(TagUtils.convertToStrings(tags))
            .build();
    }

    private static boolean checkMoreThenOne(Set<Category> categories) {
        if (Objects.isNull(categories)) {
            return false;
        }

        return categories.size() > 1;
    }
}