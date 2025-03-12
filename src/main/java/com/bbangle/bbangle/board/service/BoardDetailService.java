package com.bbangle.bbangle.board.service;

import static com.bbangle.bbangle.board.validator.BoardValidator.validateListNotEmpty;
import static com.bbangle.bbangle.exception.BbangleErrorCode.BOARD_NOT_FOUND;
import static com.bbangle.bbangle.exception.BbangleErrorCode.IMAGE_URL_NULL;

import com.bbangle.bbangle.board.common.TagUtils;
import com.bbangle.bbangle.board.domain.ViewCount;
import com.bbangle.bbangle.board.dto.BoardAndImageDto;
import com.bbangle.bbangle.board.dto.BoardDto;
import com.bbangle.bbangle.board.dto.BoardImageDetailResponse;
import com.bbangle.bbangle.board.dto.BoardInfo;
import com.bbangle.bbangle.board.dto.SimilarityBoardDto;
import com.bbangle.bbangle.board.dto.SimilarityBoardResponse;
import com.bbangle.bbangle.board.repository.BoardDetailRepository;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.service.component.ViewCountComponent;
import com.bbangle.bbangle.boardstatistic.service.BoardStatisticService;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.util.HtmlUtils;
import com.bbangle.bbangle.wishlist.repository.WishListBoardRepository;
import com.bbangle.bbangle.boardstatistic.repository.BoardStatisticRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardDetailService {

    @Value("${cdn.domain}")
    private String cdn;
    private static final String HTTP = "http";

    private final WishListBoardRepository wishListBoardRepository;
    private final ViewCountComponent viewCountComponent;
    private static final int RECOMMENDATION_ITEM_COUNT = 3;
    private static final int ZERO_INSUFFICIENT = 0;
    private final BoardStatisticRepository boardStatisticRepository;
    private final BoardDetailRepository boardDetailRepository;
    private final BoardStatisticService boardStatisticService;
    private final BoardRepository boardRepository;
    private final HtmlUtils htmlUtils;

    @Transactional
    public BoardImageDetailResponse getBoardDtos(Long memberId, Long boardId, String ipAddress) {
        List<BoardAndImageDto> boardAndImageDtos = boardRepository.findBoardAndBoardImageByBoardId(
                boardId);

        validateListNotEmpty(boardAndImageDtos, BOARD_NOT_FOUND);

        BoardDto boardDto = BoardDto.from(
                getFirstBoardInfo(boardAndImageDtos));

        boolean isWished = Objects.nonNull(memberId)
                && wishListBoardRepository.existsByBoardIdAndMemberId(boardId, memberId);

        List<String> boardImageUrls = extractImageUrl(boardAndImageDtos);

        if (Objects.isNull(boardDto.getProfile())) {
            throw new BbangleException(IMAGE_URL_NULL);
        }

        String boardProfileUrl = buildFullUrl(boardDto.getProfile());

        String boardDetailHtml = boardDetailRepository.findByBoardId(boardId);
        String boardDetailHtmlWithCdnUrl = htmlUtils.convertHtmlWithFullImageUrls(boardDetailHtml);

        String visitorInfo = ViewCount.builder()
                .boardId(boardId)
                .ipAddress(ipAddress)
                .build()
                .toString();

        viewCountComponent.visit(visitorInfo);
        boardStatisticService.updateViewCount(boardId);

        return BoardImageDetailResponse.from(
                boardDto,
                isWished,
                boardProfileUrl,
                boardImageUrls,
                boardDetailHtmlWithCdnUrl);
    }

    private List<String> extractImageUrl(List<BoardAndImageDto> boardAndImageDtos) {
        return boardAndImageDtos.stream()
                .filter(imageDto -> Objects.nonNull(imageDto.url()))
                .map(imageDto -> buildFullUrl(imageDto.url()))
                .toList();
    }

    private String buildFullUrl(String url) {
        if (Objects.isNull(url)) {
            throw new BbangleException(IMAGE_URL_NULL);
        }

        if (url.contains(HTTP)) {
            return url;
        }

        return cdn + url;
    }

    private BoardAndImageDto getFirstBoardInfo(List<BoardAndImageDto> boardAndImageTuples) {
        return boardAndImageTuples.stream()
                .findFirst()
                .orElseThrow(() -> new BbangleException(BOARD_NOT_FOUND));
    }

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

