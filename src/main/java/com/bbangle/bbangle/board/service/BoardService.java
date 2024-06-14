package com.bbangle.bbangle.board.service;


import static com.bbangle.bbangle.board.validator.BoardValidator.*;
import static com.bbangle.bbangle.exception.BbangleErrorCode.BOARD_NOT_FOUND;

import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.dto.BoardAndImageDto;
import com.bbangle.bbangle.board.dto.ProductDto;
import com.bbangle.bbangle.board.dto.BoardDto;
import com.bbangle.bbangle.board.dto.BoardImageDetailResponse;
import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.dto.ProductResponse;
import com.bbangle.bbangle.board.repository.BoardDetailRepository;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.board.repository.util.BoardPageGenerator;
import com.bbangle.bbangle.board.sort.FolderBoardSortType;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.boardstatistic.service.BoardStatisticService;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.page.BoardCustomPage;
import com.bbangle.bbangle.store.dto.PopularBoardDto;
import com.bbangle.bbangle.store.dto.PopularBoardResponse;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.bbangle.bbangle.wishlist.repository.WishListBoardRepository;
import com.bbangle.bbangle.wishlist.repository.WishListFolderRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private static final Boolean DEFAULT_BOARD = false;
    private static final Boolean BOARD_IN_FOLDER = true;
    private static final int ONE_CATEGOTY = 1;

    private final BoardRepository boardRepository;
    private final BoardDetailRepository boardDetailRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final BoardStatisticService boardStatisticService;
    private final WishListFolderRepository folderRepository;
    private final WishListBoardRepository wishListBoardRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional(readOnly = true)
    public BoardCustomPage<List<BoardResponseDto>> getBoardList(
        FilterRequest filterRequest,
        SortType sort,
        Long cursorId,
        Long memberId
    ) {
        List<BoardResponseDao> boards = boardRepository
            .getBoardResponseList(filterRequest, sort, cursorId);
        BoardCustomPage<List<BoardResponseDto>> boardPage = BoardPageGenerator.getBoardPage(boards,
            DEFAULT_BOARD);

        if (Objects.nonNull(memberId) && memberRepository.existsById(memberId)) {
            updateLikeStatus(boardPage, memberId);
        }

        return boardPage;
    }

    @Transactional(readOnly = true)
    public BoardCustomPage<List<BoardResponseDto>> getPostInFolder(
        Long memberId,
        FolderBoardSortType sort,
        Long folderId,
        Long cursorId
    ) {
        WishListFolder folder = folderRepository.findByMemberIdAndId(memberId, folderId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.FOLDER_NOT_FOUND));

        List<BoardResponseDao> allByFolder = boardRepository.getAllByFolder(sort, cursorId, folder,
            memberId);

        return BoardPageGenerator.getBoardPage(allByFolder, BOARD_IN_FOLDER);
    }


    private List<String> extractImageUrl(List<BoardAndImageDto> boardAndImageDtos) {
        return boardAndImageDtos.stream()
            .map(BoardAndImageDto::url)
            .toList();
    }

    private BoardAndImageDto getFirstBoardInfo(List<BoardAndImageDto> boardAndImageTuples) {
        return boardAndImageTuples.stream()
            .findFirst()
            .orElseThrow(() -> new BbangleException(BOARD_NOT_FOUND));
    }

    public BoardImageDetailResponse getBoardDtos(Long memberId, Long boardId, String viewCountKey) {
        List<BoardAndImageDto> boardAndImageDtos = boardRepository.findBoardAndBoardImageByBoardId(
            boardId);

        validateListNotEmpty(boardAndImageDtos, BOARD_NOT_FOUND);

        BoardDto boardDto = BoardDto.from(
            getFirstBoardInfo(boardAndImageDtos));

        boolean isWished = Objects.nonNull(memberId)
            && wishListBoardRepository.existsByBoardIdAndMemberId(memberId, boardId);

        List<String> boardImageUrls = extractImageUrl(boardAndImageDtos);
        List<String> boardDetailUrls = boardDetailRepository.findByBoardId(boardId);

        boardStatisticService.updateViewCount(boardId);
        if(viewCountKey != null) {
            redisTemplate.opsForValue().set(viewCountKey, "true");
        }

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

        validateListNotEmpty(products, BOARD_NOT_FOUND);

        List<ProductDto> productDtos = convertToProductDtos(products);
        Boolean isBundled = isBundled(products);

        return ProductResponse.of(isBundled, productDtos);
    }

    public List<PopularBoardResponse> getTopBoardInfo(Long memberId, Long storeId) {
        List<Long> boardIds = boardRepository.getTopBoardIds(storeId);

        if (boardIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<PopularBoardDto> boardDtos = boardRepository.getTopBoardInfo(boardIds, memberId);
        Map<Long, Set<Category>> categorys = productRepository.getCategoryInfoByBoardId(boardIds);

        boardDtos = sortByPopularity(boardIds, boardDtos); // 인기순 정렬

        return combineBaseOnBoardId(boardDtos, categorys);
    }

    private List<PopularBoardDto> sortByPopularity(List<Long> orders,
        List<PopularBoardDto> popularBoardDtos) {
        return orders.stream().map(
                id -> popularBoardDtos.stream()
                    .filter(popularBoardDto -> popularBoardDto.getBoardId().equals(id))
                    .findFirst()
                    .get())
            .toList();
    }

    private List<PopularBoardResponse> combineBaseOnBoardId(
        List<PopularBoardDto> popularBoardDtos, Map<Long, Set<Category>> categorys) {
        return popularBoardDtos.stream()
            .map(popularBoardDto -> PopularBoardResponse.from(popularBoardDto, categorys))
            .toList();
    }

}
