package com.bbangle.bbangle.seller.admin.service.model;

import com.bbangle.bbangle.seller.domain.model.DocumentType;

public record SellerDocumentDownloadInfo(
    String storeName,
    String url,
    DocumentType documentType,
    String originalFileName
) {

    public String zipFileName() {
        String extension = originalFileName.substring(originalFileName.lastIndexOf('.'));
        return documentType.getKoreanName() + extension;
    }

    public String zipEntryPath() {
        return storeName + "/" + zipFileName();
    }
}
