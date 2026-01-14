package com.bbangle.bbangle.seller.seller.service.command;

public record RegisterDocumentCommand(
    Long sellerId,
    String url,
    String name,
    String type
) {
}
