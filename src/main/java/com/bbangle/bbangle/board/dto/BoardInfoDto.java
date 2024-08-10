package com.bbangle.bbangle.board.dto;

import com.bbangle.bbangle.board.dao.TagsDao;
import lombok.Getter;

@Getter
public class BoardInfoDto {

    private Long boardId;
    private String boardProfile;
    private String boardTitle;
    private Integer boardPrice;
    private Boolean isSoldOut;
    private Boolean isNotification;
    private TagsDao tags;
    private Boolean isBundled;
    private Boolean isWished;

    public BoardInfoDto(
        Long boardId,
        String boardProfile,
        String boardTitle,
        Integer boardPrice,
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
        this.boardProfile = boardProfile;
        this.boardTitle = boardTitle;
        this.boardPrice = boardPrice;
        this.isSoldOut = isSoldOut > 0;
        this.isNotification = isNotification;
        this.isBundled = isBundled;
        this.isWished = isWished;
        this.tags = TagsDao.builder()
            .glutenFreeTag(glutenFreeTag > 0)
            .sugarFreeTag(sugarFreeTag > 0)
            .highProteinTag(highProteinTag > 0)
            .veganTag(veganTag > 0)
            .ketogenicTag(ketogenicTag > 0)
            .build();
    }
}
