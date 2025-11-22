package com.bbangle.bbangle.store.seller.service;


import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.common.page.StoreCustomPage;
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

@DisplayName("[통합테스트] StoreIntegrationTest")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SellerStoreServiceTest {


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

        // 검색에 걸리는 스토어명 (LIKE '%빵%')
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
            "따끈한빵하우스"
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
                    .isDeleted(false)
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
    @DisplayName("가게 이름 일부를 입력하면 해당하는 가게 이름 목록을 10개 반환한다")
    void selectStoreNameForSeller() {
        // given
        Long cursorId = null;
        String keyword = "빵";
        // when
        StoreCustomPage<List<StoreInfo>> result =
            sellerStoreService.selectStoreNameForSeller(keyword, cursorId);
        // then
        assertThat(result.getContent().size()).isLessThanOrEqualTo(10);

        assertThat(result.getContent())
            .allMatch(info -> info.name().contains(keyword));
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
            .allMatch(info -> info.name().contains("빵"));
    }

}
