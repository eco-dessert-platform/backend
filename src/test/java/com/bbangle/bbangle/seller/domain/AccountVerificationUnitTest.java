package com.bbangle.bbangle.seller.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("[단위 테스트] AccountVerification")
@ExtendWith(MockitoExtension.class)
class AccountVerificationUnitTest {

    @Mock
    private Seller seller;

    @Test
    @DisplayName("계좌 인증 정보 생성에 성공한다")
    void success_create_account_verification() {
        // arrange
        String bankCode = "92";
        String encryptedAccountNumber = "encryptedValue123";
        String accountHolder = "홍길동";
        boolean verified = true;

        // act
        AccountVerification accountVerification = AccountVerification.create(
            bankCode, encryptedAccountNumber, accountHolder, verified, seller
        );

        // assert
        assertThat(accountVerification).isNotNull();
        assertThat(accountVerification.getBankCode()).isEqualTo(bankCode);
        assertThat(accountVerification.getAccountNumber()).isEqualTo(encryptedAccountNumber);
        assertThat(accountVerification.getAccountHolder()).isEqualTo(accountHolder);
        assertThat(accountVerification.isVerified()).isTrue();
        assertThat(accountVerification.getSeller()).isEqualTo(seller);
    }

    @Test
    @DisplayName("미인증 상태의 계좌 인증 정보 생성에 성공한다")
    void success_create_unverified_account_verification() {
        // arrange
        String bankCode = "88";
        String encryptedAccountNumber = "encryptedValue456";
        String accountHolder = "김철수";
        boolean verified = false;

        // act
        AccountVerification accountVerification = AccountVerification.create(
            bankCode, encryptedAccountNumber, accountHolder, verified, seller
        );

        // assert
        assertThat(accountVerification).isNotNull();
        assertThat(accountVerification.isVerified()).isFalse();
    }
}
