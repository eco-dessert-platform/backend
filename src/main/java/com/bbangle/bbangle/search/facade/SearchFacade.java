package com.bbangle.bbangle.search.facade;

import com.bbangle.bbangle.common.page.ProcessedDataCursor;
import com.bbangle.bbangle.search.service.SearchService;
import com.bbangle.bbangle.search.service.dto.SearchCommand;
import com.bbangle.bbangle.search.service.dto.SearchInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchFacade {

    private final SearchService searchService;

    public ProcessedDataCursor<SearchInfo.Select, SearchInfo.SearchBoardPage> getBoardList(SearchCommand.Main command) {
        return searchService.getBoardList(command);
    }
}
