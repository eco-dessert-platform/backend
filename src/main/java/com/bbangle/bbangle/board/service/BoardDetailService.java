package com.bbangle.bbangle.board.service;

import com.bbangle.bbangle.board.common.TagUtils;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.ViewCount;
import com.bbangle.bbangle.board.dto.*;
import com.bbangle.bbangle.board.repository.BoardDetailRepository;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.service.component.ViewCountComponent;
import com.bbangle.bbangle.board.service.dto.BoardDetailCommand;
import com.bbangle.bbangle.board.service.dto.BoardDetailInfo;
import com.bbangle.bbangle.board.service.mapper.BoardDetailInfoMapper;
import com.bbangle.bbangle.boardstatistic.repository.BoardStatisticRepository;
import com.bbangle.bbangle.boardstatistic.service.BoardStatisticService;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.push.repository.PushRepository;
import com.bbangle.bbangle.util.HtmlUtils;
import com.bbangle.bbangle.wishlist.repository.WishListBoardRepository;
import com.bbangle.bbangle.wishlist.repository.WishListStoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardDetailService {

    private static final int RECOMMENDATION_ITEM_COUNT = 3;
    private static final int ZERO_INSUFFICIENT = 0;
    private static final Boolean NOT_WISHED_BOARD = false;
    private static final Boolean NOT_WISHED_STORE = false;
    private static final List<Long> NOT_BBANGKETTING_PRODUCT_IDS = List.of();


    private final WishListBoardRepository wishListBoardRepository;
    private final WishListStoreRepository wishListStoreRepository;
    private final PushRepository pushRepository;
    private final ViewCountComponent viewCountComponent;
    private final BoardStatisticRepository boardStatisticRepository;
    private final BoardDetailRepository boardDetailRepository;
    private final BoardStatisticService boardStatisticService;
    private final BoardRepository boardRepository;
    private final HtmlUtils htmlUtils;
    private final BoardDetailInfoMapper boardDetailInfoMapper;

    @Transactional
    public BoardImageDetailResponse getBoardDtos(Long memberId, Long boardId,
                                                 String ipAddress) {
        List<BoardAndImageDto> boardAndImageDtos = boardRepository.findBoardAndBoardImageByBoardId(
                boardId);
        BoardAndImageResponses boardAndImageResponses = BoardAndImageResponses.createFromDtos(
                boardAndImageDtos);

        boolean isWished = Objects.nonNull(memberId)
                && wishListBoardRepository.existsByBoardIdAndMemberId(boardId, memberId);

        String boardDetailHtml = boardDetailRepository.findByBoardId(boardId);
        String boardDetailHtmlWithCdnUrl = htmlUtils.convertHtmlWithFullImageUrls(
                boardDetailHtml);

        String visitorInfo = ViewCount.builder()
                .boardId(boardId)
                .ipAddress(ipAddress)
                .build()
                .toString();

        viewCountComponent.visit(visitorInfo);
        boardStatisticService.updateViewCount(boardId);

        return BoardImageDetailResponse.of(
                boardAndImageResponses,
                isWished,
                boardDetailHtmlWithCdnUrl);
    }

    public List<SimilarityBoardResponse> getSimilarityBoardResponses(Long memberId,
                                                                     Long boardId) {
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
                Comparator.comparingInt(
                        dto -> similarityOrderByBoardIds.indexOf(dto.getBoardId())));

        return boardResponses;
    }

    private void addRandomRecommandationBoard(List<Long> similarityOrderByBoardIds) {
        int insufficientNumber =
                RECOMMENDATION_ITEM_COUNT - similarityOrderByBoardIds.size();
        if (insufficientNumber > ZERO_INSUFFICIENT) {
            List<Long> popularBoardIds = boardStatisticRepository.findPopularBoardIds(
                            30).stream()
                    .filter(id -> !similarityOrderByBoardIds.contains(id))
                    .collect(Collectors.toCollection(ArrayList::new));

            Collections.shuffle(popularBoardIds);

            similarityOrderByBoardIds.addAll(
                    popularBoardIds.stream().limit(3).toList());
        }
    }

    @Transactional
    public BoardImageDetailResponse getBoardDetails(Long memberId, Long boardId,
                                                    String ipAddress) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BbangleException(BbangleErrorCode.BOARD_NOT_FOUND));

        List<BoardAndImageDto> boardAndImageDtos = boardRepository.findBoardAndBoardImageByBoardId(
                boardId);
        BoardAndImageResponses boardAndImageResponses = BoardAndImageResponses.createFromDtos(
                boardAndImageDtos);

        boolean isWished = Objects.nonNull(memberId)
                && wishListBoardRepository.existsByBoardIdAndMemberId(boardId, memberId);

        String boardDetailHtml = boardDetailRepository.findByBoardId(boardId);
        String boardDetailHtmlWithCdnUrl = htmlUtils.convertHtmlWithFullImageUrls(
                boardDetailHtml);

        String visitorInfo = ViewCount.builder()
                .boardId(boardId)
                .ipAddress(ipAddress)
                .build()
                .toString();

        viewCountComponent.visit(visitorInfo);
        boardStatisticService.updateViewCount(boardId);

        return BoardImageDetailResponse.of(
                boardAndImageResponses,
                isWished,
                boardDetailHtmlWithCdnUrl);
    }

    public BoardDetailInfo.Main getBoardDetail(BoardDetailCommand.Main command) {
        Board board = boardRepository.findById(command.boardId())
                .orElseThrow(() -> new BbangleException(BbangleErrorCode.BOARD_NOT_FOUND));

        if (Objects.isNull(command.memberId())) {
            return boardDetailInfoMapper.toMainInfo(board, NOT_WISHED_STORE, NOT_WISHED_BOARD, NOT_BBANGKETTING_PRODUCT_IDS);
        }

        boolean isWishedBoard = wishListBoardRepository.existsByBoardIdAndMemberId(
                command.boardId(), command.memberId());
        boolean isWishedStore = wishListStoreRepository.existsByStoreIdAndMemberId(
                board.getStore().getId(), command.memberId());

        List<Long> bbangkettingProductIds = getBbankettingProductsIds(board, command.memberId());

        return boardDetailInfoMapper.toMainInfo(board, isWishedStore, isWishedBoard, bbangkettingProductIds);
    }

    private List<Long> getBbankettingProductsIds(Board board, Long memberId) {
        List<Long> productIds = board.getProducts().stream().map(Product::getId).toList();
        return pushRepository.findExistingPushProductIds(productIds, memberId);
    }

    // 패키지 구조 수정 시, 로직 변경해야함
    @Transactional
    public void increaseVisitor(BoardDetailCommand.Main command) {
        String visitorInfo = ViewCount.builder()
                .boardId(command.boardId())
                .ipAddress(command.ipAddress())
                .build()
                .toString();

        viewCountComponent.visit(visitorInfo);
        boardStatisticService.updateViewCount(command.boardId());
    }
}

