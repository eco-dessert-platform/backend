package com.bbangle.bbangle.search.service;

import com.bbangle.bbangle.board.dto.BoardAllTitleDto;
import com.bbangle.bbangle.search.dto.request.SearchBoardRequest;
import com.bbangle.bbangle.search.dto.response.RecencySearchResponse;
import com.bbangle.bbangle.search.dto.response.SearchBoardResponse;
import com.bbangle.bbangle.search.dto.response.SearchStoreResponse;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.common.redis.domain.RedisEnum;
import com.bbangle.bbangle.search.domain.Search;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.search.repository.SearchRepository;
import com.bbangle.bbangle.store.dto.StoreResponseDto;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.util.KomoranUtil;
import com.bbangle.bbangle.util.TrieUtil;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private static TrieUtil autoCompleteEngine;
    private final String BOARD_MIGRATION = "board";
    private final String STORE_MIGRATION = "store";
    private final int ONE_HOUR = 3600000;
    private final String BEST_KEYWORD_KEY = "keyword";
    private final String[] DEFAULT_SEARCH_KEYWORDS = {"글루텐프리", "비건", "저당", "키토제닉"};
    private final int DEFAULT_PAGE = 10;
    private final int LIMIT_KEYWORD_COUNT = 10;

    private final SearchRepository searchRepository;
    private final RedisRepository redisRepository;
    private final StoreRepository storeRepository;
    private final BoardRepository boardRepository;

    private void updateRedisWithTokenizedStoreTitles() {
        Map<String, List<Long>> storeTitleTokenToIdMapping;

        HashMap<Long, String> storeTitles = storeRepository.getAllStoreTitle();
        storeTitleTokenToIdMapping = createTitleTokenToIdMapping(storeTitles,
            RedisEnum.STORE.name());
        try {
            synchronizeRedis(storeTitleTokenToIdMapping, STORE_MIGRATION);
        } catch (Exception e) {
            log.error("[에러] 레디스 서버 장애가 발생했습니다\n에러내용:\n{}", e);
        }
    }

    private List<String> getNTokenizer(String title) {
        // 토큰화된 단어 중 명사만 반환
        return getTokenizer(title).getMorphesByTags("NNG", "NNP", "NNB", "NP", "NR", "NA");
    }

    private List<String> getAllTokenizer(String title) {
        // 토큰화된 단어를 전부 반환
        return getTokenizer(title).getTokenList()
            .stream()
            .map(token -> token.getMorph())
            .toList();
    }

    private List<String> tokenizeTitle(String title, String targetType) {
        return targetType.equals(RedisEnum.STORE.name()) ? getAllTokenizer(title)
            : getNTokenizer(title);
    }


    private void createAutoCompleateNode(String title) {
        autoCompleteEngine.insert(title);

        List<String> allTokens = getAllTokenizer(title);

        for (String token : allTokens) {
            autoCompleteEngine.insert(token);
        }
    }

    private void addTokenToMapping(Map<String, List<Long>> mapping, String token, Long id) {
        mapping.computeIfAbsent(token, k -> new ArrayList<>()).add(id);
    }

    private Map<String, List<Long>> createTitleTokenToIdMapping(Map<Long, String> targetTitles,
        String targetType) {
        Map<String, List<Long>> titleTokenToIdMapping = new HashMap<>();

        for (Map.Entry<Long, String> entry : targetTitles.entrySet()) {
            Long id = entry.getKey();
            String title = entry.getValue();

            createAutoCompleateNode(title);

            List<String> tokens = tokenizeTitle(title, targetType);
            for (String token : tokens) {
                addTokenToMapping(titleTokenToIdMapping, token, id);
            }
        }

        return titleTokenToIdMapping;
    }

    private KomoranResult getTokenizer(String title) {
        // title 토큰화 => "맛있는 비건 베이커리" => ["맛있", "는", "비건", "베이커리"]
        return KomoranUtil.getInstance().analyze(title);
    }

    private void uploadRedis(Map<String, List<Long>> resultMap, String targetType) {
        // resultMap을 토큰 : [BoardId,...] 로 변경하여 저장
        for (Map.Entry<String, List<Long>> entry : resultMap.entrySet()) {
            redisRepository.set(targetType, entry.getKey(),
                entry.getValue()
                    .stream()
                    .map(id -> id.toString())
                    .toArray(String[]::new));
        }
    }

    private void synchronizeRedis(Map<String, List<Long>> resultMap, String migrationType) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);

        String migrationInfo = redisRepository.getString(RedisEnum.MIGRATION.name(), migrationType);
        String redisNamespace = migrationType == BOARD_MIGRATION ? RedisEnum.BOARD.name()
            : RedisEnum.STORE.name();

        if (
            migrationInfo.equals("") ||
                LocalDateTime.parse(migrationInfo)
                    .isBefore(oneHourAgo)
        ) {
            redisRepository.setFromString(RedisEnum.MIGRATION.name(), migrationType,
                LocalDateTime.now()
                    .toString());
            uploadRedis(resultMap, redisNamespace);
            log.info("[완료] 보드 동기화");
        }
    }

    private void updateRedisWithTokenizedBoardTitles() {
        Map<String, List<Long>> boardTitleTokenToIdMapping;

        List<BoardAllTitleDto> boardTitles = boardRepository.findTitleByBoardAll();

        Map<Long, String> titlesAtBoard = boardTitles.stream()
            .collect(Collectors.toMap(BoardAllTitleDto::boardId, BoardAllTitleDto::Title));

        boardTitleTokenToIdMapping = createTitleTokenToIdMapping(titlesAtBoard,
            RedisEnum.BOARD.name());
        try {
            synchronizeRedis(boardTitleTokenToIdMapping, BOARD_MIGRATION);
        } catch (Exception e) {
            log.error("[에러] 레디스 서버 장애가 발생했습니다\n에러내용:\n{}", e);
        }
    }

    @Override
    @PostConstruct
    public void initSetting() {
        KomoranUtil.getInstance();
        autoCompleteEngine = new TrieUtil();

        updateRedisWithTokenizedBoardTitles();
        updateRedisWithTokenizedStoreTitles();
        log.info("[완료] 레디스에 동기화 완료");
    }

    private boolean isKeywordEmpty(String[] bestKeyword) {
        return bestKeyword == null || bestKeyword.length == 0;
    }

    private void updateBestKeywordInRedis(String namespace, String[] bestKeyword) {
        redisRepository.delete(namespace, BEST_KEYWORD_KEY);
        redisRepository.set(namespace, BEST_KEYWORD_KEY, bestKeyword);
    }

    private void setDefaultKeywordsInRedis(String namespace) {
        redisRepository.set(namespace, BEST_KEYWORD_KEY, DEFAULT_SEARCH_KEYWORDS);
        log.info("인기 검색어 기본값 사용");
    }

    private void handleEmptyKeywordFromRepository(String namespace) {
        List<String> bestKeywords = redisRepository.getStringList(namespace, BEST_KEYWORD_KEY);

        if (bestKeywords.isEmpty()) {
            setDefaultKeywordsInRedis(namespace);
        } else {
            log.info("이전 인기 검색어 사용");
        }
    }

    @Override
    @Scheduled(fixedRate = ONE_HOUR)
    public void updateRedisAtBestKeyword() {
        String bestKeywordNamespace = RedisEnum.BEST_KEYWORD.name();
        String[] bestKeyword = searchRepository.getBestKeyword();

        if (isKeywordEmpty(bestKeyword)) {
            handleEmptyKeywordFromRepository(bestKeywordNamespace);
        } else {
            updateBestKeywordInRedis(bestKeywordNamespace, bestKeyword);
        }
    }

    @Override
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

    @Override
    @Transactional(readOnly = true)
    public SearchBoardResponse getSearchBoardDtos(Long memberId, SearchBoardRequest boardRequest) {

        Pageable pageable = PageRequest.of(boardRequest.page(), DEFAULT_PAGE);

        if (boardRequest.keyword().isBlank()) {
            return SearchBoardResponse.getEmpty(pageable.getPageNumber(), DEFAULT_PAGE, 0L);
        }

        List<String> keywordTokens = getAllTokenizer(boardRequest.keyword());

        List<Long> searchedBoardIndexs = keywordTokens.stream()
            .map(key -> redisRepository.get(RedisEnum.BOARD.name(), key))
            .filter(list -> list != null)
            .flatMap(List::stream)
            .distinct()
            .collect(Collectors.toList());

        if (searchedBoardIndexs.isEmpty()) {
            return SearchBoardResponse.getEmpty(pageable.getPageNumber(), DEFAULT_PAGE, 0L);
        }

        Long searchedBoardAllCount = searchRepository.getSearchedBoardAllCount(boardRequest,
            searchedBoardIndexs);
        return searchRepository.getSearchedBoard(memberId, searchedBoardIndexs, boardRequest,
            pageable, searchedBoardAllCount);
    }

    @Override
    @Transactional(readOnly = true)
    public SearchStoreResponse getSearchStoreDtos(Long memberId, int page, String keyword) {
        if (keyword.isBlank()) {
            return SearchStoreResponse.getEmpty(page, DEFAULT_PAGE);
        }

        List<String> keywordTokens = getAllTokenizer(keyword);

        List<Long> storeIndexs = keywordTokens.stream()
            .map(key -> redisRepository.get(RedisEnum.STORE.name(), key))
            .filter(list -> list != null)
            .flatMap(List::stream)
            .distinct()
            .collect(Collectors.toList());

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

    @Override
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

    @Override
    @Transactional
    public Boolean deleteRecencyKeyword(String keyword, Long memberId) {
        Member member = Member.builder()
            .id(memberId)
            .build();

        searchRepository.markAsDeleted(keyword, member);
        return true;
    }

    @Override
    public List<String> getBestKeyword() {
        return redisRepository.getStringList(
            RedisEnum.BEST_KEYWORD.name(),
            BEST_KEYWORD_KEY
        );
    }

    @Override
    public List<String> getAutoKeyword(String keyword) {
        return autoCompleteEngine.autoComplete(keyword, LIMIT_KEYWORD_COUNT);
    }
}
