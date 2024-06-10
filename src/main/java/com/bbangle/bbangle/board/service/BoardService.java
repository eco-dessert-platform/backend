package com.bbangle.bbangle.board.service;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.dto.BoardDetailResponse;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.util.BoardPageGenerator;
import com.bbangle.bbangle.board.sort.FolderBoardSortType;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.boardstatistic.service.BoardStatisticService;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.page.BoardCustomPage;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.boardstatistic.repository.BoardStatisticRepository;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.bbangle.bbangle.wishlist.repository.WishListFolderRepository;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private static final Boolean DEFAULT_BOARD = false;
    private static final Boolean BOARD_IN_FOLDER = true;
    private static final Double VIEW_COUNT_SCORE = 1.0;

    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final WishListFolderRepository folderRepository;
    @Qualifier("defaultRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;
    private final BoardStatisticService boardStatisticService;
    private final BoardStatisticRepository boardStatisticRepository;

    @Transactional(readOnly = true)
    public BoardCustomPage<List<BoardResponseDto>> getBoardList(
        FilterRequest filterRequest,
        SortType sort,
        Long cursorId,
        Long memberId
    ) {
        List<BoardResponseDao> boards = boardRepository
            .getBoardResponseList(filterRequest, sort, cursorId);
        BoardCustomPage<List<BoardResponseDto>> boardPage = BoardPageGenerator.getBoardPage(boards, DEFAULT_BOARD);

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

        List<BoardResponseDao> allByFolder = boardRepository.getAllByFolder(sort, cursorId, folder, memberId);

        return BoardPageGenerator.getBoardPage(allByFolder, BOARD_IN_FOLDER);
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
    public BoardDetailResponse getBoardDetailResponse(Long memberId, Long boardId) {
        return boardRepository.getBoardDetailResponse(memberId, boardId);
    }

    @Transactional
    public void updateCountView(Long boardId, String viewCountKey) {
        BoardStatistic boardStatistic = boardStatisticRepository.findByBoardId(boardId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.RANKING_NOT_FOUND));
        boardStatistic.updateBasicScore(0.1);

        boardStatisticService.updateRankingScore(boardId, VIEW_COUNT_SCORE);

        redisTemplate.opsForValue()
            .set(viewCountKey, "true", Duration.ofMinutes(3));
    }

    @Transactional
    public void adaptPurchaseReaction(Long boardId, String purchaseCountKey) {
        BoardStatistic boardStatistic = boardStatisticRepository.findByBoardId(boardId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.RANKING_NOT_FOUND));
        boardStatistic.updateBasicScore(1.0);

        redisTemplate.opsForValue()
            .set(purchaseCountKey, "true", Duration.ofMinutes(3));
    }


}
