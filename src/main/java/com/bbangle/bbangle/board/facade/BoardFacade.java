package com.bbangle.bbangle.board.facade;

import com.bbangle.bbangle.common.page.CursorPagination;
import com.bbangle.bbangle.search.service.SearchService;
import com.bbangle.bbangle.search.service.dto.SearchCommand;
import com.bbangle.bbangle.search.service.dto.SearchInfo;
import com.bbangle.bbangle.wishlist.customer.service.WishListBoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class BoardFacade {
    private final SearchService searchService;
    private final WishListBoardService wishListBoardService;

    @Transactional(readOnly = true)
    public CursorPagination<SearchInfo.Select> getBoardList(@Valid SearchCommand.Main command) {
        SearchInfo.BoardsInfo boardsInfo = searchService.getBoardList(command);
        Map<Long, Boolean> boardWishedMap = wishListBoardService.getBoardWishedMap(command.memberId(), boardsInfo.getBoards());
        return searchService.convertBoardsToCursorPagination(boardsInfo, boardWishedMap);
    }
}
