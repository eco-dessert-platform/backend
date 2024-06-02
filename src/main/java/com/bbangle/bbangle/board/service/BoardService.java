package com.bbangle.bbangle.board.service;


import static com.bbangle.bbangle.board.validator.BoardValidator.*;
import static com.bbangle.bbangle.exception.BbangleErrorCode.BOARD_NOT_FOUND;

import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.dto.BoardAndImageDto;
import com.bbangle.bbangle.board.dto.ProductDto;
import com.bbangle.bbangle.board.dto.BoardDto;
import com.bbangle.bbangle.board.dto.BoardImageDetailResponse;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.board.dto.CursorInfo;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.dto.ProductResponse;
import com.bbangle.bbangle.board.repository.BoardDetailRepository;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.common.sort.SortType;
import com.bbangle.bbangle.config.ranking.BoardLikeInfo;
import com.bbangle.bbangle.config.ranking.ScoreType;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.page.BoardCustomPage;
import com.bbangle.bbangle.ranking.domain.Ranking;
import com.bbangle.bbangle.ranking.repository.RankingRepository;
import com.bbangle.bbangle.review.repository.ReviewRepository;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.bbangle.bbangle.wishlist.repository.WishListBoardRepository;
import com.bbangle.bbangle.wishlist.repository.WishListFolderRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH");

    private final BoardRepository boardRepository;
    private final BoardDetailRepository boardDetailRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final WishListFolderRepository folderRepository;
    private final WishListBoardRepository wishListBoardRepository;
    @Qualifier("defaultRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;
    @Qualifier("boardLikeInfoRedisTemplate")
    private final RedisTemplate<String, Object> boardLikeInfoRedisTemplate;
    private final RankingRepository rankingRepository;
    private static final int ONE_CATEGOTY = 1;

    @Transactional(readOnly = true)
    public BoardCustomPage<List<BoardResponseDto>> getBoardList(
        FilterRequest filterRequest,
        SortType sort,
        CursorInfo cursorInfo,
        Long memberId
    ) {
        BoardCustomPage<List<BoardResponseDto>> boards = boardRepository
            .getBoardResponseList(filterRequest, sort, cursorInfo);

        if (Objects.nonNull(memberId) && memberRepository.existsById(memberId)) {
            updateLikeStatus(boards, memberId);
        }

        return boards;
    }

    private List<String> extractImageUrl(List<BoardAndImageDto> boardAndImageTuples) {
        return boardAndImageTuples.stream()
            .map(BoardAndImageDto::purchaseUrl)
            .toList();
    }

    private BoardAndImageDto getFirstBoardInfo(List<BoardAndImageDto> boardAndImageTuples) {
        return boardAndImageTuples.stream()
            .findFirst()
            .orElseThrow(() -> new BbangleException(BOARD_NOT_FOUND));
    }

    public BoardImageDetailResponse getBoardDtos(Long memberId, Long boardId) {
        List<BoardAndImageDto> boardAndImageDtos = boardRepository.findBoardAndBoardImageByBoardId(
            boardId);
        validateListElementExist(boardAndImageDtos, BOARD_NOT_FOUND);

        BoardDto boardDto = BoardDto.from(
            getFirstBoardInfo(boardAndImageDtos));

        boolean isWished = Objects.nonNull(memberId)
            && wishListBoardRepository.existsByBoardIdAndMemberId(memberId, boardId);

        List<String> boardImageUrls = extractImageUrl(boardAndImageDtos);
        List<String> boardDetailUrls = boardDetailRepository.findByBoardId(boardId);

        return BoardImageDetailResponse.from(
            boardDto,
            isWished,
            boardImageUrls,
            boardDetailUrls);
    }

    private void updateLikeStatus(
        BoardCustomPage<List<BoardResponseDto>> boardResponseDto,
        Long memberId
    ) {
        List<Long> responseList = extractIds(boardResponseDto);
        List<Long> likedContentIds = boardRepository.getLikedContentsIds(responseList, memberId);

        boardResponseDto.getContent()
            .stream()
            .filter(board -> likedContentIds.contains(board.getBoardId()))
            .forEach(board -> board.updateLike(true));
    }

    private List<Long> extractIds(
        BoardCustomPage<List<BoardResponseDto>> boardResponseDto
    ) {
        return boardResponseDto.getContent()
            .stream()
            .map(BoardResponseDto::getBoardId)
            .toList();
    }

    private List<ProductDto> convertToProductDtos(List<Product> products) {
        return products.stream()
            .map(ProductDto::from)
            .toList();
    }

    private Boolean isBundled(List<Product> products) {
        return products.stream()
            .map(Product::getCategory)
            .distinct()
            .count() > ONE_CATEGOTY;
    }

    public ProductResponse getProductResponse(Long boardId) {
        List<Product> products = productRepository.findByBoardId(boardId);

        validateListElementExist(products, BOARD_NOT_FOUND);

        List<ProductDto> productDtos = convertToProductDtos(products);
        Boolean isBundled = isBundled(products);

        return ProductResponse.of(isBundled, productDtos);
    }

    public Slice<BoardResponseDto> getPostInFolder(
        Long memberId,
        String sort,
        Long folderId,
        Pageable pageable
    ) {
        Member member = memberRepository.findMemberById(memberId);

        WishListFolder folder = folderRepository.findByMemberAndId(member, folderId)
            .orElseThrow(() -> new BbangleException("존재하지 않는 폴더입니다."));

        return boardRepository.getAllByFolder(sort, pageable, folderId, folder);
    }

    @Transactional
    public void updateCountView(Long boardId, String viewCountKey) {
        Ranking ranking = rankingRepository.findByBoardId(boardId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.RANKING_NOT_FOUND));
        ranking.updatePopularScore(0.1);

        boardLikeInfoRedisTemplate.opsForList()
            .rightPush(
                LocalDateTime.now().format(formatter),
                new BoardLikeInfo(boardId, 0.1, LocalDateTime.now(), ScoreType.VIEW));

        redisTemplate.opsForValue()
            .set(viewCountKey, "true", Duration.ofMinutes(3));
    }

    @Transactional
    public void adaptPurchaseReaction(Long boardId, String purchaseCountKey) {
        Ranking ranking = rankingRepository.findByBoardId(boardId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.RANKING_NOT_FOUND));
        ranking.updatePopularScore(1.0);

        boardLikeInfoRedisTemplate.opsForList()
            .rightPush(LocalDateTime.now()
                    .format(formatter),
                new BoardLikeInfo(boardId, 1, LocalDateTime.now(), ScoreType.PURCHASE));

        redisTemplate.opsForValue()
            .set(purchaseCountKey, "true", Duration.ofMinutes(3));
    }
}
