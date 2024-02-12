package com.bbangle.bbangle.controller;

import com.bbangle.bbangle.dto.MessageResDto;
import com.bbangle.bbangle.dto.RecencySearchResponse;
import com.bbangle.bbangle.dto.SearchResponseDto;
import com.bbangle.bbangle.service.SearchService;
import com.bbangle.bbangle.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/search")
public class SearchController {
    private final String GET_RECENCY_KEYWORD_SEARCH_API = "/recency";
    private final String DELETE_RECENCY_KEYWORD_SEARCH_API = "/recency";
    private final String GET_BEST_KEYWORD_SEARCH_API = "/best-keyword";
    private final String GET_AUTO_KEYWORD_SEARCH_API = "/auto-keyword";
    private final String SUCCESS_SAVEKEYWORD = "검색어 저장 완료";
    private final Long ANONYMOUS_ID = 1L;

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<SearchResponseDto> getSearchedBoard(
            @RequestParam(value = "boardPage")
            int boardPage,
            @RequestParam(value = "storePage")
            int storePage,
            @RequestParam(value = "keyword")
            String keyword
    ){
        return ResponseEntity.ok().body(searchService.getSearchResult(boardPage, storePage, keyword));
    }

    @PostMapping
    public ResponseEntity<Map<String, MessageResDto>> saveKeyword(
            @RequestParam("keyword")
            String keyword
    ){
        Long memberId = SecurityUtils.getMemberIdWithAnonymous();

        // 유저가 아니라면 비회원 아이디를 사용
        if (memberId == null){
            memberId = ANONYMOUS_ID;
        }

        searchService.saveKeyword(memberId, keyword);
        return ResponseEntity.ok().body(Map.of("content",new MessageResDto(SUCCESS_SAVEKEYWORD)));
    }

    @GetMapping(GET_RECENCY_KEYWORD_SEARCH_API)
    public ResponseEntity<RecencySearchResponse> getRecencyKeyword(){
        Long memberId = SecurityUtils.getMemberId();

        RecencySearchResponse recencyKeyword = searchService.getRecencyKeyword(memberId);
        var successResponse = ResponseEntity.ok().body(
                recencyKeyword
        );

        return successResponse;
    }

    @DeleteMapping(DELETE_RECENCY_KEYWORD_SEARCH_API)
    public ResponseEntity<Map<String, Boolean>> deleteRecencyKeyword(
            @RequestParam(value = "keyword")
            String keyword){
        Long memberId = SecurityUtils.getMemberId();

        return ResponseEntity.ok().body(
                Map.of("content",searchService.deleteRecencyKeyword(keyword, memberId))
        );
    }

    @GetMapping(GET_BEST_KEYWORD_SEARCH_API)
    public ResponseEntity<Map<String, List<String>>> getBestKeyword(){
        return ResponseEntity.ok().body(
                Map.of("content",searchService.getBestKeyword())
        );
    }

    @GetMapping(GET_AUTO_KEYWORD_SEARCH_API)
    public ResponseEntity<Map<String, List<String>>> getAutoKeyword(
            @RequestParam("keyword")
            String keyword
    ){
        return ResponseEntity.ok().body(
                Map.of("content",searchService.getAutoKeyword(keyword))
        );
    }
}
