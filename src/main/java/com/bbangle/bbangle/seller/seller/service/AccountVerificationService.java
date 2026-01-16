package com.bbangle.bbangle.seller.seller.service;

import static com.bbangle.bbangle.exception.BbangleErrorCode.ACCOUNT_NOT_VERIFIED;
import static com.bbangle.bbangle.exception.BbangleErrorCode.ACCOUNT_VERIFICATION_NOT_FOUND;

import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.domain.AccountVerification;
import com.bbangle.bbangle.seller.repository.AccountVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountVerificationService {

    private final AccountVerificationRepository accountVerificationRepository;

    public void confirmAccount(Long accountVerificationId) {
        AccountVerification accountVerification = accountVerificationRepository.findById(accountVerificationId)
            .orElseThrow(() -> new BbangleException(ACCOUNT_VERIFICATION_NOT_FOUND));

        if (!accountVerification.isVerified()) {
            throw new BbangleException(ACCOUNT_NOT_VERIFIED);
        }
    }
}
