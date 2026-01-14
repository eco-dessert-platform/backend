package com.bbangle.bbangle.seller.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.seller.domain.model.DocumentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("[단위 테스트] SellerDocument")
@ExtendWith(MockitoExtension.class)
public class SellerDocumentTest {

    @Mock
    private Seller seller;

    @Test
    @DisplayName("판매자 서류 생성에 성공한다")
    void success_create_seller_document() {
        // arrange
        String name = "business_registration.pdf";
        String url = "https://s3.amazonaws.com/bbangle/documents/business_registration.pdf";
        String type = "BUSINESS_REGISTRATION_CERTIFICATE";

        // act
        SellerDocument document = SellerDocument.create(name, url, type, seller);

        // assert
        assertThat(document).isNotNull();
        assertThat(document.getName()).isEqualTo(name);
        assertThat(document.getUrl()).isEqualTo(url);
        assertThat(document.getType()).isEqualTo(DocumentType.BUSINESS_REGISTRATION_CERTIFICATE);
        assertThat(document.getSeller()).isEqualTo(seller);
        assertThat(document.getStatus()).isEqualTo(CertificationStatus.PENDING);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "BUSINESS_REGISTRATION_CERTIFICATE",
        "MAIL_ORDER_SALES_REPORT",
        "INSTANT_FOOD_MANUFACTURING_PROCESSING_REGISTRATION",
        "BANKBOOK_COPY"
    })
    @DisplayName("모든 서류 타입으로 판매자 서류 생성에 성공한다")
    void success_create_seller_document_with_all_document_types(String documentType) {
        // arrange
        String name = "document.pdf";
        String url = "https://s3.amazonaws.com/bbangle/documents/document.pdf";

        // act
        SellerDocument document = SellerDocument.create(name, url, documentType, seller);

        // assert
        assertThat(document).isNotNull();
        assertThat(document.getType()).isEqualTo(DocumentType.valueOf(documentType));
        assertThat(document.getStatus()).isEqualTo(CertificationStatus.PENDING);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("판매자 서류 생성 시 서류 파일명이 null 또는 빈 문자열이면 실패한다")
    void fail_create_seller_document_with_null_or_empty_name(String invalidName) {
        // arrange
        String url = "https://s3.amazonaws.com/bbangle/documents/business_registration.pdf";
        String type = "BUSINESS_REGISTRATION_CERTIFICATE";

        // act & assert
        assertThatThrownBy(() -> SellerDocument.create(invalidName, url, type, seller))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.SELLER_DOCUMENT_NAME_REQUIRED.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    @DisplayName("판매자 서류 생성 시 서류 파일명이 공백 문자열이면 실패한다")
    void fail_create_seller_document_with_whitespace_name(String whitespaceName) {
        // arrange
        String url = "https://s3.amazonaws.com/bbangle/documents/business_registration.pdf";
        String type = "BUSINESS_REGISTRATION_CERTIFICATE";

        // act & assert
        assertThatThrownBy(() -> SellerDocument.create(whitespaceName, url, type, seller))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.SELLER_DOCUMENT_NAME_REQUIRED.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\t"})
    @DisplayName("판매자 서류 생성 시 URL이 null, 빈 문자열 또는 공백이면 실패한다")
    void fail_create_seller_document_with_invalid_url(String invalidUrl) {
        // arrange
        String name = "business_registration.pdf";
        String type = "BUSINESS_REGISTRATION_CERTIFICATE";

        // act & assert
        assertThatThrownBy(() -> SellerDocument.create(name, invalidUrl, type, seller))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.SELLER_DOCUMENT_URL_REQUIRED.getMessage());
    }

    @Test
    @DisplayName("판매자 서류 생성 시 서류 타입이 null이면 실패한다")
    void fail_create_seller_document_with_null_type() {
        // arrange
        String name = "business_registration.pdf";
        String url = "https://s3.amazonaws.com/bbangle/documents/business_registration.pdf";

        // act & assert
        assertThatThrownBy(() -> SellerDocument.create(name, url, null, seller))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.SELLER_DOCUMENT_TYPE_REQUIRED.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"INVALID_TYPE", "UNKNOWN", "TEST", ""})
    @DisplayName("판매자 서류 생성 시 유효하지 않은 서류 타입이면 실패한다")
    void fail_create_seller_document_with_invalid_type(String invalidType) {
        // arrange
        String name = "business_registration.pdf";
        String url = "https://s3.amazonaws.com/bbangle/documents/business_registration.pdf";

        // act & assert
        assertThatThrownBy(() -> SellerDocument.create(name, url, invalidType, seller))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("판매자 서류 생성 시 판매자가 null이면 실패한다")
    void fail_create_seller_document_with_null_seller() {
        // arrange
        String name = "business_registration.pdf";
        String url = "https://s3.amazonaws.com/bbangle/documents/business_registration.pdf";
        String type = "BUSINESS_REGISTRATION_CERTIFICATE";

        // act & assert
        assertThatThrownBy(() -> SellerDocument.create(name, url, type, null))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.SELLER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("생성된 판매자 서류의 초기 상태는 PENDING이다")
    void seller_document_initial_status_is_pending() {
        // arrange
        String name = "business_registration.pdf";
        String url = "https://s3.amazonaws.com/bbangle/documents/business_registration.pdf";
        String type = "BUSINESS_REGISTRATION_CERTIFICATE";

        // act
        SellerDocument document = SellerDocument.create(name, url, type, seller);

        // assert
        assertThat(document.getStatus()).isEqualTo(CertificationStatus.PENDING);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "business_registration.pdf",
        "사업자등록증.pdf",
        "document_with_underscore.jpg",
        "document-with-dash.png",
        "123456789.pdf"
    })
    @DisplayName("다양한 형식의 파일명으로 판매자 서류 생성에 성공한다")
    void success_create_seller_document_with_various_file_names(String fileName) {
        // arrange
        String url = "https://s3.amazonaws.com/bbangle/documents/" + fileName;
        String type = "BUSINESS_REGISTRATION_CERTIFICATE";

        // act
        SellerDocument document = SellerDocument.create(fileName, url, type, seller);

        // assert
        assertThat(document).isNotNull();
        assertThat(document.getName()).isEqualTo(fileName);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "document.jpg",
        "document.jpeg",
        "document.png",
        "document.pdf",
        "document.JPG",
        "document.JPEG",
        "document.PNG",
        "document.PDF"
    })
    @DisplayName("허용된 확장자(jpg, jpeg, png, pdf)로 판매자 서류 생성에 성공한다")
    void success_create_seller_document_with_allowed_extensions(String fileName) {
        // arrange
        String url = "https://s3.amazonaws.com/bbangle/documents/" + fileName;
        String type = "BUSINESS_REGISTRATION_CERTIFICATE";

        // act
        SellerDocument document = SellerDocument.create(fileName, url, type, seller);

        // assert
        assertThat(document).isNotNull();
        assertThat(document.getName()).isEqualTo(fileName);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "document.txt",
        "document.doc",
        "document.docx",
        "document.xls",
        "document.xlsx",
        "document.zip",
        "document.exe",
        "document.hwp"
    })
    @DisplayName("허용되지 않은 확장자로 판매자 서류 생성 시 실패한다")
    void fail_create_seller_document_with_not_allowed_extensions(String fileName) {
        // arrange
        String url = "https://s3.amazonaws.com/bbangle/documents/" + fileName;
        String type = "BUSINESS_REGISTRATION_CERTIFICATE";

        // act & assert
        assertThatThrownBy(() -> SellerDocument.create(fileName, url, type, seller))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.INVALID_DOCUMENT_FILE_EXTENSION.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "document",
        "document.",
        "no_extension_file",
        "."
    })
    @DisplayName("확장자가 없거나 잘못된 형식의 파일명으로 판매자 서류 생성 시 실패한다")
    void fail_create_seller_document_with_no_extension(String fileName) {
        // arrange
        String url = "https://s3.amazonaws.com/bbangle/documents/" + fileName;
        String type = "BUSINESS_REGISTRATION_CERTIFICATE";

        // act & assert
        assertThatThrownBy(() -> SellerDocument.create(fileName, url, type, seller))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.INVALID_DOCUMENT_FILE_EXTENSION.getMessage());
    }

}
