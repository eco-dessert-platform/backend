package com.bbangle.bbangle.search.facade;

import com.bbangle.bbangle.common.page.CursorPagination;
import com.bbangle.bbangle.search.service.SearchService;
import com.bbangle.bbangle.search.service.dto.SearchCommand.Main;
import com.bbangle.bbangle.search.service.dto.SearchInfo;
import com.bbangle.bbangle.search.service.dto.SearchInfo.Select;
import com.bbangle.bbangle.wishlist.customer.service.WishListBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SearchFacade {

    private final SearchService searchService;
    private final WishListBoardService wishListBoardService;

    @Transactional(readOnly = true)
    public CursorPagination<Select> getBoardList(Main command) {
        SearchInfo.BoardsInfo boardsInfo = searchService.getBoardList(command);
        Map<Long, Boolean> boardWishedMap = wishListBoardService.getBoardWishedMap(command.memberId(), boardsInfo.getBoards());
        return searchService.convertBoardsToCursorPagination(boardsInfo, boardWishedMap);
    }
}
