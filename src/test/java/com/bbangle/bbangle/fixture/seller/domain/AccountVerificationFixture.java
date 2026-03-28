package com.bbangle.bbangle.fixture.seller.domain;

import com.bbangle.bbangle.seller.domain.AccountVerification;
import com.bbangle.bbangle.seller.domain.Seller;

public class AccountVerificationFixture {

    public static final String DEFAULT_BANK_CODE = "NH";

    private AccountVerificationFixture() {}

    private static AccountVerification baseBuilder(
        String bankCode,
        String encryptedAccountNumber,
        boolean verified,
        Seller seller
    ) {
        return AccountVerification.create(bankCode, encryptedAccountNumber, seller.getName(), verified, seller);
    }

    public static AccountVerification defaultAccountVerification(Seller seller, String accountNumber, boolean verified) {
        return baseBuilder(DEFAULT_BANK_CODE, accountNumber, verified, seller);
    }
}
