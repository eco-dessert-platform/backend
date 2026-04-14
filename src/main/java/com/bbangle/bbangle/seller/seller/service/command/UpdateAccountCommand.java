package com.bbangle.bbangle.seller.seller.service.command;

public record UpdateAccountCommand(
    Long sellerId,
    String bankCode,
    String accountNumber
) {

}
