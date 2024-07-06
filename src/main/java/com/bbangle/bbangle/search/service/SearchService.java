package com.bbangle.bbangle.search.service;

import com.bbangle.bbangle.search.dto.request.SearchBoardRequest;
import com.bbangle.bbangle.search.dto.response.RecencySearchResponse;
import com.bbangle.bbangle.search.dto.response.SearchBoardResponse;
import com.bbangle.bbangle.search.dto.response.SearchStoreResponse;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.common.redis.domain.RedisEnum;
import com.bbangle.bbangle.search.domain.Search;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.search.repository.SearchRepository;
import com.bbangle.bbangle.store.dto.StoreResponseDto;
import com.bbangle.bbangle.util.MorphemeAnalyzer;
import com.bbangle.bbangle.search.service.utils.AutoCompleteUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {
    private static final String BEST_KEYWORD_KEY = "keyword";
    private static final int DEFAULT_PAGE = 10;
    private static final int LIMIT_KEYWORD_COUNT = 10;
    private final SearchRepository searchRepository;
    private final RedisRepository redisRepository;
    private final MorphemeAnalyzer morphemeAnalyzer;
    private final AutoCompleteUtil autoCompleteUtil;

    @Transactional
    public void saveKeyword(Long memberId, String keyword) {
        var member = Member.builder()
            .id(memberId)
            .build();

        var search = Search.builder()
            .member(member)
            .keyword(keyword)
            .createdAt(LocalDateTime.now())
            .build();

        searchRepository.save(search);
    }

    @Transactional(readOnly = true)
    public SearchBoardResponse getSearchBoardDtos(Long memberId, SearchBoardRequest boardRequest) {

        Pageable pageable = PageRequest.of(boardRequest.page(), DEFAULT_PAGE);

        if (boardRequest.keyword().isBlank()) {
            return SearchBoardResponse.getEmpty(pageable.getPageNumber(), DEFAULT_PAGE, 0L);
        }

        List<String> keywordTokens = morphemeAnalyzer.getAllTokenizer(boardRequest.keyword());

        List<Long> searchedBoardIndexs = keywordTokens.stream()
            .map(key -> redisRepository.get(RedisEnum.BOARD.name(), key))
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .distinct()
            .toList();

        if (searchedBoardIndexs.isEmpty()) {
            return SearchBoardResponse.getEmpty(pageable.getPageNumber(), DEFAULT_PAGE, 0L);
        }

        Long searchedBoardAllCount = searchRepository.getSearchedBoardAllCount(boardRequest,
            searchedBoardIndexs);
        return searchRepository.getSearchedBoard(memberId, searchedBoardIndexs, boardRequest,
            pageable, searchedBoardAllCount);
    }

    @Transactional(readOnly = true)
    public SearchStoreResponse getSearchStoreDtos(Long memberId, int page, String keyword) {
        if (keyword.isBlank()) {
            return SearchStoreResponse.getEmpty(page, DEFAULT_PAGE);
        }

        List<String> keywordTokens = morphemeAnalyzer.getAllTokenizer(keyword);

        List<Long> storeIndexs = keywordTokens.stream()
            .map(key -> redisRepository.get(RedisEnum.STORE.name(), key))
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .distinct()
            .toList();

        if (storeIndexs.isEmpty()) {
            return SearchStoreResponse.getEmpty(page, DEFAULT_PAGE);
        }

        List<StoreResponseDto> storeResponseDtos = searchRepository.getSearchedStore(memberId,
            storeIndexs, PageRequest.of(page, DEFAULT_PAGE));
        //스토어 및 보드 검색 결과 가져오기
        return SearchStoreResponse.builder()
            .content(storeResponseDtos)
            .itemAllCount(storeIndexs.size())
            .pageNumber(page)
            .limitItemCount(DEFAULT_PAGE)
            .currentItemCount(storeResponseDtos.size())
            .existNextPage(storeIndexs.size() - ((page + 1) * DEFAULT_PAGE) > 0)
            .build();
    }

    @Transactional(readOnly = true)
    public RecencySearchResponse getRecencyKeyword(Long memberId) {
        Member member = Member.builder()
            .id(memberId)
            .build();

        return memberId == 1L ?
            RecencySearchResponse.getEmpty() :
            RecencySearchResponse.builder()
                .content(searchRepository.getRecencyKeyword(member))
                .build();
    }

    @Transactional
    public Boolean deleteRecencyKeyword(String keyword, Long memberId) {
        Member member = Member.builder()
            .id(memberId)
            .build();

        searchRepository.markAsDeleted(keyword, member);
        return true;
    }

    public List<String> getBestKeyword() {
        return redisRepository.getStringList(
            RedisEnum.BEST_KEYWORD.name(),
            BEST_KEYWORD_KEY
        );
    }

    public List<String> getAutoKeyword(String keyword) {
        return autoCompleteUtil.autoComplete(keyword, LIMIT_KEYWORD_COUNT);
    }
}
