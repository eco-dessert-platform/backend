package com.bbangle.bbangle.seller.seller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.seller.domain.AccountVerification;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.AccountVerificationRepository;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.seller.seller.service.command.VerifyAccountCommand;
import com.bbangle.bbangle.seller.seller.service.info.AccountVerificationDetailInfo;
import com.bbangle.bbangle.seller.seller.service.info.AccountVerificationInfo;
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
        Store store = StoreFixture.defaultStore();
        storeRepository.save(store);

        testSeller = SellerFixture.defaultSeller();
        testSeller.registerStore(store);
        sellerRepository.save(testSeller);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("인증된 계좌 확인에 성공한다")
    void success_confirm_verified_account() {
        // arrange: 인증된 계좌 정보 생성
        createAccountVerification(testSeller, true);
        em.flush();
        em.clear();

        // act & assert
        assertThatCode(() -> accountVerificationService.confirmAccount(testSeller.getId()))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("인증되지 않은 계좌 확인 시 예외가 발생한다")
    void fail_confirm_unverified_account() {
        // arrange: 인증되지 않은 계좌 정보 생성
        createAccountVerification(testSeller, false);
        em.flush();
        em.clear();

        // act & assert
        assertThatThrownBy(() -> accountVerificationService.confirmAccount(testSeller.getId()))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.ACCOUNT_NOT_VERIFIED.getMessage());
    }

    @Test
    @DisplayName("계좌 인증 이력이 없는 판매자로 확인 시 예외가 발생한다")
    void fail_confirm_with_non_existent_account_verification() {
        // arrange: 계좌 인증 없이 바로 sellerId 사용
        em.flush();
        em.clear();

        // act & assert
        assertThatThrownBy(() -> accountVerificationService.confirmAccount(testSeller.getId()))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.ACCOUNT_VERIFICATION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("계좌 인증 요청이 성공한다")
    void success_verify_account() {
        // arrange
        Seller seller = sellerRepository.findById(testSeller.getId()).orElseThrow();
        VerifyAccountCommand command = new VerifyAccountCommand(
            seller.getId(),
            "92",
            "123412341234"
        );

        // act
        AccountVerificationInfo result = accountVerificationService.verifyAccount(command);

        // assert
        assertThat(result).isNotNull();
        assertThat(result.sellerId()).isEqualTo(seller.getId());
        assertThat(result.verified()).isTrue();
        assertThat(result.createdAt()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 판매자로 계좌 인증 요청 시 예외가 발생한다")
    void fail_verify_account_with_non_existent_seller() {
        // arrange
        VerifyAccountCommand command = new VerifyAccountCommand(
            99999L,
            "92",
            "123412341234"
        );

        // act & assert
        assertThatThrownBy(() -> accountVerificationService.verifyAccount(command))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.SELLER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("계좌 인증 조회에 성공한다")
    void success_get_account_verification() {
        // arrange
        VerifyAccountCommand command = new VerifyAccountCommand(
            testSeller.getId(),
            "92",
            "123412341234"
        );
        accountVerificationService.verifyAccount(command);
        em.flush();
        em.clear();

        // act
        AccountVerificationDetailInfo result = accountVerificationService.getAccountVerification(testSeller.getId());

        // assert
        assertThat(result).isNotNull();
        assertThat(result.sellerId()).isEqualTo(testSeller.getId());
        assertThat(result.bankCode()).isEqualTo("92");
        assertThat(result.accountNumber()).isEqualTo("123412341234");
        assertThat(result.accountHolder()).isNotNull();
        assertThat(result.verified()).isTrue();
        assertThat(result.createdAt()).isNotNull();
    }

    @Test
    @DisplayName("계좌 인증 이력이 없는 판매자 조회 시 예외가 발생한다")
    void fail_get_account_verification_not_found() {
        // act & assert
        assertThatThrownBy(() -> accountVerificationService.getAccountVerification(testSeller.getId()))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.ACCOUNT_VERIFICATION_NOT_FOUND.getMessage());
    }

    /**
     * 테스트용 AccountVerification 생성 헬퍼 메서드
     */
    private AccountVerification createAccountVerification(Seller seller, boolean verified) {
        AccountVerification accountVerification = AccountVerification.create(
            "92",
            "encryptedAccountNumber",
            "홍길동",
            verified,
            seller
        );
        return accountVerificationRepository.save(accountVerification);
    }
}
