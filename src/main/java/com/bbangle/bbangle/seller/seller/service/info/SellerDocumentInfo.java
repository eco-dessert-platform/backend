package com.bbangle.bbangle.seller.seller.service.info;

import com.bbangle.bbangle.seller.domain.SellerDocument;
import java.time.LocalDateTime;
import lombok.Builder;

public record SellerDocumentInfo(
    Long sellerDocumentId,
    Long sellerId,
    String url,
    String documentName,
    String documentType,
    String status,
    LocalDateTime createdAt
) {

    @Builder
    public SellerDocumentInfo(Long sellerDocumentId, Long sellerId, String url, String documentName,
                              String documentType, String status,
                              LocalDateTime createdAt) {
        this.sellerDocumentId = sellerDocumentId;
        this.sellerId = sellerId;
        this.url = url;
        this.documentName = documentName;
        this.documentType = documentType;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static SellerDocumentInfo from(SellerDocument sellerDocument) {
        return SellerDocumentInfo.builder()
            .sellerDocumentId(sellerDocument.getId())
            .sellerId(sellerDocument.getSeller().getId())
            .url(sellerDocument.getUrl())
            .documentType(sellerDocument.getType().name())
            .documentName(sellerDocument.getName())
            .status(sellerDocument.getStatus().name())
            .createdAt(sellerDocument.getCreatedAt())
            .build();
    }
}
