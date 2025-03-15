package com.bbangle.bbangle.search.controller;

import static com.bbangle.bbangle.board.sort.SortType.RECOMMEND;

import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.common.page.SearchCustomPage;
import com.bbangle.bbangle.search.dto.response.RecencySearchResponse;
import com.bbangle.bbangle.search.dto.response.SearchResponse;
import com.bbangle.bbangle.search.service.SearchService;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    private final SearchService searchService;
    private final ResponseService responseService;

    @GetMapping("/boards")
    public CommonResult getList(
        @ParameterObject
        FilterRequest filterRequest,
        @RequestParam(required = false, value = "sort")
        SortType sort,
        @RequestParam(required = false, value = "keyword")
        String keyword,
        @RequestParam(required = false, value = "cursorId")
        Long cursorId,
        @AuthenticationPrincipal
        Long memberId
    ) {
        sort = settingDefaultSortTypeIfNull(sort);
        SearchCustomPage<SearchResponse> searchCustomPage = searchService.getBoardList(
            filterRequest,
            sort,
            keyword,
            cursorId,
            memberId);
        return responseService.getSingleResult(searchCustomPage);
    }

    private SortType settingDefaultSortTypeIfNull(SortType sort) {
        return Objects.nonNull(sort) ? sort : RECOMMEND;
    }

    @PostMapping
    public CommonResult saveKeyword(
        @RequestParam("keyword")
        String keyword,
        @AuthenticationPrincipal
        Long memberId
    ) {
        searchService.saveKeyword(memberId, keyword);
        return responseService.getSuccessResult();
    }

    @GetMapping("/recency")
    public CommonResult getRecencyKeyword(
        @AuthenticationPrincipal
        Long memberId
    ) {
        RecencySearchResponse recencyKeyword = searchService.getRecencyKeyword(memberId);
        return responseService.getSingleResult(recencyKeyword);
    }

    @DeleteMapping("/recency")
    public CommonResult deleteRecencyKeyword(
        @RequestParam(value = "keyword")
        String keyword,
        @AuthenticationPrincipal
        Long memberId
    ) {
        searchService.deleteRecencyKeyword(keyword, memberId);

        return responseService.getSuccessResult();
    }

    @GetMapping("/best-keyword")
    public CommonResult getBestKeyword() {
        List<String> bestKeywords = searchService.getBestKeyword();
        return responseService.getListResult(bestKeywords);
    }

    @GetMapping("/auto-keyword")
    public CommonResult getAutoKeyword(
        @RequestParam("keyword")
        String keyword
    ) {
        List<String> autoKeywords = searchService.getAutoKeyword(keyword);
        return responseService.getListResult(autoKeywords);
    }
}
