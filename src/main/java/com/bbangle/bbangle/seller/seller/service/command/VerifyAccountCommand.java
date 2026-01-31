package com.bbangle.bbangle.seller.seller.service.command;

public record VerifyAccountCommand(
    Long sellerId,
    String bankCode,
    String accountNumber
) {

}
