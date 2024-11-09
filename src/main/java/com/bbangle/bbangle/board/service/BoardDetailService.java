package com.bbangle.bbangle.board.service;

import com.bbangle.bbangle.board.common.TagUtils;
import com.bbangle.bbangle.board.dto.BoardInfo;
import com.bbangle.bbangle.board.dto.SimilarityBoardDto;
import com.bbangle.bbangle.board.dto.SimilarityBoardResponse;
import com.bbangle.bbangle.board.repository.BoardDetailRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardDetailService {

    private final BoardDetailRepository boardDetailRepository;

    public List<SimilarityBoardResponse> getSimilarityBoardResponses(Long memberId, Long boardId) {
        List<SimilarityBoardDto> similarityBoardDtos = boardDetailRepository.findSimilarityBoardByBoardId(memberId, boardId);

        Map<Long, BoardInfo> boardInfoMap = new HashMap<>();
        for (SimilarityBoardDto dto : similarityBoardDtos) {
            Long currentBoardId = dto.getBoardId();

            BoardInfo boardInfo = boardInfoMap.computeIfAbsent(currentBoardId, id -> new BoardInfo(dto));

            boardInfo.addTag(dto.getTagsDao());
            boardInfo.addCategory(dto.getCategory());
            boardInfo.addIsSoldOut(dto.getIsSoldOut());
        }

        return boardInfoMap.values().stream()
            .map(boardInfo -> {
                List<String> tags = TagUtils.convertToStrings(boardInfo.getTags());
                Boolean isBundled = boardInfo.getCategories().size() > 1;
                Boolean isSoldOut = !boardInfo.getIsSoldOut().contains(false);

                return SimilarityBoardResponse.builder()
                    .boardId(boardInfo.getBoardId())
                    .storeId(boardInfo.getStoreId())
                    .thumbnail(boardInfo.getThumbnail())
                    .storeName(boardInfo.getStoreName())
                    .title(boardInfo.getTitle())
                    .discountRate(boardInfo.getDiscountRate())
                    .price(boardInfo.getPrice())
                    .reviewRate(boardInfo.getReviewRate())
                    .reviewCount(boardInfo.getReviewCount())
                    .tags(tags)
                    .isWished(boardInfo.getIsWished())
                    .isBundled(isBundled)
                    .isSoldOut(isSoldOut)
                    .similarityType(boardInfo.getSimilarityType())
                    .build();
            }).toList();
    }
}

