package com.bbangle.bbangle.board.service;

import com.bbangle.bbangle.board.common.TagUtils;
import com.bbangle.bbangle.board.dto.BoardInfo;
import com.bbangle.bbangle.board.dto.SimilarityBoardDto;
import com.bbangle.bbangle.board.dto.SimilarityBoardResponse;
import com.bbangle.bbangle.board.repository.BoardDetailRepository;
import com.bbangle.bbangle.boardstatistic.repository.BoardStatisticRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardDetailService {

    private static final int RECOMMENDATION_ITEM_COUNT = 3;
    private static final int ZERO_INSUFFICIENT = 0;
    private final BoardStatisticRepository boardStatisticRepository;
    private final BoardDetailRepository boardDetailRepository;

    public List<SimilarityBoardResponse> getSimilarityBoardResponses(Long memberId, Long boardId) {
        List<Long> similarityOrderByBoardIds = new ArrayList<>(
            boardDetailRepository.findSimilarityBoardIdsByNotSoldOut(boardId,
                RECOMMENDATION_ITEM_COUNT));

        addRandomRecommandationBoard(similarityOrderByBoardIds);

        List<SimilarityBoardDto> similarityBoardDtos = boardDetailRepository.findSimilarityBoardByBoardId(
            memberId, similarityOrderByBoardIds);

        Map<Long, BoardInfo> boardInfoMap = new HashMap<>();
        for (SimilarityBoardDto dto : similarityBoardDtos) {
            Long currentBoardId = dto.getBoardId();

            BoardInfo boardInfo = boardInfoMap.computeIfAbsent(currentBoardId,
                id -> new BoardInfo(dto));

            boardInfo.addTag(dto.getTagsDao());
            boardInfo.addCategory(dto.getCategory());
            boardInfo.addIsSoldOut(dto.getIsSoldOut());
        }

        List<SimilarityBoardResponse> boardResponses = boardInfoMap.values().stream()
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
                    .build();
            }).collect(Collectors.toCollection(ArrayList::new));

        boardResponses.sort(
            Comparator.comparingInt(dto -> similarityOrderByBoardIds.indexOf(dto.getBoardId())));

        return boardResponses;
    }

    private void addRandomRecommandationBoard(List<Long> similarityOrderByBoardIds) {
        int insufficientNumber = RECOMMENDATION_ITEM_COUNT - similarityOrderByBoardIds.size();
        if (insufficientNumber > ZERO_INSUFFICIENT) {
            List<Long> popularBoardIds = boardStatisticRepository.findPopularBoardIds(30).stream()
                .filter(id -> !similarityOrderByBoardIds.contains(id))
                .collect(Collectors.toCollection(ArrayList::new));

            Collections.shuffle(popularBoardIds);

            similarityOrderByBoardIds.addAll(popularBoardIds.stream().limit(3).toList());
        }
    }
}

