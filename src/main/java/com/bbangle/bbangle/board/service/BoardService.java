package com.bbangle.bbangle.board.service;

import static com.bbangle.bbangle.board.repository.BoardRepositoryImpl.BOARD_PAGE_SIZE;

import com.bbangle.bbangle.board.dao.BoardThumbnailDao;
import com.bbangle.bbangle.board.dto.BoardInfoDto;
import com.bbangle.bbangle.board.dto.BoardResponse;
import com.bbangle.bbangle.board.dto.BoardResponses;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.util.BoardFilterCreator;
import com.bbangle.bbangle.board.repository.sort.strategy.BoardSortRepository;
import com.bbangle.bbangle.board.repository.sort.BoardSortRepositoryFactory;
import com.bbangle.bbangle.wishlist.repository.sort.strategy.BoardInFolderSortRepository;
import com.bbangle.bbangle.wishlist.repository.sort.BoardInFolderSortFactory;
import com.bbangle.bbangle.board.sort.FolderBoardSortType;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.common.page.CursorPageResponse;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.bbangle.bbangle.wishlist.repository.WishListFolderRepository;
import com.querydsl.core.BooleanBuilder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final WishListFolderRepository folderRepository;

    private final BoardSortRepositoryFactory boardSortRepositoryFactory;
    private final BoardInFolderSortFactory boardInFolderSortFactory;

    @Cacheable(
            value = "recommendContents",
            key = "'defaultRecommendCache'",
            cacheManager = "contentCacheManager",
            condition = "#filterRequest.glutenFreeTag == null && #filterRequest.highProteinTag == null && #filterRequest.sugarFreeTag == null && #filterRequest.veganTag == null && #filterRequest.ketogenicTag == null && #filterRequest.category == null && #filterRequest.minPrice == null && #filterRequest.maxPrice == null && #filterRequest.orderAvailableToday == null && #sort == T(com.bbangle.bbangle.board.sort.SortType).RECOMMEND && #cursorId == null && #memberId == null"
    )
    public CursorPageResponse<BoardResponse> getBoards(FilterRequest filterRequest, SortType sortType,
                                                       Long cursorId, Long memberId) {
        BooleanBuilder filter = new BoardFilterCreator(filterRequest).create();

        BoardSortRepository strategy = boardSortRepositoryFactory.getStrategy(sortType);
        List<Long> boardIds = strategy.findBoardIds(filter, cursorId);
        List<BoardThumbnailDao> daos = boardRepository.getThumbnailBoardsByIds(
                boardIds,
                strategy.getSortOrders(),
                memberId);
        return getResponseFromDao(daos);
    }

    public CursorPageResponse<BoardResponse> getResponseFromDao(List<BoardThumbnailDao> boardDaos) {
        BoardResponses boardResponses = BoardResponses.of(boardDaos);
        return CursorPageResponse.of(boardResponses.boardResponses(), BOARD_PAGE_SIZE, BoardResponse::getBoardId);
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

        BoardInFolderSortRepository strategy = boardInFolderSortFactory.getStrategy(sort);
        List<Long> boardIds = strategy.findBoardIds(cursorId, folder.getId());
        List<BoardThumbnailDao> daos = boardRepository.getThumbnailBoardsByIds(boardIds, strategy.getSortOrders(), folder.getId());

        BoardResponses responses = BoardResponses.of(daos);
        return CursorPageResponse.of(responses.boardResponses(), BOARD_PAGE_SIZE, BoardResponse::getBoardId);
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

