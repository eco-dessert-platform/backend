package com.bbangle.bbangle.seller.admin.service;

import static com.bbangle.bbangle.exception.BbangleErrorCode.SELLER_DOCUMENT_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.admin.service.model.SellerDocumentDownloadInfo;
import com.bbangle.bbangle.seller.domain.model.DocumentType;
import com.bbangle.bbangle.seller.repository.SellerDocumentRepository;
import java.io.ByteArrayOutputStream;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("[단위 테스트] AdminSellerDocumentService")
@ExtendWith(MockitoExtension.class)
class AdminSellerDocumentServiceUnitTest {

    @InjectMocks
    private AdminSellerDocumentService adminSellerDocumentService;

    @Mock
    private SellerDocumentRepository sellerDocumentRepository;

    @Nested
    @DisplayName("downloadSellerDocuments() 테스트")
    class DownloadSellerDocumentsTest {

        @Test
        @DisplayName("서류가 존재하지 않으면 예외가 발생한다")
        void fail_when_no_documents_found() {
            // given
            List<Long> sellerIds = List.of(1L, 2L);
            given(sellerDocumentRepository.findDocumentsBySellerIds(sellerIds))
                .willReturn(List.of());

            // when & then
            assertThatThrownBy(() ->
                adminSellerDocumentService.downloadSellerDocuments(sellerIds, new ByteArrayOutputStream())
            )
                .isInstanceOf(BbangleException.class)
                .hasMessageContaining(SELLER_DOCUMENT_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("sellerIds가 빈 리스트면 예외가 발생한다")
        void fail_when_seller_ids_empty() {
            // given
            List<Long> sellerIds = List.of();
            given(sellerDocumentRepository.findDocumentsBySellerIds(sellerIds))
                .willReturn(List.of());

            // when & then
            assertThatThrownBy(() ->
                adminSellerDocumentService.downloadSellerDocuments(sellerIds, new ByteArrayOutputStream())
            )
                .isInstanceOf(BbangleException.class)
                .hasMessageContaining(SELLER_DOCUMENT_NOT_FOUND.getMessage());
        }
    }
}
