package com.bbangle.bbangle.seller.admin.service;

import static com.bbangle.bbangle.exception.BbangleErrorCode.SELLER_DOCUMENT_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.SellerDocument;
import com.bbangle.bbangle.store.domain.Store;
import jakarta.persistence.EntityManager;
import java.io.ByteArrayOutputStream;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합 테스트] AdminSellerDocumentService")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminSellerDocumentServiceIntegrationTest {

    @Autowired
    private AdminSellerDocumentService adminSellerDocumentService;

    @Autowired
    private EntityManager em;

    private Store createStore(String name) {
        Store store = StoreFixture.defaultStore(name);
        em.persist(store);
        return store;
    }

    private Seller createSeller(Store store) {
        Seller seller = SellerFixture.defaultSeller(store);
        em.persist(seller);
        return seller;
    }

    private void createDocument(Seller seller, String type, String fileName, String url) {
        SellerDocument doc = SellerDocument.create(fileName, url, type, seller);
        em.persist(doc);
    }

    @Nested
    @DisplayName("downloadSellerDocuments() 테스트")
    class DownloadSellerDocumentsTest {

        @Test
        @DisplayName("서류가 없는 판매자 ID만 요청하면 예외가 발생한다")
        void fail_when_seller_has_no_documents() {
            // given
            Store store = createStore("빵그리의 오븐");
            Seller seller = createSeller(store);
            em.flush();
            em.clear();

            // when & then
            assertThatThrownBy(() ->
                adminSellerDocumentService.downloadSellerDocuments(
                    List.of(seller.getId()), new ByteArrayOutputStream()
                )
            )
                .isInstanceOf(BbangleException.class)
                .hasMessageContaining(SELLER_DOCUMENT_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("존재하지 않는 sellerId만 요청하면 예외가 발생한다")
        void fail_when_seller_not_exist() {
            // given
            List<Long> nonExistentIds = List.of(99999L);

            // when & then
            assertThatThrownBy(() ->
                adminSellerDocumentService.downloadSellerDocuments(
                    nonExistentIds, new ByteArrayOutputStream()
                )
            )
                .isInstanceOf(BbangleException.class)
                .hasMessageContaining(SELLER_DOCUMENT_NOT_FOUND.getMessage());
        }
    }
}
