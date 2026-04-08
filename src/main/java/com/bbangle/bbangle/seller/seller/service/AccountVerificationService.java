package com.bbangle.bbangle.seller.seller.service;

import static com.bbangle.bbangle.exception.BbangleErrorCode.ACCOUNT_NOT_VERIFIED;
import static com.bbangle.bbangle.exception.BbangleErrorCode.ACCOUNT_VERIFICATION_ALREADY_EXISTS;
import static com.bbangle.bbangle.exception.BbangleErrorCode.ACCOUNT_VERIFICATION_FAILED;
import static com.bbangle.bbangle.exception.BbangleErrorCode.ACCOUNT_VERIFICATION_NOT_FOUND;
import static com.bbangle.bbangle.exception.BbangleErrorCode.SELLER_NOT_FOUND;

import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.domain.AccountVerification;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.AccountVerificationRepository;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.seller.seller.service.client.AccountVerificationClient;
import com.bbangle.bbangle.seller.seller.service.command.VerifyAccountCommand;
import com.bbangle.bbangle.seller.seller.service.info.AccountVerificationDetailInfo;
import com.bbangle.bbangle.seller.seller.service.info.AccountVerificationInfo;
import com.bbangle.bbangle.util.AesEncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountVerificationService {

    private final AccountVerificationRepository accountVerificationRepository;
    private final SellerRepository sellerRepository;
    private final AccountVerificationClient accountVerificationClient;
    private final AesEncryptionUtil aesEncryptionUtil;

    public AccountVerificationDetailInfo getAccountVerification(Long sellerId) {
        AccountVerification accountVerification = accountVerificationRepository.findBySellerId(sellerId)
            .orElseThrow(() -> new BbangleException(ACCOUNT_VERIFICATION_NOT_FOUND));
        String decryptedAccountNumber = aesEncryptionUtil.decrypt(accountVerification.getAccountNumber());
        return AccountVerificationDetailInfo.of(accountVerification, decryptedAccountNumber);
    }

    public void confirmAccount(Long sellerId) {
        AccountVerification accountVerification = accountVerificationRepository.findBySellerId(sellerId)
            .orElseThrow(() -> new BbangleException(ACCOUNT_VERIFICATION_NOT_FOUND));

        if (!accountVerification.isVerified()) {
            throw new BbangleException(ACCOUNT_NOT_VERIFIED);
        }
    }

    @Transactional
    public AccountVerificationInfo verifyAccount(VerifyAccountCommand command) {
        Seller seller = sellerRepository.findById(command.sellerId())
            .orElseThrow(() -> new BbangleException(SELLER_NOT_FOUND));

        if (accountVerificationRepository.findBySellerId(command.sellerId()).isPresent()) {
            throw new BbangleException(ACCOUNT_VERIFICATION_ALREADY_EXISTS);
        }

        String accountHolder = accountVerificationClient.verifyAccount(
            command.bankCode(),
            command.accountNumber()
        );

        if (accountHolder == null || accountHolder.isEmpty()) {
            throw new BbangleException(ACCOUNT_VERIFICATION_FAILED);
        }

        String encryptedAccountNumber = aesEncryptionUtil.encrypt(command.accountNumber());

        AccountVerification accountVerification = AccountVerification.create(
            command.bankCode(),
            encryptedAccountNumber,
            accountHolder,
            true,
            seller
        );

        return AccountVerificationInfo.from(accountVerificationRepository.save(accountVerification));
    }
}
