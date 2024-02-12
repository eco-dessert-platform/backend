package com.bbangle.bbangle.service.impl;

import com.bbangle.bbangle.dto.KeywordDto;
import com.bbangle.bbangle.dto.SearchResponseDto;
import com.bbangle.bbangle.dto.StoreResponseDto;
import com.bbangle.bbangle.model.Member;
import com.bbangle.bbangle.model.RedisEnum;
import com.bbangle.bbangle.model.Search;
import com.bbangle.bbangle.model.Store;
import com.bbangle.bbangle.repository.InitRepository;
import com.bbangle.bbangle.repository.RedisRepository;
import com.bbangle.bbangle.repository.SearchRepository;
import com.bbangle.bbangle.repository.StoreRepository;
import com.bbangle.bbangle.service.SearchService;
import com.bbangle.bbangle.util.KomoranUtil;
import com.bbangle.bbangle.util.TrieUtil;
import jakarta.annotation.PostConstruct;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private static TrieUtil trie;
    private final String MIGRATION="migration";
    private final int ONE_HOUR = 3600000;
    private final String BEST_KEYWORD_KEY = "keyword";

    private final SearchRepository searchRepository;
    private final StoreRepository storeRepository;
    private final RedisRepository redisRepository;
    private final InitRepository initRepository;

    @Override
    @PostConstruct
    public void loadData() {
        Map<String, List<Long>> resultBoardMap;
        Map<String, List<Long>> resultStoreMap;

        try{
            if (!redisRepository.get(RedisEnum.BOARD.label(), MIGRATION).isEmpty()){
                log.info("[완료] 이미 동기화가 되어 있습니다");
                return;
            }
        } catch (Exception e) {
            log.error("[에러] 레디스 서버 장애가 발생했습니다\n에러내용:\n{}", e);
            return;
        }
        finally {
            // 토큰화 싱글톤 객체 활성화
            KomoranUtil.getInstance();

            // 자동완성 기능의 트리 알고리즘
            trie = new TrieUtil();

            // 모든 상품 게시판 제목을 가져옴
            HashMap<Long, String> boardTitles = initRepository.getAllBoardTitle();

            boardTitles.entrySet().forEach(entry ->{
                trie.insert(entry.getValue());
            });

            // 게시판 제목을 토큰화 한 후 Map<String, List<Long>> 타입으로 변경
            resultBoardMap = getWord(boardTitles, RedisEnum.BOARD.label());

            // 토큰화된 게시판 제목을 트리에 등록
            resultBoardMap.entrySet().forEach(entry ->{
                trie.insert(entry.getKey());
            });

            // 모든 상점 이름을 가져옴
            HashMap<Long, String> storeTitles = initRepository.getAllStoreTitle();
            // 상점 전체 이름을 트리에 등록
            storeTitles.entrySet().forEach(entry ->{
                trie.insert(entry.getValue());
            });

            // 상점 이름을 토큰화 한 후 Map<String, List<Long>> 타입으로 변경
            resultStoreMap = getWord(storeTitles, RedisEnum.STORE.label());

            // 토큰화된 상점 이름을 트리에 등록
            resultStoreMap.entrySet().forEach(entry ->{
                trie.insert(entry.getKey());
            });
        }

        uploadRedis(resultBoardMap, RedisEnum.BOARD.label());
        log.info("[완료] 보드 동기화");

        uploadRedis(resultStoreMap, RedisEnum.STORE.label());
        log.info("[완료] 스토어 동기화");
        log.info("[완료] 레디스에 동기화 완료");
    }

    // 최적화 예정
    private Map<String, List<Long>> getWord(HashMap<Long, String> targetTitles, String targetType){
        Map<String, List<Long>> resultMap = new HashMap<>();

        for (Map.Entry<Long, String> entry : targetTitles.entrySet()) {
            Long id = entry.getKey();
            String title = entry.getValue();
            List<String> boardTitleList = targetType==RedisEnum.STORE.label() ?  getAllTokenizer(title) : getNTokenizer(title);

            for (String item : boardTitleList) {
                if (resultMap.containsKey(item)) {
                    resultMap.get(item).add(id);  // 이미 있는 키에 대해 아이디를 추가
                } else {
                    List<Long> idList = new ArrayList<>();
                    idList.add(id);
                    resultMap.put(item, idList);  // 새로운 키에 대해 새로운 아이디 리스트 생성
                }
            }
        }

        if (targetType == RedisEnum.BOARD.label()){
            resultMap.put(MIGRATION, List.of(0L));
        }

        return resultMap;
    }

    private void uploadRedis(Map<String, List<Long>> resultMap, String targetType){
        // resultMap을 토큰 : [BoardId,...] 로 변경하여 저장
        for (Map.Entry<String, List<Long>> entry : resultMap.entrySet()) {
            redisRepository.set(targetType,entry.getKey(),
                    entry.getValue()
                            .stream()
                            .map(id -> id.toString())
                            .toArray(String[]::new));
        }
    }


    private KomoranResult getTokenizer(String title) {
        return KomoranUtil.getInstance().analyze(title);
    }

    @Override
    @Scheduled(fixedRate = ONE_HOUR)
    public void updateRedisAtBestKeyword() {
        // 레디스 BEST_KEYWORD 네임스페이스 가져옴
        String bestKeywordKey = RedisEnum.BEST_KEYWORD.label();
        // (현재 시간 기준 - 24시간 전) 검색 데이터로 가장 많이 검색된 키워드 7개 추출
        String[] bestKeyword = searchRepository.getBestKeyword();

        // 만약 베스트 키워드가 없을 시 기존 데이터 사용
        if (bestKeyword == null) {
            return;
        }

        // 베스트 키워드 레디스 삭제
        redisRepository.delete(bestKeywordKey, BEST_KEYWORD_KEY);
        // 최근 베스트 키워드 레디스 등록
        redisRepository.set(bestKeywordKey, BEST_KEYWORD_KEY, bestKeyword);
    }


    @Override
    public void saveKeyword(Long memberId, String keyword){
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
    public SearchResponseDto getSearchResult(String keyword) {

        // 검색어 토큰화
        List<String> keys = getAllTokenizer(keyword);

        // 토큰화된 검색어를 통해 게시판 아이디 가져오기
        List<Long> boardIndexs = keys.stream()
                .map(key -> redisRepository.get(RedisEnum.BOARD.label(), key))
                .filter(list -> list != null)  // Filter out null lists
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());

        // 토큰화된 검색어를 통해 스토어 아이디 가져오기
        List<Long> storeIndexs = keys.stream()
                .map(key -> redisRepository.get(RedisEnum.STORE.label(), key))
                .filter(list -> list != null)  // Filter out null lists
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());

        int pageNumber = 0;  // 첫 번째 페이지
        int pageSize = 10;  // 페이지당 10개 아이템

        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(pageNumber, pageSize);


        //스토어 및 보드 검색 결과 가져오기
        var searchBoardResult = searchRepository.getSearchResult(boardIndexs, pageable);
        var searchStoreResult = getList(storeIndexs, pageable);

        return new SearchResponseDto(searchBoardResult, searchStoreResult);
    }

    private Slice<StoreResponseDto> getList(List<Long> ides, Pageable pageable) {
        Slice<Store> sliceBy = storeRepository.findByIdIn(ides ,pageable);

        List<StoreResponseDto> dtoList = sliceBy.getContent()
                .stream()
                .map(StoreResponseDto::fromWithoutLogin)
                .collect(Collectors.toList());

        return new SliceImpl<>(dtoList, pageable, sliceBy.hasNext());
    }

    @Override
    public List<KeywordDto> getRecencyKeyword(Long memberId) {
        return searchRepository.getRecencyKeyword(
                Member.builder()
                        .id(memberId)
                        .build()
        );
    }

    @Override
    public Boolean deleteRecencyKeyword(Long keywordId, Long memberId) {
        try {
            // UPDATE search SET search.isDeleted WHERE id=keywordId AND member = member;
            searchRepository.markAsDeleted(keywordId,
                    Member.builder().
                            id(memberId).
                            build());
            return true;
        } catch (Exception e){
            e.getMessage();
            return false;
        }

    }

    @Override
    public List<String> getBestKeyword() {
        return redisRepository.getStringList(
                RedisEnum.BEST_KEYWORD.label(),
                BEST_KEYWORD_KEY
        );
    }

    @Override
    public List<String> getAutoKeyword(String keyword) {
        // 초기에 등록된 트리 데이터를 이용해, 자동완성 데이터를 반환
        return trie.autoComplete(keyword, 7);
    }

    private List<String> getNTokenizer(String title){
        // 토큰화된 단어 중 명사만 반환
        return  getTokenizer(title).getMorphesByTags("NNG", "NNP", "NNB", "NP", "NR", "NA");
    }

    private List<String> getAllTokenizer(String title) {
        // 토큰화된 단어를 전부 반환
        return getTokenizer(title).getTokenList().stream().map(token-> token.getMorph()).toList();
    }
}