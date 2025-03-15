package com.bbangle.bbangle.board.service;

import com.bbangle.bbangle.board.dto.BoardInfoDto;
import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.dto.BoardResponse;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.util.BoardPageGenerator;
import com.bbangle.bbangle.board.sort.FolderBoardSortType;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.common.page.CursorPageResponse;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.bbangle.bbangle.wishlist.repository.WishListFolderRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private static final Boolean DEFAULT_BOARD = false;
    private static final Boolean BOARD_IN_FOLDER = true;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final WishListFolderRepository folderRepository;

    @Cacheable(
            value = "recommendContents",
            key = "'defaultRecommendCache'",
            cacheManager = "contentCacheManager",
            condition = "#filterRequest.glutenFreeTag == null && #filterRequest.highProteinTag == null && #filterRequest.sugarFreeTag == null && #filterRequest.veganTag == null && #filterRequest.ketogenicTag == null && #filterRequest.category == null && #filterRequest.minPrice == null && #filterRequest.maxPrice == null && #filterRequest.orderAvailableToday == null && #sort == T(com.bbangle.bbangle.board.sort.SortType).RECOMMEND && #cursorId == null && #memberId == null"
    )
    public CursorPageResponse<BoardResponse> getBoards(FilterRequest filterRequest, SortType sort,
                                                       Long cursorId, Long memberId) {
        List<BoardResponseDao> boards = boardRepository.getBoardResponseList(memberId, filterRequest, sort, cursorId);
        return getResponseFromDao(boards, memberId, DEFAULT_BOARD);
    }

    public CursorPageResponse<BoardResponse> getResponseFromDao(List<BoardResponseDao> boardResponseDaos,
                                                                Long memberId,
                                                                Boolean isInFolder) {
        CursorPageResponse<BoardResponse> boardPage = BoardPageGenerator.getBoardPage(boardResponseDaos, isInFolder);

        if (Objects.nonNull(memberId) && memberRepository.existsById(memberId)) {
            updateLikeStatus(boardPage, memberId);
        }

        return boardPage;
    }

    private void updateLikeStatus(
            CursorPageResponse<BoardResponse> boardResponses,
            Long memberId
    ) {
        List<Long> responseList = extractIds(boardResponses);
        List<Long> likedContentIds = boardRepository.getLikedContentsIds(responseList, memberId);

        boardResponses.getData()
                .stream()
                .filter(board -> likedContentIds.contains(board.getBoardId()))
                .forEach(response -> response.updateLike(true));
    }

    private List<Long> extractIds(CursorPageResponse<BoardResponse> boardResponses) {
        return boardResponses.getData()
                .stream()
                .map(BoardResponse::getBoardId)
                .toList();
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<BoardResponse> getPostInFolder(
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

    @Transactional(readOnly = true)
    public List<BoardInfoDto> getTopBoardInfo(Long memberId, Long storeId) {
        return boardRepository.findBestBoards(memberId, storeId);
    }

    @Transactional(readOnly = true)
    public Long getFilteredBoardCount(FilterRequest filterRequest) {
        return boardRepository.getBoardCount(filterRequest);
    }

}

