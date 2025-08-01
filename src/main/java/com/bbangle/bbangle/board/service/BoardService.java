package com.bbangle.bbangle.board.service;

import com.bbangle.bbangle.board.constant.FolderBoardSortType;
import com.bbangle.bbangle.board.dao.BoardThumbnailDao;
import com.bbangle.bbangle.board.dto.BoardResponse;
import com.bbangle.bbangle.board.dto.BoardResponses;
import com.bbangle.bbangle.board.repository.BoardRepository;
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
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;
    private final WishListFolderRepository folderRepository;
    private final BoardInFolderSortFactory boardInFolderSortFactory;

    public CursorPageResponse<BoardResponse> getResponseFromDao(List<BoardThumbnailDao> boardDaos) {
        BoardResponses boardResponses = BoardResponses.from(boardDaos);
        return CursorPageResponse.of(boardResponses.boardResponses(), BOARD_PAGE_SIZE, BoardResponse::getBoardId);
    }

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

}

