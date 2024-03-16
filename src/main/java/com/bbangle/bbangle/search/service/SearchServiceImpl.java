package com.bbangle.bbangle.search.service;

import com.bbangle.bbangle.search.dto.RecencySearchResponse;
import com.bbangle.bbangle.search.dto.SearchBoardDto;
import com.bbangle.bbangle.search.dto.SearchStoreDto;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.common.redis.domain.RedisEnum;
import com.bbangle.bbangle.search.domain.Search;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.search.repository.SearchRepository;
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

    private static TrieUtil trie;
    private final String BOARD_MIGRATION = "board";
    private final String STORE_MIGRATION = "store";
    private final int ONE_HOUR = 3600000;
    private final String BEST_KEYWORD_KEY = "keyword";
    private final String[] DEFAULT_SEARCH_KEYWORDS = {"글루텐프리", "비건", "저당", "키토제닉"};
    private final int DEFAULT_PAGE = 10;

    private final SearchRepository searchRepository;
    private final RedisRepository redisRepository;
    private final StoreRepository storeRepository;
    private final BoardRepository boardRepository;

    @Override
    @PostConstruct
    public void initSetting() {
        Map<String, List<Long>> resultBoardMap;
        Map<String, List<Long>> resultStoreMap;

        try {
            // 토큰화 싱글톤 객체 활성화
            KomoranUtil.getInstance();

            // 자동완성 기능의 트리 알고리즘
            trie = new TrieUtil();

            // 모든 상품 게시판 제목을 가져옴
            HashMap<Long, String> boardTitles = boardRepository.getAllBoardTitle();

            // 게시판 제목을 토큰화 한 후 Map<String, List<Long>> 타입으로 변경
            resultBoardMap = getWord(boardTitles, RedisEnum.BOARD.name());

            // 모든 상점 이름을 가져옴
            HashMap<Long, String> storeTitles = storeRepository.getAllStoreTitle();

            // 상점 이름을 토큰화 한 후 Map<String, List<Long>> 타입으로 변경
            resultStoreMap = getWord(storeTitles, RedisEnum.STORE.name());

            // 레디스 검색 게시판명 동기화
            synchronizeRedis(resultBoardMap, BOARD_MIGRATION);
            // 레디스 검색 스토어명 동기화
            synchronizeRedis(resultStoreMap, STORE_MIGRATION);

            log.info("[완료] 레디스에 동기화 완료");

        } catch (Exception e) {
            log.error("[에러] 레디스 서버 장애가 발생했습니다\n에러내용:\n{}", e);
        }
    }

    // 최적화 예정
    private Map<String, List<Long>> getWord(HashMap<Long, String> targetTitles, String targetType) {
        Map<String, List<Long>> resultMap = new HashMap<>();

        for (Map.Entry<Long, String> entry : targetTitles.entrySet()) {
            Long id = entry.getKey();
            String title = entry.getValue();
            trie.insert(title);
            List<String> boardTitleList = targetType == RedisEnum.STORE.name() ? getAllTokenizer(
                    title) : getNTokenizer(title);

            for (String item : boardTitleList) {
                trie.insert(item);

                if (resultMap.containsKey(item)) {
                    resultMap.get(item)
                            .add(id);  // 이미 있는 키에 대해 아이디를 추가
                } else {
                    List<Long> idList = new ArrayList<>();
                    idList.add(id);
                    resultMap.put(item, idList);  // 새로운 키에 대해 새로운 아이디 리스트 생성
                }
            }
        }

        return resultMap;
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

    private KomoranResult getTokenizer(String title) {
        return KomoranUtil.getInstance()
                .analyze(title);
    }

    private void synchronizeRedis(Map<String, List<Long>> resultMap, String migrationType) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);

        String migration = redisRepository.getString(RedisEnum.MIGRATION.name(), migrationType);
        String redisNamespace = migrationType == BOARD_MIGRATION ? RedisEnum.BOARD.name()
                : RedisEnum.STORE.name();

        if (
                migration == null ||
                        LocalDateTime.parse(migration)
                                .isBefore(oneHourAgo)
        ) {
            redisRepository.setFromString(RedisEnum.MIGRATION.name(), migrationType,
                    LocalDateTime.now()
                            .toString());
            uploadRedis(resultMap, redisNamespace);
            log.info("[완료] 보드 동기화");
        }
    }

    @Override
    @Scheduled(fixedRate = ONE_HOUR)
    public void updateRedisAtBestKeyword() {
        // 레디스 BEST_KEYWORD 네임스페이스 가져옴
        String bestKeywordKey = RedisEnum.BEST_KEYWORD.name();
        // (현재 시간 기준 - 24시간 전) 검색 데이터로 가장 많이 검색된 키워드 7개 추출
        String[] bestKeyword = searchRepository.getBestKeyword();

        // 만약 베스트 키워드가 없을 시 기존 데이터 사용
        if (bestKeyword == null || bestKeyword.length == 0) {
            List isRedisKeywordData = redisRepository.getStringList(bestKeywordKey,
                    BEST_KEYWORD_KEY);

            // 레디스 값도 없을때 기본 데이터 저장
            if (isRedisKeywordData.size() == 0) {
                redisRepository.set(bestKeywordKey, BEST_KEYWORD_KEY, DEFAULT_SEARCH_KEYWORDS);
                log.info("인기 검색어 기본값 사용");
                return;
            }
            log.info("이전 인기 검색어 사용");
            return;
        }

        // 베스트 키워드 레디스 삭제
        redisRepository.delete(bestKeywordKey, BEST_KEYWORD_KEY);
        // 최근 베스트 키워드 레디스 등록
        redisRepository.set(bestKeywordKey, BEST_KEYWORD_KEY, bestKeyword);
    }


    @Override
    public void saveKeyword(Long memberId, String keyword) {
        searchRepository.save(
                Search.builder()
                        .member(Member.builder()
                                .id(memberId)
                                .build())
                        .keyword(keyword)
                        .createdAt(LocalDateTime.now())
                        .build());
    }

    @Override

    public SearchBoardDto getSearchBoardDtos(Long memberId, int boardPage, String keyword, String sort, Boolean glutenFreeTag, Boolean highProteinTag,
                                             Boolean sugarFreeTag, Boolean veganTag, Boolean ketogenicTag,
                                             Boolean orderAvailableToday, String category, Integer minPrice, Integer maxPrice) {

        Pageable pageable = PageRequest.of(boardPage, DEFAULT_PAGE);
        if (keyword.isBlank()){
            return SearchBoardDto.builder()
                    .content(List.of())
                    .itemAllCount(0)
                    .pageNumber(pageable.getPageNumber())
                    .limitItemCount(DEFAULT_PAGE)
                    .currentItemCount(0)
                    .existNextPage(false)
                    .build();
        }

        // 검색어 토큰화
        List<String> keys = getAllTokenizer(keyword);

        // 토큰화된 검색어를 통해 게시판 아이디 가져오기
        List<Long> boardIndexs = keys.stream()
            .map(key -> redisRepository.get(RedisEnum.BOARD.name(), key))
            .filter(list -> list != null)  // Filter out null lists
            .flatMap(List::stream)
            .distinct()
            .collect(Collectors.toList());

        if (boardIndexs.size() <= 0){
            return SearchBoardDto.builder()
                    .content(List.of())
                    .itemAllCount(0)
                    .pageNumber(pageable.getPageNumber())
                    .limitItemCount(DEFAULT_PAGE)
                    .currentItemCount(0)
                    .existNextPage(false)
                    .build();
        }

        return searchRepository.getSearchResult(
                memberId, boardIndexs, sort, glutenFreeTag, highProteinTag,
                sugarFreeTag, veganTag, ketogenicTag, orderAvailableToday,
                category, minPrice, maxPrice, pageable);
    }

    @Override
    public SearchStoreDto getSearchStoreDtos(Long memberId, int page, String keyword){

        if (keyword.isBlank()){
            return SearchStoreDto.builder()
                    .content(List.of())
                    .itemAllCount(0)
                    .pageNumber(page)
                    .limitItemCount(DEFAULT_PAGE)
                    .currentItemCount(0)
                    .existNextPage(false)
                    .build();
        }

        // 검색어 토큰화
        List<String> keys = getAllTokenizer(keyword);

        // 토큰화된 검색어를 통해 스토어 아이디 가져오기
        List<Long> storeIndexs = keys.stream()
                .map(key -> redisRepository.get(RedisEnum.STORE.name(), key))
                .filter(list -> list != null)  // Filter out null lists
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());

        if (storeIndexs.size() <= 0) {
            return SearchStoreDto.builder()
                    .content(List.of())
                    .itemAllCount(0)
                    .pageNumber(page)
                    .limitItemCount(DEFAULT_PAGE)
                    .currentItemCount(0)
                    .existNextPage(false)
                    .build();
        }

        var content = searchRepository.getSearchedStore(memberId, storeIndexs, PageRequest.of(page, DEFAULT_PAGE));
        //스토어 및 보드 검색 결과 가져오기
        return SearchStoreDto.builder()
                .content(content)
                .itemAllCount(storeIndexs.size())
                .pageNumber(page)
                .limitItemCount(DEFAULT_PAGE)
                .currentItemCount(content.size())
                .existNextPage(storeIndexs.size() - ((page + 1) * DEFAULT_PAGE) > 0)
                .build();

    }

    @Override
    public RecencySearchResponse getRecencyKeyword(Long memberId) {
        return memberId==1L ?
                RecencySearchResponse.builder()
                        .content(List.of())
                        .build():
                RecencySearchResponse.builder()
                        .content(searchRepository.getRecencyKeyword(
                                Member.builder()
                                        .id(memberId)
                                        .build()))
                        .build();
    }

    @Override
    @Transactional
    public Boolean deleteRecencyKeyword(String keyword, Long memberId) {
        // UPDATE search SET search.isDeleted WHERE id=keywordId AND member = member;
        searchRepository.markAsDeleted(keyword,
                Member.builder().
                        id(memberId).
                        build());
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
        // 초기에 등록된 트리 데이터를 이용해, 자동완성 데이터를 반환
        return trie.autoComplete(keyword, 7);
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
}
