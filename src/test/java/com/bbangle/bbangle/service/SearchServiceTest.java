package com.bbangle.bbangle.service;

import com.bbangle.bbangle.model.RedisEnum;
import com.bbangle.bbangle.repository.InitRepository;
import com.bbangle.bbangle.repository.RedisRepository;
import com.bbangle.bbangle.util.KomoranUtil;
import com.bbangle.bbangle.util.TrieUtil;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class SearchServiceTest {
    private final String BEST_KEYWORD_KEY = "keyword";

    @Autowired
    InitRepository initRepository;

    @Autowired
    SearchService searchService;

    @Autowired
    RedisRepository redisRepository;


    @BeforeEach
    public void loadData(){
        searchService.loadData();
        searchService.updateRedisAtBestKeyword();
    }

    @Test
    public void trieUtilTest(){
        TrieUtil trieUtil = new TrieUtil();

        trieUtil.insert("비건 베이커리");
        trieUtil.insert("비건");
        trieUtil.insert("비건 베이커리 짱짱");
        trieUtil.insert("초코송이");

        var resultOne = trieUtil.autoComplete("초", 1);
        Assertions.assertEquals(resultOne,List.of("초코송이"));
        Assertions.assertEquals(resultOne.size(),1);

        var resultTwo = trieUtil.autoComplete("비", 2);
        Assertions.assertEquals(resultTwo,List.of("비건", "비건 베이커리"));
        Assertions.assertEquals(resultTwo.size(),2);

        var resultThree = trieUtil.autoComplete("비", 3);
        Assertions.assertEquals(resultThree,List.of("비건", "비건 베이커리", "비건 베이커리 짱짱"));
        Assertions.assertEquals(resultThree.size(),3);

        var resultFour = trieUtil.autoComplete("바", 3);
        Assertions.assertEquals(resultFour,List.of());
        Assertions.assertEquals(resultFour.size(),0);
    }

    @Test
    public void getAllBoardTitleTest(){
        var result = initRepository.getAllBoardTitle();
        System.out.println(result);
    }
    @Test
    public void getBoardIdesTest(){
        searchService.getSearchResult("비건 베이커리");
    }

    @Test
    public List<String> KomoranUtilSingletonTest(String title) {
        var komoran = KomoranUtil.getInstance();
        KomoranResult analyzeResultList = komoran.analyze(title);

        List<Token> tokenList = analyzeResultList.getTokenList();

        return analyzeResultList.getMorphesByTags("NNG", "NNP", "NNB", "SL");
    }

    @Test
    public void getBestKeyword(){
        var result = redisRepository.getStringList(
                RedisEnum.BEST_KEYWORD.label(),
                BEST_KEYWORD_KEY
        );

        Assertions.assertEquals(result.size() < 7, true);

        searchService.updateRedisAtBestKeyword();

        result = redisRepository.getStringList(
                RedisEnum.BEST_KEYWORD.label(),
                BEST_KEYWORD_KEY
        );

        Assertions.assertEquals(result.size() < 7, true);
    }
}
