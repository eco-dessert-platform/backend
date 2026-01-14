package com.bbangle.bbangle.seller.seller.service.command;

public record RegisterDocumentCommand(
    Long sellerId,
    String url,
    String name,
    String type
) {
    public RegisterDocumentCommand(Long sellerId, String url, String name, String type) {
        this.sellerId = sellerId;
        this.url = url;
        this.name = name;
        this.type = type;
    }
}
