package com.bbangle.bbangle.seller.seller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.domain.AccountVerification;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.AccountVerificationRepository;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.seller.seller.service.client.AccountVerificationClient;
import com.bbangle.bbangle.seller.seller.service.command.UpdateAccountCommand;
import com.bbangle.bbangle.seller.seller.service.command.VerifyAccountCommand;
import com.bbangle.bbangle.seller.seller.service.info.AccountVerificationDetailInfo;
import com.bbangle.bbangle.seller.seller.service.info.AccountVerificationInfo;
import com.bbangle.bbangle.util.AesEncryptionUtil;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("[단위 테스트] AccountVerificationService")
@ExtendWith(MockitoExtension.class)
class AccountVerificationServiceUnitTest {

    @InjectMocks
    private AccountVerificationService accountVerificationService;

    @Mock
    private AccountVerificationRepository accountVerificationRepository;

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private AccountVerificationClient accountVerificationClient;

    @Mock
    private AesEncryptionUtil aesEncryptionUtil;

    @Mock
    private Seller seller;

    @Mock
    private AccountVerification accountVerification;

    @Test
    @DisplayName("계좌 인증 조회에 성공한다")
    void success_get_account_verification() {
        // arrange
        Long sellerId = 1L;
        String encryptedAccountNumber = "encryptedValue";
        String decryptedAccountNumber = "123412341234";

        given(accountVerificationRepository.findBySellerId(sellerId))
            .willReturn(Optional.of(accountVerification));
        given(accountVerification.getAccountNumber()).willReturn(encryptedAccountNumber);
        given(aesEncryptionUtil.decrypt(encryptedAccountNumber)).willReturn(decryptedAccountNumber);
        given(accountVerification.getId()).willReturn(1L);
        given(accountVerification.getSeller()).willReturn(seller);
        given(seller.getId()).willReturn(sellerId);
        given(accountVerification.getBankCode()).willReturn("92");
        given(accountVerification.getAccountHolder()).willReturn("홍길동");
        given(accountVerification.isVerified()).willReturn(true);
        given(accountVerification.getCreatedAt()).willReturn(null);

        // act
        AccountVerificationDetailInfo result = accountVerificationService.getAccountVerification(sellerId);

        // assert
        assertThat(result).isNotNull();
        assertThat(result.sellerId()).isEqualTo(sellerId);
        assertThat(result.accountNumber()).isEqualTo(decryptedAccountNumber);
        assertThat(result.bankCode()).isEqualTo("92");
        assertThat(result.accountHolder()).isEqualTo("홍길동");
        assertThat(result.verified()).isTrue();

        verify(accountVerificationRepository).findBySellerId(sellerId);
        verify(aesEncryptionUtil).decrypt(encryptedAccountNumber);
    }

    @Test
    @DisplayName("계좌 인증 이력이 없는 경우 조회 시 예외가 발생한다")
    void fail_get_account_verification_not_found() {
        // arrange
        Long sellerId = 999L;

        given(accountVerificationRepository.findBySellerId(sellerId))
            .willReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() -> accountVerificationService.getAccountVerification(sellerId))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.ACCOUNT_VERIFICATION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("계좌 인증 요청이 성공한다")
    void success_verify_account() {
        // arrange
        Long sellerId = 1L;
        String bankCode = "92";
        String accountNumber = "123412341234";
        String encryptedAccountNumber = "encryptedValue";
        String accountHolder = "홍길동";

        VerifyAccountCommand command = new VerifyAccountCommand(sellerId, bankCode, accountNumber);

        given(sellerRepository.findById(sellerId)).willReturn(Optional.of(seller));
        given(accountVerificationClient.verifyAccount(bankCode, accountNumber)).willReturn(accountHolder);
        given(aesEncryptionUtil.encrypt(accountNumber)).willReturn(encryptedAccountNumber);
        given(accountVerificationRepository.save(any(AccountVerification.class)))
            .willReturn(accountVerification);
        given(accountVerification.getId()).willReturn(1L);
        given(accountVerification.getSeller()).willReturn(seller);
        given(accountVerification.isVerified()).willReturn(true);
        given(accountVerification.getCreatedAt()).willReturn(null);
        given(seller.getId()).willReturn(sellerId);

        // act
        AccountVerificationInfo result = accountVerificationService.verifyAccount(command);

        // assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.sellerId()).isEqualTo(sellerId);
        assertThat(result.verified()).isTrue();

        verify(sellerRepository).findById(sellerId);
        verify(accountVerificationClient).verifyAccount(bankCode, accountNumber);
        verify(aesEncryptionUtil).encrypt(accountNumber);
        verify(accountVerificationRepository).save(any(AccountVerification.class));
    }

