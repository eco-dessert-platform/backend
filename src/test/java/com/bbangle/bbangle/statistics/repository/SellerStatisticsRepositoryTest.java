package com.bbangle.bbangle.statistics.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.TestContainersConfig;
import com.bbangle.bbangle.config.QueryDslConfig;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.statistics.domain.SellerStatisticsDailyFixture;
import com.bbangle.bbangle.search.repository.component.SearchFilter;
import com.bbangle.bbangle.search.repository.component.SearchSort;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.statistics.domain.SellerStatisticsDaily;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DisplayName("[Repository] SellerStatisticsRepository")
@ActiveProfiles("test")
@Import({
    TestContainersConfig.class,
    QueryDslConfig.class,
    SearchFilter.class,
    SearchSort.class
})
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SellerStatisticsRepositoryTest {

    @Autowired
    private SellerStatisticsRepository sut;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("판매자 일별 통계를 날짜 범위로 조회하고 오름차순 정렬한다")
    void findBySellerIdAndStatDateBetweenOrderByStatDateAsc() {
        Seller seller = SellerFixture.defaultSeller();
        Seller otherSeller = SellerFixture.defaultSeller();
        em.persist(seller);
        em.persist(otherSeller);

        em.persist(SellerStatisticsDailyFixture.create(
            seller,
            LocalDateTime.of(2026, 3, 1, 0, 0),
            3,
            2,
            10000
        ));
        em.persist(SellerStatisticsDailyFixture.create(
            seller,
            LocalDateTime.of(2026, 3, 3, 0, 0),
            1,
            1,
            3000
        ));
        em.persist(SellerStatisticsDailyFixture.create(
            otherSeller,
            LocalDateTime.of(2026, 3, 2, 0, 0),
            9,
            7,
            50000
        ));

        em.flush();
        em.clear();

        List<SellerStatisticsDaily> result = sut.findBySellerIdAndStatDateBetweenOrderByStatDateAsc(
            seller.getId(),
            LocalDateTime.of(2026, 3, 1, 0, 0),
            LocalDateTime.of(2026, 3, 7, 23, 59, 59)
        );

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStatDate()).isEqualTo(LocalDateTime.of(2026, 3, 1, 0, 0));
        assertThat(result.get(0).getTotalOrdersCount()).isEqualTo(3L);
        assertThat(result.get(0).getTotalBuyersCount()).isEqualTo(2L);
        assertThat(result.get(1).getStatDate()).isEqualTo(LocalDateTime.of(2026, 3, 3, 0, 0));
    }
}
