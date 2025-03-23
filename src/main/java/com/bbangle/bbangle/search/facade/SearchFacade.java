package com.bbangle.bbangle.search.facade;

import com.bbangle.bbangle.common.page.CursorPagination;
import com.bbangle.bbangle.search.service.SearchService;
import com.bbangle.bbangle.search.service.dto.SearchCommand.Main;
import com.bbangle.bbangle.search.service.dto.SearchInfo.Select;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchFacade {

    private final SearchService searchService;

    public CursorPagination<Select> getBoardList(Main command) {
        return searchService.getBoardList(command);
    }
}
