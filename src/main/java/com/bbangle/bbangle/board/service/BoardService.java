package com.bbangle.bbangle.board.service;


import static com.bbangle.bbangle.board.validator.BoardValidator.*;
import static com.bbangle.bbangle.exception.BbangleErrorCode.BOARD_NOT_FOUND;

import com.bbangle.bbangle.board.dto.BoardAndImageDto;
import com.bbangle.bbangle.board.dto.BoardInfoDto;
import com.bbangle.bbangle.board.dto.BoardDto;
import com.bbangle.bbangle.board.dto.BoardImageDetailResponse;
import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.repository.BoardDetailRepository;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.util.BoardPageGenerator;
import com.bbangle.bbangle.board.sort.FolderBoardSortType;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.boardstatistic.service.BoardStatisticService;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.page.BoardCustomPage;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.bbangle.bbangle.wishlist.repository.WishListBoardRepository;
import com.bbangle.bbangle.wishlist.repository.WishListFolderRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private static final Boolean DEFAULT_BOARD = false;
    private static final Boolean BOARD_IN_FOLDER = true;
    private static final String HTTP = "http";
    private final BoardRepository boardRepository;
    private final BoardDetailRepository boardDetailRepository;
    private final MemberRepository memberRepository;
    private final BoardStatisticService boardStatisticService;
    private final WishListFolderRepository folderRepository;
    private final WishListBoardRepository wishListBoardRepository;
    @Qualifier("defaultRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;
    @Value("${cdn.domain}")
    private String cdn;



    @Transactional(readOnly = true)
    public BoardCustomPage<List<BoardResponseDto>> getBoardList(
        FilterRequest filterRequest,
        SortType sort,
        Long cursorId,
        Long memberId
    ) {
        List<BoardResponseDao> boards = boardRepository
            .getBoardResponseList(memberId, filterRequest, sort, cursorId);
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

    @Transactional
    public BoardImageDetailResponse getBoardDtos(Long memberId, Long boardId, String viewCountKey) {
        List<BoardAndImageDto> boardAndImageDtos = boardRepository.findBoardAndBoardImageByBoardId(
            boardId);

        validateListNotEmpty(boardAndImageDtos, BOARD_NOT_FOUND);

        BoardDto boardDto = BoardDto.from(
            getFirstBoardInfo(boardAndImageDtos));

        boolean isWished = Objects.nonNull(memberId)
            && wishListBoardRepository.existsByBoardIdAndMemberId(boardId, memberId);

        List<String> boardImageUrls = extractImageUrl(boardAndImageDtos);

        String boardProfileUrl = buildFullUrl(boardDto.getProfile());

        List<String> boardDetailUrls = boardDetailRepository.findByBoardId(boardId)
            .stream()
            .map(this::buildFullUrl)
            .toList();

        boardStatisticService.updateViewCount(boardId);
        if (viewCountKey != null) {
            redisTemplate.opsForValue().set(viewCountKey, "true");
        }

        return BoardImageDetailResponse.from(
            boardDto,
            isWished,
            boardProfileUrl,
            boardImageUrls,
            boardDetailUrls);
    }

    private String buildFullUrl(String url) {
        if (url.contains(HTTP)) {
            return url;
        }

        return cdn + url;
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

    @Transactional(readOnly = true)
    public List<BoardInfoDto> getTopBoardInfo(Long memberId, Long storeId) {
        return boardRepository.findBestBoards(memberId, storeId);
    }

    @Transactional(readOnly = true)
    public Long getFilteredBoardCount(FilterRequest filterRequest) {
        return boardRepository.getBoardCount(filterRequest);
    }

}

