package com.bbangle.bbangle.board.dto;

import com.bbangle.bbangle.board.common.TagUtils;
import com.bbangle.bbangle.board.dao.TagsDao;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import lombok.Getter;

@Getter
public class BoardInfoDto {

    private Long boardId;
    private String thumbnail;
    private String title;
    private Integer price;
    private Integer discountRate;
    BigDecimal reviewRate;
    Long reviewCount;
    private Boolean isSoldOut;
    private Boolean isBbangcketing;
    private List<String> tags;
    private Boolean isBundled;
    private Boolean isWished;

    public BoardInfoDto(
        Long boardId,
        String boardProfile,
        String boardTitle,
        Integer boardPrice,
        Integer boardDiscount,
        BigDecimal boardReviewGrade,
        Long boardReviewCount,
        Integer isSoldOut,
        Boolean isNotification,
        Integer glutenFreeTag,
        Integer highProteinTag,
        Integer sugarFreeTag,
        Integer veganTag,
        Integer ketogenicTag,
        Boolean isBundled,
        Boolean isWished
    ) {
        this.boardId = boardId;
        this.thumbnail = boardProfile;
        this.title = boardTitle;
        this.price = boardPrice;
        this.discountRate = boardDiscount;
        this.reviewRate = boardReviewGrade;
        this.reviewCount = boardReviewCount;
        this.isSoldOut = isSoldOut > 0;
        this.isBbangcketing = isNotification;
        this.isBundled = isBundled;
        this.isWished = isWished;

        this.tags = createTags(glutenFreeTag, highProteinTag, sugarFreeTag, veganTag, ketogenicTag);
    }

    private List<String> createTags(
        Integer glutenFreeTag,
        Integer highProteinTag,
        Integer sugarFreeTag,
        Integer veganTag,
        Integer ketogenicTag
    ) {
        return TagUtils.convertToStrings(
            Collections.singletonList(TagsDao.builder()
                .glutenFreeTag(glutenFreeTag > 0)
                .highProteinTag(highProteinTag > 0)
                .sugarFreeTag(sugarFreeTag > 0)
                .veganTag(veganTag > 0)
                .ketogenicTag(ketogenicTag > 0)
                .build())
        );
    }
}
