package com.bbangle.bbangle.board.dto;

import com.bbangle.bbangle.board.common.TagUtils;
import com.bbangle.bbangle.board.dao.TagsDao;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import com.bbangle.bbangle.board.domain.Board;
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

    public void updateIsWished(boolean isWished) {
        this.isWished = isWished;
    };

    public BoardInfoDto(Long boardId, String thumbnail, String title, Integer price, Integer discountRate, BigDecimal reviewRate, Long reviewCount, Boolean isSoldOut, Boolean isBbangcketing, List<String> tags, Boolean isBundled, Boolean isWished) {
        this.boardId = boardId;
        this.thumbnail = thumbnail;
        this.title = title;
        this.price = price;
        this.discountRate = discountRate;
        this.reviewRate = reviewRate;
        this.reviewCount = reviewCount;
        this.isSoldOut = isSoldOut;
        this.isBbangcketing = isBbangcketing;
        this.tags = tags;
        this.isBundled = isBundled;
        this.isWished = isWished;
    }

    /* todo::설명하고 삭제한 후 merge
    * product 관련 내용은 isSoldOut, isBbangketing, getTags, isBundled
    * 호출 시 영속성 컨텍스트에서 값을 확인해서 꺼내옴
    * 없을 시, DB에서 조회
    *     이때, N+1 발생
    *     그렇기에 batch-size를 500으로 설정
    *          batch-size 설정 시 필요한 product 엔티티 전체를 in 절로 가져옴
    * */
    public static BoardInfoDto create(Board board) {
        return new BoardInfoDto(
            board.getId(),
            board.getThumbnail(),
            board.getTitle(),
            board.getPrice(),
            board.getDiscountRate(),
            board.getBoardStatistic().getBoardReviewGrade(),
            board.getBoardStatistic().getBoardReviewCount(),
            board.isSoldOut(),
            board.isBbangketing(),
            board.getTags(),
            board.isBundled(),
            false
        );
    }

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
