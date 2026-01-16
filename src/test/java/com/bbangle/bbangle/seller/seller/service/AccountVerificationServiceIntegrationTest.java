package com.bbangle.bbangle.seller.seller.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.domain.AccountVerification;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.seller.repository.AccountVerificationRepository;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] AccountVerificationServiceIntegrationTest")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AccountVerificationServiceIntegrationTest {

    @Autowired
    private AccountVerificationService accountVerificationService;

    @Autowired
    private AccountVerificationRepository accountVerificationRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EntityManager em;

    private Seller testSeller;

    @BeforeEach
    void setUp() {
        // arrange: 테스트용 스토어와 판매자 생성
        Store store = Store.builder()
            .name("테스트 스토어")
            .build();
        storeRepository.save(store);

        testSeller = Seller.create(
            "01012345678",
            "01087654321",
            "test@example.com",
            "서울특별시 강남구 테헤란로 123",
            "456호",
            "https://example.com/profile.jpg",
            CertificationStatus.PENDING,
            store
        );
        sellerRepository.save(testSeller);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("인증된 계좌 확인에 성공한다")
    void success_confirm_verified_account() {
        // arrange: 인증된 계좌 정보 생성
        AccountVerification accountVerification = createAccountVerification(testSeller, true);
        em.flush();
        em.clear();

        // act & assert
        assertThatCode(() -> accountVerificationService.confirmAccount(accountVerification.getId()))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("인증되지 않은 계좌 확인 시 예외가 발생한다")
    void fail_confirm_unverified_account() {
        // arrange: 인증되지 않은 계좌 정보 생성
        AccountVerification accountVerification = createAccountVerification(testSeller, false);
        em.flush();
        em.clear();

        // act & assert
        assertThatThrownBy(() -> accountVerificationService.confirmAccount(accountVerification.getId()))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.ACCOUNT_NOT_VERIFIED.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 계좌 인증 ID로 확인 시 예외가 발생한다")
    void fail_confirm_with_non_existent_id() {
        // arrange
        Long nonExistentId = 99999L;

        // act & assert
        assertThatThrownBy(() -> accountVerificationService.confirmAccount(nonExistentId))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.ACCOUNT_VERIFICATION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("null ID로 계좌 확인 시 예외가 발생한다")
    void fail_confirm_with_null_id() {
        // act & assert
        assertThatThrownBy(() -> accountVerificationService.confirmAccount(null))
            .isInstanceOf(Exception.class);
    }

    /**
     * 테스트용 AccountVerification 생성 헬퍼 메서드
     */
    private AccountVerification createAccountVerification(Seller seller, boolean verified) {
        AccountVerification accountVerification = new AccountVerification(
            "테스트은행",
            "1234567890",
            "홍길동",
            verified,
            seller
        );
        return accountVerificationRepository.save(accountVerification);
    }
}
