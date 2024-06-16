package com.bbangle.bbangle.search.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.search.dto.request.SearchBoardRequest;
import com.bbangle.bbangle.search.dto.response.RecencySearchResponse;
import com.bbangle.bbangle.search.dto.response.SearchBoardResponse;
import com.bbangle.bbangle.search.dto.response.SearchStoreResponse;
import com.bbangle.bbangle.search.service.SearchService;
import com.bbangle.bbangle.util.SecurityUtils;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/search")
public class SearchController {
    private final String GET_BOARD_KEYWORD_SEARCH_API = "/boards";
    private final String GET_STORE_KEYWORD_SEARCH_API = "/stores";
    private final String GET_RECENCY_KEYWORD_SEARCH_API = "/recency";
    private final String DELETE_RECENCY_KEYWORD_SEARCH_API = "/recency";
    private final String GET_BEST_KEYWORD_SEARCH_API = "/best-keyword";
    private final String GET_AUTO_KEYWORD_SEARCH_API = "/auto-keyword";
    private final String SUCCESS_SAVEKEYWORD = "검색어 저장 완료";

    private final SearchService searchService;
    private final ResponseService responseService;

    @GetMapping(GET_BOARD_KEYWORD_SEARCH_API)
    public CommonResult getSearchBoardDtos(
            SearchBoardRequest searchBoardRequest
    ){
        Long memberId = SecurityUtils.getMemberIdWithAnonymous();

        SearchBoardResponse searchBoardDtos = searchService.getSearchBoardDtos(memberId,
            searchBoardRequest);
        return responseService.getSingleResult(searchBoardDtos);
    }

    @GetMapping(GET_STORE_KEYWORD_SEARCH_API)
    public CommonResult getSearchStoreDtos(
            @RequestParam("page")
            int page,
            @RequestParam("keyword")
            String keyword
    ){
        Long memberId = SecurityUtils.getMemberIdWithAnonymous();
        SearchStoreResponse searchStoreDtos = searchService.getSearchStoreDtos(memberId, page,
            keyword);
        return responseService.getSingleResult(searchStoreDtos);
    }

    @PostMapping
    public CommonResult saveKeyword(
        @RequestParam("keyword")
        String keyword
    ) {
        Long memberId = SecurityUtils.getMemberIdWithAnonymous();

        searchService.saveKeyword(memberId, keyword);
        return responseService.getSingleResult(
            Map.of("content", SUCCESS_SAVEKEYWORD));
    }

    @GetMapping(GET_RECENCY_KEYWORD_SEARCH_API)
    public CommonResult getRecencyKeyword() {
        Long memberId = SecurityUtils.getMemberIdWithAnonymous();
        RecencySearchResponse recencyKeyword = searchService.getRecencyKeyword(memberId);
        return responseService.getSingleResult(recencyKeyword);
    }

    @DeleteMapping(DELETE_RECENCY_KEYWORD_SEARCH_API)
    public CommonResult deleteRecencyKeyword(
        @RequestParam(value = "keyword")
        String keyword
    ) {
        Long memberId = SecurityUtils.getMemberId();

        return responseService.getSingleResult(
                Map.of("content", searchService.deleteRecencyKeyword(keyword, memberId))
            );
    }

    @GetMapping(GET_BEST_KEYWORD_SEARCH_API)
    public CommonResult getBestKeyword() {
        return responseService.getSingleResult(
                Map.of("content", searchService.getBestKeyword()));
    }

    @GetMapping(GET_AUTO_KEYWORD_SEARCH_API)
    public CommonResult getAutoKeyword(
        @RequestParam("keyword")
        String keyword
    ) {
        return responseService.getSingleResult(
                Map.of("content", searchService.getAutoKeyword(keyword))
            );
    }
}
