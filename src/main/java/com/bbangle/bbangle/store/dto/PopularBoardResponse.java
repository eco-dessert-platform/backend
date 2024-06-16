package com.bbangle.bbangle.store.dto;

import com.bbangle.bbangle.board.domain.Category;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PopularBoardResponse {

    private Long boardId;
    private String thumbnail;
    private String title;
    private Integer price;
    private Boolean isWished;
    private Boolean isBundled;

    public static PopularBoardResponse from(PopularBoardDto boardDto,
        Map<Long, Set<Category>> categorys) {
        return PopularBoardResponse.builder()
            .boardId(boardDto.getBoardId())
            .thumbnail(boardDto.getBoardProfile())
            .title(boardDto.getBoardTitle())
            .price(boardDto.getBoardPrice())
            .isWished(boardDto.getIsWished())
            .isBundled(checkCategotyCount(boardDto, categorys))
            .build();
    }

    private static boolean checkCategotyCount(PopularBoardDto boardDto,
        Map<Long, Set<Category>> categorys) {
        Set<Category> targetCategories = categorys.get(boardDto.getBoardId());

        if (Objects.isNull(targetCategories)) {
            return false;
        }

        return categorys.get(boardDto.getBoardId()).size() > 1;
    }

}