package com.bbangle.bbangle.board.customer.facade;

import com.bbangle.bbangle.common.page.CursorPagination;
import com.bbangle.bbangle.search.customer.service.SearchService;
import com.bbangle.bbangle.search.customer.service.dto.SearchCommand;
import com.bbangle.bbangle.search.customer.service.dto.SearchInfo;
import com.bbangle.bbangle.wishlist.service.WishListBoardService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardFacade {

    private final SearchService searchService;
    private final WishListBoardService wishListBoardService;

    @Transactional(readOnly = true)
    public CursorPagination<SearchInfo.Select> getBoardList(@Valid SearchCommand.Main command) {
        SearchInfo.BoardsInfo boardsInfo = searchService.getBoardList(command);
        Map<Long, Boolean> boardWishedMap = wishListBoardService.getBoardWishedMap(
            command.memberId(), boardsInfo.getBoards());
        return searchService.convertBoardsToCursorPagination(boardsInfo, boardWishedMap);
    }
}
