package com.bbangle.bbangle.seller.seller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.SellerDocument;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.seller.repository.SellerDocumentRepository;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.seller.seller.service.command.RegisterDocumentCommand;
import com.bbangle.bbangle.seller.seller.service.info.SellerDocumentInfo;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] SellerDocumentServiceIntegrationTest")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SellerDocumentServiceIntegrationTest {

    @Autowired
    private SellerDocumentService sellerDocumentService;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private SellerDocumentRepository sellerDocumentRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EntityManager em;

    private Seller testSeller;

    @BeforeEach
    void setUp() {
        // arrange: 테스트용 스토어와 판매자 생성
        Store store = StoreFixture.defaultStore();
        storeRepository.save(store);

        testSeller = SellerFixture.defaultSeller();
        testSeller.registerStore(store);
        sellerRepository.save(testSeller);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("판매자 서류 등록에 성공한다")
    void success_register_seller_document() {
        // arrange
        RegisterDocumentCommand command = new RegisterDocumentCommand(
            testSeller.getId(),
            "https://s3.amazonaws.com/bbangle/documents/business_registration.pdf",
            "business_registration.pdf",
            "BUSINESS_REGISTRATION_CERTIFICATE"
        );

        // act
        SellerDocumentInfo result = sellerDocumentService.registerDocument(command);
        em.flush();
        em.clear();

        // assert
        assertThat(result).isNotNull();
        assertThat(result.sellerDocumentId()).isNotNull();
        assertThat(result.sellerId()).isEqualTo(testSeller.getId());
        assertThat(result.url()).isEqualTo(command.url());
        assertThat(result.documentName()).isEqualTo(command.name());
        assertThat(result.documentType()).isEqualTo(command.type());
        assertThat(result.status()).isEqualTo(CertificationStatus.PENDING.name());
        assertThat(result.createdAt()).isNotNull();

        // DB 검증
        SellerDocument savedDocument = sellerDocumentRepository.findById(result.sellerDocumentId())
            .orElseThrow();
        assertThat(savedDocument.getName()).isEqualTo(command.name());
        assertThat(savedDocument.getUrl()).isEqualTo(command.url());
        assertThat(savedDocument.getType().name()).isEqualTo(command.type());
        assertThat(savedDocument.getSeller().getId()).isEqualTo(testSeller.getId());
        assertThat(savedDocument.getStatus()).isEqualTo(CertificationStatus.PENDING);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "BUSINESS_REGISTRATION_CERTIFICATE",
        "MAIL_ORDER_SALES_REPORT",
        "INSTANT_FOOD_MANUFACTURING_PROCESSING_REGISTRATION",
        "BANKBOOK_COPY"
    })
    @DisplayName("모든 서류 타입으로 판매자 서류 등록에 성공한다")
    void success_register_seller_document_with_all_document_types(String documentType) {
        // arrange
        RegisterDocumentCommand command = new RegisterDocumentCommand(
            testSeller.getId(),
            "https://s3.amazonaws.com/bbangle/documents/document.pdf",
            "document.pdf",
            documentType
        );

        // act
        SellerDocumentInfo result = sellerDocumentService.registerDocument(command);
        em.flush();
        em.clear();

        // assert
        assertThat(result).isNotNull();
        assertThat(result.documentType()).isEqualTo(documentType);

        // DB 검증
        SellerDocument savedDocument = sellerDocumentRepository.findById(result.sellerDocumentId())
            .orElseThrow();
        assertThat(savedDocument.getType().name()).isEqualTo(documentType);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "document.jpg",
        "document.jpeg",
        "document.png",
        "document.pdf"
    })
    @DisplayName("허용된 파일 확장자로 판매자 서류 등록에 성공한다")
    void success_register_seller_document_with_allowed_extensions(String fileName) {
        // arrange
        RegisterDocumentCommand command = new RegisterDocumentCommand(
            testSeller.getId(),
            "https://s3.amazonaws.com/bbangle/documents/" + fileName,
            fileName,
            "BUSINESS_REGISTRATION_CERTIFICATE"
        );

        // act
        SellerDocumentInfo result = sellerDocumentService.registerDocument(command);
        em.flush();
        em.clear();

        // assert
        assertThat(result).isNotNull();
        assertThat(result.documentName()).isEqualTo(fileName);

        // DB 검증
        SellerDocument savedDocument = sellerDocumentRepository.findById(result.sellerDocumentId())
            .orElseThrow();
        assertThat(savedDocument.getName()).isEqualTo(fileName);
    }

    @Test
    @DisplayName("존재하지 않는 판매자 ID로 서류 등록 시 실패한다")
    void fail_register_document_with_non_existent_seller_id() {
        // arrange
        Long nonExistentSellerId = 99999L;
        RegisterDocumentCommand command = new RegisterDocumentCommand(
            nonExistentSellerId,
            "https://s3.amazonaws.com/bbangle/documents/business_registration.pdf",
            "business_registration.pdf",
            "BUSINESS_REGISTRATION_CERTIFICATE"
        );

        // act & assert
        assertThatThrownBy(() -> sellerDocumentService.registerDocument(command))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.SELLER_NOT_FOUND.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("파일명이 null 또는 빈 문자열인 경우 서류 등록에 실패한다")
    void fail_register_document_with_null_or_empty_name(String invalidName) {
        // arrange
        RegisterDocumentCommand command = new RegisterDocumentCommand(
            testSeller.getId(),
            "https://s3.amazonaws.com/bbangle/documents/business_registration.pdf",
            invalidName,
            "BUSINESS_REGISTRATION_CERTIFICATE"
        );

        // act & assert
        assertThatThrownBy(() -> sellerDocumentService.registerDocument(command))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.SELLER_DOCUMENT_NAME_REQUIRED.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    @DisplayName("파일명이 공백 문자열인 경우 서류 등록에 실패한다")
    void fail_register_document_with_whitespace_name(String whitespaceName) {
        // arrange
        RegisterDocumentCommand command = new RegisterDocumentCommand(
            testSeller.getId(),
            "https://s3.amazonaws.com/bbangle/documents/business_registration.pdf",
            whitespaceName,
            "BUSINESS_REGISTRATION_CERTIFICATE"
        );

        // act & assert
        assertThatThrownBy(() -> sellerDocumentService.registerDocument(command))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.SELLER_DOCUMENT_NAME_REQUIRED.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\t"})
    @DisplayName("URL이 null, 빈 문자열 또는 공백인 경우 서류 등록에 실패한다")
    void fail_register_document_with_invalid_url(String invalidUrl) {
        // arrange
        RegisterDocumentCommand command = new RegisterDocumentCommand(
            testSeller.getId(),
            invalidUrl,
            "business_registration.pdf",
            "BUSINESS_REGISTRATION_CERTIFICATE"
        );

        // act & assert
        assertThatThrownBy(() -> sellerDocumentService.registerDocument(command))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.SELLER_DOCUMENT_URL_REQUIRED.getMessage());
    }

    @Test
    @DisplayName("서류 타입이 null인 경우 서류 등록에 실패한다")
    void fail_register_document_with_null_type() {
        // arrange
        RegisterDocumentCommand command = new RegisterDocumentCommand(
            testSeller.getId(),
            "https://s3.amazonaws.com/bbangle/documents/business_registration.pdf",
            "business_registration.pdf",
            null
        );

        // act & assert
        assertThatThrownBy(() -> sellerDocumentService.registerDocument(command))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.SELLER_DOCUMENT_TYPE_REQUIRED.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"INVALID_TYPE", "UNKNOWN", "TEST", ""})
    @DisplayName("유효하지 않은 서류 타입인 경우 서류 등록에 실패한다")
    void fail_register_document_with_invalid_type(String invalidType) {
        // arrange
        RegisterDocumentCommand command = new RegisterDocumentCommand(
            testSeller.getId(),
            "https://s3.amazonaws.com/bbangle/documents/business_registration.pdf",
            "business_registration.pdf",
            invalidType
        );

        // act & assert
        assertThatThrownBy(() -> sellerDocumentService.registerDocument(command))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "document.txt",
        "document.doc",
        "document.docx",
        "document.exe",
        "document.hwp"
    })
    @DisplayName("허용되지 않은 파일 확장자인 경우 서류 등록에 실패한다")
    void fail_register_document_with_not_allowed_extension(String invalidFileName) {
        // arrange
        RegisterDocumentCommand command = new RegisterDocumentCommand(
            testSeller.getId(),
            "https://s3.amazonaws.com/bbangle/documents/" + invalidFileName,
            invalidFileName,
            "BUSINESS_REGISTRATION_CERTIFICATE"
        );

        // act & assert
        assertThatThrownBy(() -> sellerDocumentService.registerDocument(command))
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
    @DisplayName("확장자가 없거나 잘못된 형식의 파일명인 경우 서류 등록에 실패한다")
    void fail_register_document_with_no_extension(String invalidFileName) {
        // arrange
        RegisterDocumentCommand command = new RegisterDocumentCommand(
            testSeller.getId(),
            "https://s3.amazonaws.com/bbangle/documents/" + invalidFileName,
            invalidFileName,
            "BUSINESS_REGISTRATION_CERTIFICATE"
        );

        // act & assert
        assertThatThrownBy(() -> sellerDocumentService.registerDocument(command))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.INVALID_DOCUMENT_FILE_EXTENSION.getMessage());
    }

    @Test
    @DisplayName("여러 서류를 순차적으로 등록할 수 있다")
    void success_register_multiple_documents() {
        // arrange
        RegisterDocumentCommand command1 = new RegisterDocumentCommand(
            testSeller.getId(),
            "https://s3.amazonaws.com/bbangle/documents/business_registration.pdf",
            "business_registration.pdf",
            "BUSINESS_REGISTRATION_CERTIFICATE"
        );

        RegisterDocumentCommand command2 = new RegisterDocumentCommand(
            testSeller.getId(),
            "https://s3.amazonaws.com/bbangle/documents/bankbook.jpg",
            "bankbook.jpg",
            "BANKBOOK_COPY"
        );

        // act
        SellerDocumentInfo result1 = sellerDocumentService.registerDocument(command1);
        SellerDocumentInfo result2 = sellerDocumentService.registerDocument(command2);
        em.flush();
        em.clear();

        // assert
        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result1.sellerDocumentId()).isNotEqualTo(result2.sellerDocumentId());

        // DB 검증
        assertThat(sellerDocumentRepository.findAll()).hasSize(2);
    }
}
