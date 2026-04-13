package com.bbangle.bbangle.seller.seller.service.info;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.seller.domain.AccountVerification;
import com.bbangle.bbangle.seller.domain.Seller;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;

@DisplayName("[단위 테스트] AccountVerificationDetailInfo")
@ExtendWith(MockitoExtension.class)
class AccountVerificationDetailInfoUnitTest {

    @Mock
    private AccountVerification accountVerification;

    @Mock
    private Seller seller;

    @Test
    @DisplayName("AccountVerification으로부터 AccountVerificationDetailInfo 생성에 성공한다")
    void success_create_from_account_verification() {
        // arrange
        Long id = 1L;
        Long sellerId = 10L;
        String bankCode = "92";
        String accountHolder = "홍길동";
        String decryptedAccountNumber = "123412341234";
        boolean verified = true;
        LocalDateTime createdAt = LocalDateTime.of(2025, 12, 10, 14, 32, 10);

        given(accountVerification.getId()).willReturn(id);
        given(accountVerification.getSeller()).willReturn(seller);
        given(seller.getId()).willReturn(sellerId);
        given(accountVerification.getBankCode()).willReturn(bankCode);
        given(accountVerification.getAccountHolder()).willReturn(accountHolder);
        given(accountVerification.isVerified()).willReturn(verified);
        given(accountVerification.getCreatedAt()).willReturn(createdAt);

        // act
        AccountVerificationDetailInfo info = AccountVerificationDetailInfo.of(
            accountVerification, decryptedAccountNumber
        );

        // assert
        assertThat(info.id()).isEqualTo(id);
        assertThat(info.sellerId()).isEqualTo(sellerId);
        assertThat(info.bankCode()).isEqualTo(bankCode);
        assertThat(info.accountNumber()).isEqualTo(decryptedAccountNumber);
        assertThat(info.accountHolder()).isEqualTo(accountHolder);
        assertThat(info.verified()).isTrue();
        assertThat(info.createdAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("미인증 계좌의 AccountVerificationDetailInfo 생성에 성공한다")
    void success_create_from_unverified_account_verification() {
        // arrange
        given(accountVerification.getId()).willReturn(2L);
        given(accountVerification.getSeller()).willReturn(seller);
        given(seller.getId()).willReturn(10L);
        given(accountVerification.getBankCode()).willReturn("88");
        given(accountVerification.getAccountHolder()).willReturn(null);
        given(accountVerification.isVerified()).willReturn(false);
        given(accountVerification.getCreatedAt()).willReturn(null);

        // act
        AccountVerificationDetailInfo info = AccountVerificationDetailInfo.of(
            accountVerification, "123456789"
        );

        // assert
        assertThat(info.verified()).isFalse();
        assertThat(info.accountNumber()).isEqualTo("123456789");
    }
}
