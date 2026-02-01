package com.bbangle.bbangle.seller.seller.service.client;

public interface AccountVerificationClient {

    /**
 * Verify a bank account identified by a bank code and account number.
 *
 * @param bankCode      the bank's identifier code (e.g., routing or institution code)
 * @param accountNumber the bank account number to verify
 * @return              a string describing the verification result (for example: success, failure, or error details)
 */
String verifyAccount(String bankCode, String accountNumber);
}