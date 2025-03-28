package com.bbangle.bbangle.board.service;

import com.bbangle.bbangle.board.dao.BoardThumbnailDao;
import com.bbangle.bbangle.board.dto.BoardInfoDto;
import com.bbangle.bbangle.board.dto.BoardResponse;
import com.bbangle.bbangle.board.dto.BoardResponses;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.sort.FolderBoardSortType;
import com.bbangle.bbangle.common.page.CursorPageResponse;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.bbangle.bbangle.wishlist.repository.WishListFolderRepository;
import com.bbangle.bbangle.wishlist.repository.sort.BoardInFolderSortFactory;
import com.bbangle.bbangle.wishlist.repository.sort.strategy.BoardInFolderSortRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.bbangle.bbangle.board.repository.BoardRepositoryImpl.BOARD_PAGE_SIZE;

@Service
@RequiredArgsConstructor
public class BoardService {
    private static final Boolean BOARD_IN_FOLDER = true;
    private final BoardRepository boardRepository;
    private final WishListFolderRepository folderRepository;
    private final BoardInFolderSortFactory boardInFolderSortFactory;

    public CursorPageResponse<BoardResponse> getResponseFromDao(List<BoardThumbnailDao> boardDaos,
                                                                Boolean isInFolder) {
        BoardResponses boardResponses = BoardResponses.of(boardDaos, isInFolder);
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
        List<BoardThumbnailDao> daos = boardRepository.getThumbnailBoardsByIds(boardIds, strategy.getSortOrders(),
                folder.getId());

        BoardResponses responses = BoardResponses.from(daos);
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

