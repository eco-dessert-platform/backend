package com.bbangle.bbangle.claim.seller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.claim.domain.CancelRequest;
import com.bbangle.bbangle.claim.domain.constant.CancelRequestStatus;
import com.bbangle.bbangle.claim.domain.constant.DecisionType;
import com.bbangle.bbangle.claim.repository.CancelRequestRepository;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.board.domain.ProductFixture;
import com.bbangle.bbangle.fixture.claim.CancelRequestFixture;
import com.bbangle.bbangle.fixture.order.OrderItemFixture;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.repository.OrderItemHistoryRepository;
import com.bbangle.bbangle.order.repository.OrderItemRepository;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합 테스트] SellerCancelService")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SellerCancelServiceIntegrationTest {

    @Autowired
    private SellerCancelService sut;

    @Autowired
    private CancelRequestRepository cancelRequestRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderItemHistoryRepository orderItemHistoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private EntityManager em;

    private Store store;
    private Seller seller;
    private Product product;

    @BeforeEach
    void setUp() {
        store = storeRepository.save(StoreFixture.defaultStore());
        seller = sellerRepository.save(SellerFixture.defaultSeller(store));
        product = productRepository.save(ProductFixture.defaultProductWithStore(store));
    }

    @Nested
    @DisplayName("decision() - 승인(APPROVE) 시나리오")
    class ApproveTests {

        @Test
        @DisplayName("여러 취소를 승인하면 상태가 APPROVED로 변경되고 히스토리가 저장된다")
        void decision_approve_success() {
            // given
            List<CancelRequest> cancelRequests = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                OrderItem orderItem = orderItemRepository.save(
                    OrderItemFixture.cancelRequestedWithProduct(product)
                );
                CancelRequest cancelRequest = cancelRequestRepository.save(
                    CancelRequestFixture.requested(orderItem)
                );
                cancelRequests.add(cancelRequest);
            }

            List<Long> cancelIds = cancelRequests.stream()
                .map(CancelRequest::getId)
                .toList();

            em.flush();
            em.clear();

            String approveReason = "고객 요청 확인";

            // when
            sut.decision(cancelIds, seller.getId(), DecisionType.APPROVE, approveReason);

            // then
            em.flush();
            em.clear();

            List<CancelRequest> approvedCancels = cancelRequestRepository.findAllById(cancelIds);
            assertThat(approvedCancels)
                .hasSize(3)
                .allMatch(c -> c.getStatus() == CancelRequestStatus.APPROVED)
                .allMatch(c -> c.getSellerComment().equals(approveReason));

            long historyCount = orderItemHistoryRepository.count();
            assertThat(historyCount).isGreaterThanOrEqualTo(3);
        }
    }

    @Nested
    @DisplayName("decision() - 거절(REJECT) 시나리오")
    class RejectTests {

        @Test
        @DisplayName("여러 취소를 거절하면 상태가 REJECTED로 변경되고 히스토리가 저장된다")
        void decision_reject_success() {
            // given
            List<CancelRequest> cancelRequests = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                OrderItem orderItem = orderItemRepository.save(
                    OrderItemFixture.cancelRequestedWithProduct(product)
                );
                CancelRequest cancelRequest = cancelRequestRepository.save(
                    CancelRequestFixture.requested(orderItem)
                );
                cancelRequests.add(cancelRequest);
            }

            List<Long> cancelIds = cancelRequests.stream()
                .map(CancelRequest::getId)
                .toList();

            em.flush();
            em.clear();

            String rejectReason = "취소 불가 기간";

            // when
            sut.decision(cancelIds, seller.getId(), DecisionType.REJECT, rejectReason);

            // then
            em.flush();
            em.clear();

            List<CancelRequest> rejectedCancels = cancelRequestRepository.findAllById(cancelIds);
            assertThat(rejectedCancels)
                .hasSize(3)
                .allMatch(c -> c.getStatus() == CancelRequestStatus.REJECTED)
                .allMatch(c -> c.getSellerComment().equals(rejectReason));

            long historyCount = orderItemHistoryRepository.count();
            assertThat(historyCount).isGreaterThanOrEqualTo(3);
        }
    }

    @Nested
    @DisplayName("decision() - 권한 검증 시나리오")
    class AuthorizationTests {

        @Test
        @DisplayName("다른 판매자의 취소는 수정할 수 없다")
        void decision_fails_when_seller_different() {
            // given
            Store otherStore = storeRepository.save(StoreFixture.defaultStore());
            Seller otherSeller = sellerRepository.save(SellerFixture.defaultSeller(otherStore));

            List<CancelRequest> cancelRequests = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                OrderItem orderItem = orderItemRepository.save(
                    OrderItemFixture.cancelRequestedWithProduct(product)
                );
                CancelRequest cancelRequest = cancelRequestRepository.save(
                    CancelRequestFixture.requested(orderItem)
                );
                cancelRequests.add(cancelRequest);
            }

            List<Long> cancelIds = cancelRequests.stream()
                .map(CancelRequest::getId)
                .toList();

            em.flush();
            em.clear();

            // when & then
            assertThatThrownBy(
                () -> sut.decision(cancelIds, otherSeller.getId(), DecisionType.APPROVE, "사유")
            ).isInstanceOf(BbangleException.class);

            // 상태가 변경되지 않았는지 확인
            List<CancelRequest> unchangedCancels = cancelRequestRepository.findAllById(cancelIds);
            assertThat(unchangedCancels)
                .allMatch(c -> c.getStatus() == CancelRequestStatus.REQUESTED);
        }
    }

    @Nested
    @DisplayName("decision() - 부분 일치 시나리오")
    class PartialMatchTests {

        @Test
        @DisplayName("일부만 판매자 소유일 때 예외가 발생한다")
        void decision_fails_when_partial_ownership() {
            // given - 판매자A의 취소 3개, 판매자B의 취소 2개 생성
            Store otherStore = storeRepository.save(StoreFixture.defaultStore());
            Seller otherSeller = sellerRepository.save(SellerFixture.defaultSeller(otherStore));
            Product otherProduct = productRepository.save(
                ProductFixture.defaultProductWithStore(otherStore)
            );

            List<Long> allCancelIds = new ArrayList<>();

            // 판매자A의 취소 3개
            for (int i = 0; i < 3; i++) {
                OrderItem orderItem = orderItemRepository.save(
                    OrderItemFixture.cancelRequestedWithProduct(product)
                );
                CancelRequest cancelRequest = cancelRequestRepository.save(
                    CancelRequestFixture.requested(orderItem)
                );
                allCancelIds.add(cancelRequest.getId());
            }

            // 판매자B의 취소 2개
            for (int i = 0; i < 2; i++) {
                OrderItem orderItem = orderItemRepository.save(
                    OrderItemFixture.cancelRequestedWithProduct(otherProduct)
                );
                CancelRequest cancelRequest = cancelRequestRepository.save(
                    CancelRequestFixture.requested(orderItem)
                );
                allCancelIds.add(cancelRequest.getId());
            }

            em.flush();
            em.clear();

            // when & then - 판매자A로 5개 모두 처리하려 하면 실패
            assertThatThrownBy(
                () -> sut.decision(allCancelIds, seller.getId(), DecisionType.APPROVE, "사유")
            ).isInstanceOf(BbangleException.class);

            // 어떤 상태도 변경되지 않았는지 확인
            List<CancelRequest> unchangedCancels = cancelRequestRepository.findAllById(allCancelIds);
            assertThat(unchangedCancels)
                .allMatch(c -> c.getStatus() == CancelRequestStatus.REQUESTED);
        }
    }

    @Nested
    @DisplayName("decision() - 엣지 케이스")
    class EdgeCaseTests {

        @Test
        @DisplayName("존재하지 않는 ID로 요청하면 예외가 발생한다")
        void decision_fails_when_not_found() {
            // given
            List<Long> nonExistentIds = List.of(99999L, 99998L);

            // when & then
            assertThatThrownBy(
                () -> sut.decision(nonExistentIds, seller.getId(), DecisionType.APPROVE, "사유")
            ).isInstanceOf(BbangleException.class);
        }
    }
}
