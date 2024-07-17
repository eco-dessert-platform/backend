package com.bbangle.bbangle.search.controller;

import static com.bbangle.bbangle.board.sort.SortType.RECOMMEND;

import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.page.SearchCustomPage;
import com.bbangle.bbangle.search.dto.response.RecencySearchResponse;
import com.bbangle.bbangle.search.dto.response.SearchResponse;
import com.bbangle.bbangle.search.service.SearchService;
import com.bbangle.bbangle.util.SecurityUtils;
import java.util.Map;
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
    private static final String SUCCESS_SAVEKEYWORD = "검색어 저장 완료";

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
        String keyword
    ) {
        Long memberId = SecurityUtils.getMemberIdWithAnonymous();

        searchService.saveKeyword(memberId, keyword);
        return responseService.getSingleResult(
            Map.of("content", SUCCESS_SAVEKEYWORD));
    }

    @GetMapping("/recency")
    public CommonResult getRecencyKeyword() {
        Long memberId = SecurityUtils.getMemberIdWithAnonymous();
        RecencySearchResponse recencyKeyword = searchService.getRecencyKeyword(memberId);
        return responseService.getSingleResult(recencyKeyword);
    }

    @DeleteMapping("/recency")
    public CommonResult deleteRecencyKeyword(
        @RequestParam(value = "keyword")
        String keyword
    ) {
        Long memberId = SecurityUtils.getMemberId();

        return responseService.getSingleResult(
                Map.of("content", searchService.deleteRecencyKeyword(keyword, memberId))
            );
    }

    @GetMapping("/best-keyword")
    public CommonResult getBestKeyword() {
        return responseService.getSingleResult(
                Map.of("content", searchService.getBestKeyword()));
    }

    @GetMapping("/auto-keyword")
    public CommonResult getAutoKeyword(
        @RequestParam("keyword")
        String keyword
    ) {
        return responseService.getSingleResult(
                Map.of("content", searchService.getAutoKeyword(keyword))
            );
    }
}