    @Test
    @DisplayName("토스 API 인증 실패 시 저장하지 않고 예외가 발생한다")
    void fail_verify_account_when_verification_fails() {
        // arrange
        Long sellerId = 1L;
        VerifyAccountCommand command = new VerifyAccountCommand(sellerId, "92", "123412341234");

        given(sellerRepository.findById(sellerId)).willReturn(Optional.of(seller));
        given(accountVerificationRepository.findBySellerId(sellerId)).willReturn(Optional.empty());
        given(accountVerificationClient.verifyAccount("92", "123412341234")).willReturn(null);

        // act & assert
        assertThatThrownBy(() -> accountVerificationService.verifyAccount(command))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.ACCOUNT_VERIFICATION_FAILED.getMessage());

        verify(accountVerificationRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    @DisplayName("이미 계좌 인증 정보가 존재하면 계좌 인증이 실패한다")
    void fail_verify_account_already_exists() {
        // arrange
        Long sellerId = 1L;
        VerifyAccountCommand command = new VerifyAccountCommand(sellerId, "92", "123412341234");

        given(sellerRepository.findById(sellerId)).willReturn(Optional.of(seller));
        given(accountVerificationRepository.findBySellerId(sellerId))
            .willReturn(Optional.of(accountVerification));

        // act & assert
        assertThatThrownBy(() -> accountVerificationService.verifyAccount(command))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.ACCOUNT_VERIFICATION_ALREADY_EXISTS.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 판매자로 인해 계좌 인증이 실패한다")
    void fail_verify_account_with_non_existent_seller() {
        // arrange
        Long sellerId = 999L;
        VerifyAccountCommand command = new VerifyAccountCommand(sellerId, "92", "123412341234");

        given(sellerRepository.findById(sellerId)).willReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() -> accountVerificationService.verifyAccount(command))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.SELLER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("인증된 계좌 확인에 성공한다")
    void success_confirm_verified_account() {
        // arrange
        Long sellerId = 1L;

        given(accountVerificationRepository.findBySellerId(sellerId))
            .willReturn(Optional.of(accountVerification));
        given(accountVerification.isVerified()).willReturn(true);

        // act & assert
        accountVerificationService.confirmAccount(sellerId);

        verify(accountVerificationRepository).findBySellerId(sellerId);
    }

    @Test
    @DisplayName("인증되지 않은 계좌 확인 시 예외가 발생한다")
    void fail_confirm_unverified_account() {
        // arrange
        Long sellerId = 1L;

        given(accountVerificationRepository.findBySellerId(sellerId))
            .willReturn(Optional.of(accountVerification));
        given(accountVerification.isVerified()).willReturn(false);

        // act & assert
        assertThatThrownBy(() -> accountVerificationService.confirmAccount(sellerId))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.ACCOUNT_NOT_VERIFIED.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 판매자 ID로 계좌 확인 시 예외가 발생한다")
    void fail_confirm_with_non_existent_seller_id() {
        // arrange
        Long sellerId = 999L;

        given(accountVerificationRepository.findBySellerId(sellerId))
            .willReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() -> accountVerificationService.confirmAccount(sellerId))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.ACCOUNT_VERIFICATION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("계좌 수정에 성공한다")
    void success_update_account() {
        // arrange
        Long sellerId = 1L;
        String bankCode = "20";
        String accountNumber = "987654321";
        String encryptedAccountNumber = "newEncryptedValue";
        String accountHolder = "김철수";

        UpdateAccountCommand command = new UpdateAccountCommand(sellerId, bankCode, accountNumber);

        given(accountVerificationRepository.findBySellerId(sellerId))
            .willReturn(Optional.of(accountVerification));
        given(accountVerificationClient.verifyAccount(bankCode, accountNumber)).willReturn(accountHolder);
        given(aesEncryptionUtil.encrypt(accountNumber)).willReturn(encryptedAccountNumber);

        // act
        accountVerificationService.updateAccount(command);

        // assert
        verify(accountVerificationRepository).findBySellerId(sellerId);
        verify(accountVerificationClient).verifyAccount(bankCode, accountNumber);
        verify(aesEncryptionUtil).encrypt(accountNumber);
        verify(accountVerification).update(bankCode, encryptedAccountNumber, accountHolder);
    }

    @Test
    @DisplayName("계좌 인증 이력이 없으면 계좌 수정에 실패한다")
    void fail_update_account_not_found() {
        // arrange
        Long sellerId = 999L;
        UpdateAccountCommand command = new UpdateAccountCommand(sellerId, "20", "987654321");

        given(accountVerificationRepository.findBySellerId(sellerId)).willReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() -> accountVerificationService.updateAccount(command))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.ACCOUNT_VERIFICATION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("Toss API 인증 실패 시 계좌 수정에 실패한다")
    void fail_update_account_verification_failed() {
        // arrange
        Long sellerId = 1L;
        UpdateAccountCommand command = new UpdateAccountCommand(sellerId, "20", "987654321");

        given(accountVerificationRepository.findBySellerId(sellerId))
            .willReturn(Optional.of(accountVerification));
        given(accountVerificationClient.verifyAccount("20", "987654321")).willReturn(null);

        // act & assert
        assertThatThrownBy(() -> accountVerificationService.updateAccount(command))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.ACCOUNT_VERIFICATION_FAILED.getMessage());

        verify(accountVerification, org.mockito.Mockito.never()).update(any(), any(), any());
    }
}
