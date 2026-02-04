package com.bbangle.bbangle.store.seller.service;


import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.common.page.CursorPagination;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreStatus;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.store.seller.service.model.SellerStoreInfo.StoreInfo;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] SellerStoreService")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SellerStoreServiceIntegrationTest {

    @Autowired
    private SellerStoreService sellerStoreService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EntityManager em;

    @BeforeEach
    void setUp() {
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        em.createNativeQuery("TRUNCATE TABLE store").executeUpdate();
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
        em.clear();

        // 검색에 걸리는 스토어명 (LIKE '%빵%') - 21개
        List<String> matchingNames = List.of(
            "빵굽는하루",
            "오늘의빵집",
            "행복한빵연구소",
            "빵다방",
            "빵의정원",
            "달콤한빵나라",
            "서울빵공작소",
            "빵작업실",
            "엄마의빵집",
            "따끈한빵하우스",
            "시골빵집",
            "동네빵집",
            "프랑스빵집",
            "착한빵집",
            "장인빵집",
            "우리빵집",
            "맛있는빵집",
            "수제빵집",
            "행복한빵집",
            "아침빵집",
            "야간빵집"
        );

        // 검색에 걸리지 않는 스토어명
        List<String> nonMatchingNames = List.of(
            "커피가좋다",
            "구름카페",
            "행복서점",
            "미소문구점",
            "호두과자집",
            "고래서점",
            "작은꽃가게",
            "달빛카페",
            "두부마켓",
            "바닐라마을"
        );

        Stream.concat(matchingNames.stream(), nonMatchingNames.stream())
            .forEach(name -> {
                storeRepository.save(Store.builder()
                    .name(name)
                    .status(StoreStatus.NONE)
                    .build());

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("가게 이름 일부를 입력하면 해당하는 가게 이름 목록을 20개 반환한다")
    void selectStoreNameForSeller() {

        // given
        String keyword = "빵";

        // when
        CursorPagination<StoreInfo> result =
            sellerStoreService.selectStoreNameForSeller(keyword, null);

        // then
        assertThat(result.getContent().size()).isLessThanOrEqualTo(20);

        assertThat(result.getContent())
            .allMatch(info ->
                info.name().replace(" ", "")
                    .contains(keyword.replace(" ", ""))
            );
    }

    @Test
    @DisplayName("일치하는 가게 이름이 없으면 빈 목록을 반환한다")
    void selectStoreNameForSellerWithNoMatch() {

        // given
        String keyword = "NonExistent";

        // when
        var result = sellerStoreService.selectStoreNameForSeller(keyword, null);

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("공백있는 문자열이 넘어오면 이를 제거한다")
    void selectStoreNameForSellerWithWhitespace() {

        // given
        String keyword = "  빵  ";

        // when
        var result = sellerStoreService.selectStoreNameForSeller(keyword, null);

        // then
        assertThat(result.getContent())
            .allMatch(info -> info.name().replace(" ", "").contains("빵"));
    }

    @Test
    @DisplayName("다음 페이지가 존재하면 hasNext와 nextCursor를 반환한다.")
    void cursorPagination_hasNext() {

        // given
        String keyword = " 빵 ";

        // when
        CursorPagination<StoreInfo> result = sellerStoreService.selectStoreNameForSeller(keyword, null);

        // then
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextCursor()).isNotNull();
    }

    @Test
    @DisplayName("cursorId 이후 데이터만 조회된다.")
    void cursorPagination_with_cursorId() {

        // given
        String keyword = " 빵 ";

        CursorPagination<StoreInfo> firstPage = sellerStoreService.selectStoreNameForSeller(keyword, null);
        Long cursorId = firstPage.getNextCursor();

        // when
        CursorPagination<StoreInfo> secondPage = sellerStoreService.selectStoreNameForSeller(keyword, cursorId);

        // then
        assertThat(secondPage.getContent())
            .allMatch(info -> info.id() >= cursorId);
    }
}
