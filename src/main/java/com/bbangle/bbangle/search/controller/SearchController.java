package com.bbangle.bbangle.search.controller;

import com.bbangle.bbangle.common.message.MessageResDto;
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
  
    @GetMapping(GET_BOARD_KEYWORD_SEARCH_API)
    public ResponseEntity<SearchBoardResponse> getSearchBoardDtos(
            SearchBoardRequest searchBoardRequest
    ){
        // 회원, 비회원 둘 다 사용 가능
        Long memberId = SecurityUtils.getMemberIdWithAnonymous();

        return ResponseEntity.ok().body(searchService.getSearchBoardDtos(memberId, searchBoardRequest));
    }

    @GetMapping(GET_STORE_KEYWORD_SEARCH_API)
    public ResponseEntity<SearchStoreResponse> getSearchStoreDtos(
            @RequestParam("page")
            int page,
            @RequestParam(value = "keyword")
            String keyword
    ){
        Long memberId = SecurityUtils.getMemberIdWithAnonymous();

        return ResponseEntity.ok().body(
                searchService.getSearchStoreDtos(memberId, page, keyword)
        );
    }

    @PostMapping
    public ResponseEntity<Map<String, MessageResDto>> saveKeyword(
        @RequestParam("keyword")
        String keyword
    ) {
        Long memberId = SecurityUtils.getMemberIdWithAnonymous();

        searchService.saveKeyword(memberId, keyword);
        return ResponseEntity.ok()
            .body(Map.of("content", new MessageResDto(SUCCESS_SAVEKEYWORD)));
    }

    @GetMapping(GET_RECENCY_KEYWORD_SEARCH_API)
    public ResponseEntity<RecencySearchResponse> getRecencyKeyword() {
        Long memberId = SecurityUtils.getMemberIdWithAnonymous();

        return ResponseEntity.ok().body(searchService.getRecencyKeyword(memberId));
    }

    @DeleteMapping(DELETE_RECENCY_KEYWORD_SEARCH_API)
    public ResponseEntity<Map<String, Boolean>> deleteRecencyKeyword(
        @RequestParam(value = "keyword")
        String keyword
    ) {
        Long memberId = SecurityUtils.getMemberId();

        return ResponseEntity.ok()
            .body(
                Map.of("content", searchService.deleteRecencyKeyword(keyword, memberId))
            );
    }

    @GetMapping(GET_BEST_KEYWORD_SEARCH_API)
    public ResponseEntity<Map<String, List<String>>> getBestKeyword() {
        return ResponseEntity.ok()
            .body(
                Map.of("content", searchService.getBestKeyword())
            );
    }

    @GetMapping(GET_AUTO_KEYWORD_SEARCH_API)
    public ResponseEntity<Map<String, List<String>>> getAutoKeyword(
        @RequestParam("keyword")
        String keyword
    ) {
        return ResponseEntity.ok()
            .body(
                Map.of("content", searchService.getAutoKeyword(keyword))
            );
    }
}
