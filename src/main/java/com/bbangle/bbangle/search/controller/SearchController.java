package com.bbangle.bbangle.search.controller;

import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.CursorPagination;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.search.controller.mapper.SearchMapper;
import com.bbangle.bbangle.search.dto.response.RecencySearchResponse;
import com.bbangle.bbangle.search.facade.SearchFacade;
import com.bbangle.bbangle.search.service.SearchService;
import com.bbangle.bbangle.search.service.dto.SearchCommand;
import com.bbangle.bbangle.search.service.dto.SearchInfo.Select;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/search")
public class SearchController {

        private final SearchService searchService;
        private final ResponseService responseService;
        private final SearchMapper searchMapper;
        private final SearchFacade searchFacade;

        @GetMapping("/boards")
        public SingleResult<CursorPagination<Select>> getList(
            @ParameterObject
            FilterRequest filterRequest,
            @RequestParam(required = false, defaultValue = "RECOMMEND", value = "sort")
            SortType sort,
            @RequestParam(required = false, value = "keyword")
            String keyword,
            @RequestParam(required = false, value = "cursorId")
            Long cursorId,
            @AuthenticationPrincipal
            Long memberId
        ) {
                SearchCommand.Main command = searchMapper.toSearchMain(filterRequest, sort, keyword, cursorId, memberId);
                CursorPagination<Select> searchBoardPage = searchFacade.getBoardList(command);
                return responseService.getSingleResult(searchBoardPage);
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
