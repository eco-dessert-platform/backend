package com.bbangle.bbangle.search.customer.facade;

import com.bbangle.bbangle.common.page.CursorPagination;
import com.bbangle.bbangle.search.customer.service.SearchService;
import com.bbangle.bbangle.search.customer.service.dto.SearchCommand.Main;
import com.bbangle.bbangle.search.customer.service.dto.SearchInfo;
import com.bbangle.bbangle.search.customer.service.dto.SearchInfo.Select;
import com.bbangle.bbangle.wishlist.service.WishListBoardService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchFacade {

    private final SearchService searchService;
    private final WishListBoardService wishListBoardService;

    @Transactional(readOnly = true)
    public CursorPagination<Select> getBoardList(Main command) {
        SearchInfo.BoardsInfo boardsInfo = searchService.getBoardList(command);
        Map<Long, Boolean> boardWishedMap = wishListBoardService.getBoardWishedMap(
            command.memberId(), boardsInfo.getBoards());
        return searchService.convertBoardsToCursorPagination(boardsInfo, boardWishedMap);
    }
}
