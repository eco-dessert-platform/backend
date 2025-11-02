package com.bbangle.bbangle.search.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.fixture.FixtureConfig;
import com.bbangle.bbangle.fixture.MemberFixture;
import com.bbangle.bbangle.fixture.SearchFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.search.customer.dto.KeywordDto;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@Import(FixtureConfig.class)
@Transactional
class SearchRepositoryTest extends AbstractIntegrationTest {

    private static final int ONEDAY = 24;
    private static final Long NULL_CURSOR = null;
    @Autowired
    SearchRepository searchRepository;
    @Autowired
    RedisRepository redisRepository;
    @Autowired
    SearchFixture searchFixture;

    @AfterEach
    void deleteAllEntity() {
        redisRepository.deleteAll();
        searchRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("getBestKeyword 메서드는 베스트 게시물 검색어를 조회할 수 있다")
    void getBestKeywordTest() {
        Member member1 = memberRepository.save(MemberFixture.createKakaoMember());
        Set<String> keywords = new HashSet<>();

        while (keywords.size() < 7) {
            keywords.add(searchFixture.create(member1.getId()).getKeyword());
        }
        LocalDateTime oneDayAgo = getOneDayAgo();
        String[] bestKeywords = searchRepository.getBestKeyword(oneDayAgo);

        for (String keyword : bestKeywords) {
            assertThat(keyword).isIn(keywords);
        }
    }

    private LocalDateTime getOneDayAgo() {
        return LocalDateTime.now()
            .minusHours(ONEDAY);
    }

    @Test
    @DisplayName("getRecencyKeyword 메서드는 최근 키워드를 조회할 수 있다")
    void getRecentKeywordTest() {
        int LIMIT_KEYWORD_COUNT = 7;

        Member member1 = memberRepository.save(MemberFixture.createKakaoMember());
        Set<String> keywords = new HashSet<>();

        while (keywords.size() < 9) {
            keywords.add(searchFixture.create(member1.getId()).getKeyword());
        }

        List<KeywordDto> recencyKewords = searchRepository.getRecencyKeyword(member1.getId());
        assertThat(recencyKewords).hasSize(LIMIT_KEYWORD_COUNT);
    }

}
