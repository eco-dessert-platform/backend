package com.bbangle.bbangle.charge.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.TestContainersConfig;
import com.bbangle.bbangle.charge.domain.ChargeBalance;
import com.bbangle.bbangle.config.QueryDslConfig;
import com.bbangle.bbangle.fixture.charge.domain.ChargeBalanceFixture;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.search.repository.component.SearchFilter;
import com.bbangle.bbangle.search.repository.component.SearchSort;
import com.bbangle.bbangle.seller.domain.Seller;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DisplayName("[Repository] ChargeBalanceRepository")
@ActiveProfiles("test")
@Import({
    TestContainersConfig.class,
    QueryDslConfig.class,
    SearchFilter.class,
    SearchSort.class
})
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChargeBalanceRepositoryTest {

    @Autowired
    private ChargeBalanceRepository sut;

    @Autowired
    private EntityManager em;

    @BeforeEach
    void resetTable() {
        em.flush();
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        em.createNativeQuery("TRUNCATE TABLE charge_balance").executeUpdate();
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
    }

    @Nested
    @DisplayName("findBySellerId")
    class FindBySellerIdTest {

        @Test
        @DisplayName("판매자 ID로 충전금 잔액을 정상 조회한다")
        void testFindBySellerIdSuccess() {
            // given
            Seller seller = SellerFixture.defaultSeller();
            em.persist(seller);

            ChargeBalance chargeBalance = ChargeBalanceFixture.createDefault(seller);
            em.persist(chargeBalance);
            em.flush();
            em.clear();

            // when
            var result = sut.findBySellerId(seller.getId());

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getSeller().getId()).isEqualTo(seller.getId());
            assertThat(result.get().getBalance()).isEqualByComparingTo(new BigDecimal("100000"));
            assertThat(result.get().getTotalDeposited()).isEqualByComparingTo(new BigDecimal("500000"));
            assertThat(result.get().getTotalWithdrawn()).isEqualByComparingTo(new BigDecimal("400000"));
        }

        @Test
        @DisplayName("존재하지 않는 판매자 ID로 조회하면 empty Optional을 반환한다")
        void testFindBySellerIdNotFound() {
            // given
            Long nonExistentSellerId = 99999L;

            // when
            var result = sut.findBySellerId(nonExistentSellerId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("여러 판매자가 있을 때 특정 판매자의 충전금만 조회한다")
        void testFindBySellerIdWithMultipleSellers() {
            // given
            Seller seller1 = SellerFixture.defaultSeller();
            Seller seller2 = SellerFixture.defaultSeller();
            em.persist(seller1);
            em.persist(seller2);

            ChargeBalance chargeBalance1 = ChargeBalanceFixture.createDefault(seller1);
            ChargeBalance chargeBalance2 = ChargeBalanceFixture.createWithBalance(
                seller2, new BigDecimal("250000")
            );

            em.persist(chargeBalance1);
            em.persist(chargeBalance2);
            em.flush();
            em.clear();

            // when
            var result1 = sut.findBySellerId(seller1.getId());
            var result2 = sut.findBySellerId(seller2.getId());

            // then
            assertThat(result1).isPresent();
            assertThat(result1.get().getBalance()).isEqualByComparingTo(new BigDecimal("100000"));
            assertThat(result1.get().getSeller().getId()).isEqualTo(seller1.getId());

            assertThat(result2).isPresent();
            assertThat(result2.get().getBalance()).isEqualByComparingTo(new BigDecimal("250000"));
            assertThat(result2.get().getSeller().getId()).isEqualTo(seller2.getId());
        }

        @Test
        @DisplayName("같은 판매자에 대해 중복 조회 시 동일한 결과를 반환한다")
        void testFindBySellerIdConsistency() {
            // given
            Seller seller = SellerFixture.defaultSeller();
            em.persist(seller);

            ChargeBalance chargeBalance = ChargeBalanceFixture.createWithBalance(
                seller, new BigDecimal("150000")
            );
            em.persist(chargeBalance);
            em.flush();
            em.clear();

            // when
            var result1 = sut.findBySellerId(seller.getId());
            var result2 = sut.findBySellerId(seller.getId());

            // then
            assertThat(result1).isPresent();
            assertThat(result2).isPresent();
            assertThat(result1.get().getId()).isEqualTo(result2.get().getId());
            assertThat(result1.get().getBalance()).isEqualTo(result2.get().getBalance());
        }
    }
}
