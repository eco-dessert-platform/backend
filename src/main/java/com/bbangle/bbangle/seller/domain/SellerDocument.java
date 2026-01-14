package com.bbangle.bbangle.seller.domain;

import static com.bbangle.bbangle.exception.BbangleErrorCode.INVALID_DOCUMENT_FILE_EXTENSION;
import static com.bbangle.bbangle.exception.BbangleErrorCode.SELLER_DOCUMENT_NAME_REQUIRED;
import static com.bbangle.bbangle.exception.BbangleErrorCode.SELLER_DOCUMENT_TYPE_REQUIRED;
import static com.bbangle.bbangle.exception.BbangleErrorCode.SELLER_DOCUMENT_URL_REQUIRED;
import static com.bbangle.bbangle.exception.BbangleErrorCode.SELLER_NOT_FOUND;

import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.seller.domain.model.DocumentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Arrays;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "seller_documents")
@Entity
public class SellerDocument extends BaseEntity {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "pdf");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Seller seller;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", columnDefinition = "VARCHAR(30)")
    private DocumentType type;

    @Column(name = "url", columnDefinition = "VARCHAR(255)")
    private String url;

    @Column(name = "name", columnDefinition = "VARCHAR(60)")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "VARCHAR(30)")
    private CertificationStatus status;

    @Builder
    private SellerDocument(String name, String url, DocumentType type, Seller seller, CertificationStatus status) {
        this.name = name;
        this.url = url;
        this.type = type;
        this.seller = seller;
        this.status = status;
    }

    public static SellerDocument create(String name, String url, String type, Seller seller) {
        createValidate(name, url, type, seller);
        return SellerDocument.builder()
            .name(name)
            .url(url)
            .type(DocumentType.valueOf(type))
            .seller(seller)
            .status(CertificationStatus.PENDING)
            .build();
    }

    private static void createValidate(String name, String url, String type, Seller seller) {
        if (name == null || name.isBlank()) {
            throw new BbangleException(SELLER_DOCUMENT_NAME_REQUIRED);
        }
        validateFileExtension(name);
        if (url == null || url.isBlank()) {
            throw new BbangleException(SELLER_DOCUMENT_URL_REQUIRED);
        }
        if (type == null) {
            throw new BbangleException(SELLER_DOCUMENT_TYPE_REQUIRED);
        }
        if (seller == null) {
            throw new BbangleException(SELLER_NOT_FOUND);
        }
    }

    private static void validateFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            throw new BbangleException(INVALID_DOCUMENT_FILE_EXTENSION);
        }
        String extension = fileName.substring(lastDotIndex + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BbangleException(INVALID_DOCUMENT_FILE_EXTENSION);
        }
    }
}
