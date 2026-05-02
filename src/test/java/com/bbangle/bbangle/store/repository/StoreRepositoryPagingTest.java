package com.bbangle.bbangle.store.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.TestContainersConfig;
import com.bbangle.bbangle.common.page.CursorPagination;
import com.bbangle.bbangle.config.QueryDslConfig;
import com.bbangle.bbangle.search.repository.component.SearchFilter;
import com.bbangle.bbangle.search.repository.component.SearchSort;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.seller.service.model.SellerStoreInfo.StoreInfo;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[슬라이스 테스트] StoreRepository - 페이징 조회")
@ActiveProfiles("test")
@Import({
    TestContainersConfig.class,
    QueryDslConfig.class,
    SearchFilter.class,
    SearchSort.class
})
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Slf4j
public class StoreRepositoryPagingTest {

    private static final int TOTAL_STORES = 25;
    private static final long PAGE_SIZE = 20L;
    // 시간을 조작할 변수 (static으로 공유)
    static LocalDateTime mockTime = LocalDateTime.now().minusDays(1);

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EntityManager em;

    @BeforeEach
    void setUp() {
        // TRUNCATE는 @Transactional 영향을 받지 않음
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        em.createNativeQuery("TRUNCATE TABLE store").executeUpdate();
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
        em.clear();

        // 데이터 생성
        IntStream.rangeClosed(1, TOTAL_STORES).forEach(i -> {
            mockTime = mockTime.plusSeconds(1);

            storeRepository.save(Store.builder()
                .name("Store " + i)
                .build());
        });
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("스토어 이름 검색 - 첫 페이지를 조회한다")
    void findByStoreNameWithCursor_firstPage_success() {

        // given
        String storeName = "Store";

        // when
        CursorPagination<StoreInfo> result = storeRepository.findByStoreNameWithCursor(storeName, null);

        // then
        assertThat(result.getContent()).hasSize((int) PAGE_SIZE);
        assertThat(result.getHasNext()).isTrue();

        // id ASC이므로 가장 먼저 생성된 Store 1 부터
        assertThat(result.getContent().get(0).name()).isEqualTo("Store 1");
        assertThat(result.getContent().get(19).name()).isEqualTo("Store 20");

        // nextCursor는 21번째 Store의 id
        assertThat(result.getNextCursor()).isEqualTo(
            result.getContent().get(19).id() + 1
        );
    }

    @Test
    @DisplayName("스토어 이름 검색 - 두 번째 페이지를 cursorId 기준으로 조회한다.")
    void findByStoreNameWithCursor_secondPage_success() {

        // given
        String storeName = "Store";

        CursorPagination<StoreInfo> firstPage = storeRepository.findByStoreNameWithCursor(storeName, null);
        Long cursorId = firstPage.getNextCursor();

        // when
        CursorPagination<StoreInfo> secondPage = storeRepository.findByStoreNameWithCursor(storeName, cursorId);

        // then
        assertThat(secondPage.getContent()).hasSize(5);
        assertThat(secondPage.getHasNext()).isFalse();

        assertThat(secondPage.getContent().get(0).name()).isEqualTo("Store 21");
        assertThat(secondPage.getContent().get(4).name()).isEqualTo("Store 25");
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public DateTimeProvider dateTimeProvider() {
            // 항상 mockTime 변수의 값을 현재 시간으로 반환
            return () -> Optional.of(mockTime);
        }
    }
}
