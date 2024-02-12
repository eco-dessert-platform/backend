package com.bbangle.bbangle.controller;

import com.bbangle.bbangle.dto.KeywordDto;
import com.bbangle.bbangle.dto.MessageResDto;
import com.bbangle.bbangle.dto.SearchResponseDto;
import com.bbangle.bbangle.service.SearchService;
import com.bbangle.bbangle.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/search")
public class SearchController {
    private final String GET_RECENCY_KEYWORD_SEARCH_API = "/recency";
    private final String DELETE_RECENCY_KEYWORD_SEARCH_API = "/recency/{keywordId}";
    private final String GET_BEST_KEYWORD_SEARCH_API = "/best-keyword";
    private final String GET_AUTO_KEYWORD_SEARCH_API = "/auto-keyword";
    private final Long ANONYMOUS_ID = 0L;

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<SearchResponseDto> getSearchedBoard(
            @RequestParam(value = "keyword")
            String keyword
    ){
        return ResponseEntity.ok().body(searchService.getSearchResult(keyword));
    }

    @PostMapping
    public ResponseEntity<MessageResDto> saveKeyword(
            @RequestParam("keyword")
            String keyword
    ){
        Long memberId = SecurityUtils.getUserIdWithAnonymous();

        // 유저가 아니라면 비회원 아이디를 사용
        if (memberId == null){
            memberId = ANONYMOUS_ID;
        }

        log.info("ID:{}", memberId);
        searchService.saveKeyword(memberId, keyword);
        return ResponseEntity.ok().body(new MessageResDto("검색어 저장 완료"));
    }

    @GetMapping(GET_RECENCY_KEYWORD_SEARCH_API)
    public ResponseEntity<List<KeywordDto>> getRecencyKeyword(){
        Long memberId = SecurityUtils.getMemberId();
        List<KeywordDto> keywords = searchService.getRecencyKeyword(memberId);
        var successResponse = ResponseEntity.ok().body(keywords);

        return successResponse;
    }

    @DeleteMapping(DELETE_RECENCY_KEYWORD_SEARCH_API)
    public ResponseEntity<Boolean> deleteRecencyKeyword(
            @PathVariable(value = "keywordId")
            Long keywordId){
        Long memberId = SecurityUtils.getMemberId();

        return ResponseEntity.ok().body(
                searchService.deleteRecencyKeyword(keywordId, memberId)
        );
    }

    @GetMapping(GET_BEST_KEYWORD_SEARCH_API)
    public ResponseEntity<List<String>> getBestKeyword(){
        return ResponseEntity.ok().body(
            searchService.getBestKeyword()
        );
    }

    @GetMapping(GET_AUTO_KEYWORD_SEARCH_API)
    public ResponseEntity<List<String>> getAutoKeyword(
            @RequestParam("keyword")
            String keyword
    ){
        return ResponseEntity.ok().body(
                searchService.getAutoKeyword(keyword)
        );
    }
}
