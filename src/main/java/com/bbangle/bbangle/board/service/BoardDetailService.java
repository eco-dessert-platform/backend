package com.bbangle.bbangle.board.service;

import com.bbangle.bbangle.board.common.TagUtils;
import com.bbangle.bbangle.board.dao.TagsDao;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.dto.SimilarityBoardDto;
import com.bbangle.bbangle.board.dto.SimilarityBoardResponse;
import com.bbangle.bbangle.board.repository.BoardDetailRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardDetailService {

    private final BoardDetailRepository boardDetailRepository;

    public List<SimilarityBoardResponse> getSimilarityBoardResponses(Long memberId, Long boardId) {
        List<SimilarityBoardDto> similarityBoardDtos = boardDetailRepository.findSimilarityBoardByBoardId(
            memberId,
            boardId);

        Map<Long, List<TagsDao>> categorizedTagByBoardId = similarityBoardDtos.stream()
            .collect(Collectors.groupingBy(
                SimilarityBoardDto::getBoardId,
                Collectors.mapping(SimilarityBoardDto::getTagsDao,
                    Collectors.toList())
            ));

        Map<Long, Set<Category>> categorizedCategoryByBoardId = similarityBoardDtos.stream()
            .collect(Collectors.groupingBy(
                SimilarityBoardDto::getBoardId,
                Collectors.mapping(SimilarityBoardDto::getCategory,
                    Collectors.toSet())
            ));

        Map<Long, Set<Boolean>> categorizedIsSoldOutByBoardId = similarityBoardDtos.stream()
            .collect(Collectors.groupingBy(
                SimilarityBoardDto::getBoardId,
                Collectors.mapping(SimilarityBoardDto::getIsSoldOut,
                    Collectors.toSet())
            ));

        return similarityBoardDtos.stream()
            .collect(Collectors.toMap(
                SimilarityBoardDto::getBoardId, // 키는 getBoardId 값
                dto -> dto,                      // 값은 원래 dto 객체
                (existing, replacement) -> existing // 중복 키가 있을 경우 기존 값 유지
            ))
            .values()
            .stream()
            .map(similarityBoardDto -> {
                Long boardId1 = similarityBoardDto.getBoardId();

                List<TagsDao> tagsDaos = categorizedTagByBoardId.get(boardId1);
                Set<Category> categories = categorizedCategoryByBoardId.get(boardId1);
                Set<Boolean> soldout = categorizedIsSoldOutByBoardId.get(boardId1);

                List<String> tags = TagUtils.convertToStrings(tagsDaos);
                Boolean isBundled = categories.size() > 1;
                Boolean isSoldOut = soldout.size() == 1 && soldout.iterator().next() == true;

                return SimilarityBoardResponse.builder()
                    .boardId(similarityBoardDto.getBoardId())
                    .storeId(similarityBoardDto.getStoreId())
                    .thumbnail(similarityBoardDto.getThumbnail())
                    .storeName(similarityBoardDto.getStoreName())
                    .title(similarityBoardDto.getTitle())
                    .discountRate(similarityBoardDto.getDiscountRate())
                    .price(similarityBoardDto.getPrice())
                    .reviewRate(similarityBoardDto.getReviewRate())
                    .reviewCount(similarityBoardDto.getReviewCount())
                    .tags(tags)
                    .isWished(similarityBoardDto.getIsWished())
                    .isBundled(isBundled)
                    .isSoldOut(isSoldOut)
                    .similarityType(similarityBoardDto.getSimilarityType())
                    .build();
            }).toList();
    }

}

