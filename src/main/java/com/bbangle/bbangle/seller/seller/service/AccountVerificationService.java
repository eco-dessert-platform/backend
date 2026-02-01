package com.bbangle.bbangle.seller.seller.service;

import static com.bbangle.bbangle.exception.BbangleErrorCode.ACCOUNT_NOT_VERIFIED;
import static com.bbangle.bbangle.exception.BbangleErrorCode.ACCOUNT_VERIFICATION_NOT_FOUND;
import static com.bbangle.bbangle.exception.BbangleErrorCode.SELLER_NOT_FOUND;

import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.domain.AccountVerification;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.AccountVerificationRepository;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.seller.seller.service.client.AccountVerificationClient;
import com.bbangle.bbangle.seller.seller.service.command.VerifyAccountCommand;
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

    /**
     * Confirms that the account verification identified by the given id is marked as verified.
     *
     * @param accountVerificationId the id of the AccountVerification to confirm
     * @throws BbangleException with {@code ACCOUNT_VERIFICATION_NOT_FOUND} if no record exists for the given id
     * @throws BbangleException with {@code ACCOUNT_NOT_VERIFIED} if the found AccountVerification is not verified
     */
    public void confirmAccount(Long accountVerificationId) {
        AccountVerification accountVerification = accountVerificationRepository.findById(accountVerificationId)
            .orElseThrow(() -> new BbangleException(ACCOUNT_VERIFICATION_NOT_FOUND));

        if (!accountVerification.isVerified()) {
            throw new BbangleException(ACCOUNT_NOT_VERIFIED);
        }
    }

    /**
     * Verifies a seller's bank account using the provided command and records the result.
     *
     * @param command contains the seller ID, bank code, and account number used to perform verification
     * @return an AccountVerificationInfo representing the persisted account verification
     */
    @Transactional
    public AccountVerificationInfo verifyAccount(VerifyAccountCommand command) {
        Seller seller = sellerRepository.findById(command.sellerId())
            .orElseThrow(() -> new BbangleException(SELLER_NOT_FOUND));

        String accountHolder = accountVerificationClient.verifyAccount(
            command.bankCode(),
            command.accountNumber()
        );

        boolean verified = accountHolder != null && !accountHolder.isEmpty();
        String encryptedAccountNumber = aesEncryptionUtil.encrypt(command.accountNumber());

        AccountVerification accountVerification = AccountVerification.create(
            command.bankCode(),
            encryptedAccountNumber,
            accountHolder,
            verified,
            seller
        );

        accountVerificationRepository.save(accountVerification);

        return AccountVerificationInfo.from(accountVerification);
    }
}