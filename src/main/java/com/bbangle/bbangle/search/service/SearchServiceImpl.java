package com.bbangle.bbangle.search.service;

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

    private Map<String, List<Long>> getWord(HashMap<Long, String> targetTitles, String targetType) {
        /*
        * {id1:title, id2:title, ...} => {title:[id1, id2]} 로 리턴
        * */


        Map<String, List<Long>> resultMap = new HashMap<>();

        for (Map.Entry<Long, String> entry : targetTitles.entrySet()) {
            // 게시판 혹은 스토어 아이디
            Long id = entry.getKey();
            // 게시판 제목 혹은 스토어 명
            String title = entry.getValue();
            //trie 알고리즘에 전체 title 저장
            trie.insert(title);

            // 스토어라면, 토큰화 후 전체 문자 반환
            // 게시판이라면, 토큰화 후 명사만 반환
            //   ~'의'와 같이 '의', '을', '를' 이런 것도 검색어로 저장되기 때문
            List<String> titleList = targetType == RedisEnum.STORE.name() ? getAllTokenizer(
                    title) : getNTokenizer(title);


            for (String item : titleList) {
                //trie 알고리즘에 토큰화된 title 저장
                trie.insert(item);

                if (resultMap.containsKey(item)) {
                    resultMap.get(item).add(id);  // 이미 있는 키에 대해 아이디를 추가
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
        // title 토큰화 => "맛있는 비건 베이커리" => ["맛있", "는", "비건", "베이커리"] 
        return KomoranUtil.getInstance().analyze(title);
    }

    private void synchronizeRedis(Map<String, List<Long>> resultMap, String migrationType) {
        //현재날짜와 시간을 얻어옴
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);

        //레디스에서 마이그레이션의 키값을 가져옴
        String migration = redisRepository.getString(RedisEnum.MIGRATION.name(), migrationType);
        String redisNamespace = migrationType == BOARD_MIGRATION ? RedisEnum.BOARD.name()
                : RedisEnum.STORE.name();

        if (
                // 키값이 없거나 한시간 전이라면 레디스 업데이트
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
    @Transactional
    public void saveKeyword(Long memberId, String keyword) {
        var result = Search.builder()
                .member(Member.builder()
                        .id(memberId)
                        .build())
                .keyword(keyword)
                .createdAt(LocalDateTime.now())
                .build();
        System.out.println(memberId);
        System.out.println(result.getKeyword());
        searchRepository.save(
                result);
    }

    @Override
    @Transactional(readOnly = true)
    public SearchBoardResponse getSearchBoardDtos(Long memberId, SearchBoardRequest boardRequest) {

        Pageable pageable = PageRequest.of(boardRequest.page(), DEFAULT_PAGE);
        // 검색어가 없다면 빈 DTO 반환
        if (boardRequest.keyword().isBlank()){
            return SearchBoardResponse.getEmpty(pageable.getPageNumber(), DEFAULT_PAGE, 0);
        }

        // 검색어 토큰화
        List<String> keys = getAllTokenizer(boardRequest.keyword());

        // 토큰화된 검색어를 통해 게시판 아이디 가져오기
        List<Long> boardIndexs = keys.stream()
            .map(key -> redisRepository.get(RedisEnum.BOARD.name(), key))
            .filter(list -> list != null)  // Filter out null lists
            .flatMap(List::stream)
            .distinct()
            .collect(Collectors.toList());

        // 게시판 Id 리스트가 비었다면 빈 DTO 반환
        if (boardIndexs.size() <= 0){
            return SearchBoardResponse.getEmpty(pageable.getPageNumber(), DEFAULT_PAGE, 0);
        }

        return searchRepository.getSearchedBoard(memberId, boardIndexs, boardRequest, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public SearchStoreResponse getSearchStoreDtos(Long memberId, int page, String keyword){
        // 검색 키워드가 없다면 빈 DTO 반환
        if (keyword.isBlank()){
            return SearchStoreResponse.getEmpty(page, DEFAULT_PAGE);
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

        // 스토어 Id 리스트가 비었다면 빈 DTO 반환
        if (storeIndexs.size() <= 0) {
            return SearchStoreResponse.getEmpty(page, DEFAULT_PAGE);
        }

        var content = searchRepository.getSearchedStore(memberId, storeIndexs, PageRequest.of(page, DEFAULT_PAGE));
        //스토어 및 보드 검색 결과 가져오기
        return SearchStoreResponse.builder()
                .content(content)
                .itemAllCount(storeIndexs.size())
                .pageNumber(page)
                .limitItemCount(DEFAULT_PAGE)
                .currentItemCount(content.size())
                .existNextPage(storeIndexs.size() - ((page + 1) * DEFAULT_PAGE) > 0)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RecencySearchResponse getRecencyKeyword(Long memberId) {
        // 회원이라면 검색어 반환 아니라면 빈 DTO 반환
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
        // 키워드 isDeleted 처리 ;
        searchRepository.markAsDeleted(keyword,
                Member.builder().
                        id(memberId).
                        build());
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getBestKeyword() {
        return redisRepository.getStringList(
                RedisEnum.BEST_KEYWORD.name(),
                BEST_KEYWORD_KEY
        );
    }

    @Override
    @Transactional(readOnly = true)
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
