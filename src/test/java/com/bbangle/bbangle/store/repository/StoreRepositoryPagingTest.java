package com.bbangle.bbangle.store.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.TestContainersConfig;
import com.bbangle.bbangle.common.page.CursorPagination;
import com.bbangle.bbangle.config.QueryDslConfig;
import com.bbangle.bbangle.search.repository.component.SearchFilter;
import com.bbangle.bbangle.search.repository.component.SearchSort;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreStatus;
import com.bbangle.bbangle.store.seller.service.model.SellerStoreInfo.StoreInfo;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
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

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EntityManager em;

    // 시간을 조작할 변수 (static으로 공유)
    static LocalDateTime mockTime = LocalDateTime.now().minusDays(1);

    @TestConfiguration
    static class TestConfig {
        @Bean
        public DateTimeProvider dateTimeProvider() {
            // 항상 mockTime 변수의 값을 현재 시간으로 반환
            return () -> Optional.of(mockTime);
        }
    }

    private static final int TOTAL_STORES = 25;
    private static final long PAGE_SIZE = 20L;

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
                .status(StoreStatus.NONE)
                .build());

        });
        em.flush();
        em.clear();
    }


    @Test
    @DisplayName("첫 페이지를 정상적으로 조회한다")
    void findNextCursorPage_firstPage_success() {
        List<Long> storeIds = storeRepository.findAll().stream()
            .map(Store::getId)
            .toList();
        // when
        CursorPagination<StoreInfo> result = storeRepository.findNextCursorPage(storeIds);

        // then
        assertThat(result.getContent()).hasSize((int) PAGE_SIZE);
        assertThat(result.getHasNext()).isTrue();
        // createdAt DESC, id DESC 이므로 가장 마지막에 생성된 Store 25가 첫번째로 와야 함
        assertThat(result.getContent().get(0).name()).isEqualTo("Store 25");
        assertThat(result.getContent().get(19).name()).isEqualTo("Store 6");
    }

}
