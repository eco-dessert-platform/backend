package com.bbangle.bbangle.claim.seller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.claim.domain.ReturnRequest;
import com.bbangle.bbangle.claim.domain.constant.DecisionType;
import com.bbangle.bbangle.claim.domain.constant.ReturnRequestRequestStatus;
import com.bbangle.bbangle.claim.repository.ReturnRequestRepository;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.board.domain.ProductFixture;
import com.bbangle.bbangle.fixture.claim.ReturnRequestFixture;
import com.bbangle.bbangle.fixture.order.OrderItemFixture;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
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

@DisplayName("[통합 테스트] SellerReturnService")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SellerReturnServiceIntegrationTest {

    @Autowired
    private SellerReturnService sut;

    @Autowired
    private ReturnRequestRepository returnRequestRepository;

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
        @DisplayName("여러 반품을 승인하면 상태가 APPROVED로 변경되고 히스토리가 저장된다")
        void decision_approve_success() {
            // given
            List<ReturnRequest> returnRequests = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                OrderItem orderItem = orderItemRepository.save(
                    OrderItemFixture.returnRequestedWithProduct(product)
                );
                ReturnRequest returnRequest = returnRequestRepository.save(
                    ReturnRequestFixture.requested(orderItem)
                );
                returnRequests.add(returnRequest);
            }

            List<Long> returnIds = returnRequests.stream()
                .map(ReturnRequest::getId)
                .toList();

            em.flush();
            em.clear();

            String approveReason = "품질 우수";

            // when
            sut.decision(returnIds, seller.getId(), DecisionType.APPROVE, approveReason);

            // then
            em.flush();
            em.clear();

            List<ReturnRequest> approvedReturns = returnRequestRepository.findAllById(returnIds);
            assertThat(approvedReturns)
                .hasSize(3)
                .allMatch(r -> r.getStatus() == ReturnRequestRequestStatus.APPROVED)
                .allMatch(r -> r.getSellerComment().equals(approveReason));

            long historyCount = orderItemHistoryRepository.count();
            assertThat(historyCount).isGreaterThanOrEqualTo(3);
        }
    }

    @Nested
    @DisplayName("decision() - 거절(REJECT) 시나리오")
    class RejectTests {

        @Test
        @DisplayName("여러 반품을 거절하면 상태가 REJECTED로 변경되고 히스토리가 저장된다")
        void decision_reject_success() {
            // given
            List<ReturnRequest> returnRequests = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                OrderItem orderItem = orderItemRepository.save(
                    OrderItemFixture.returnRequestedWithProduct(product)
                );
                ReturnRequest returnRequest = returnRequestRepository.save(
                    ReturnRequestFixture.requested(orderItem)
                );
                returnRequests.add(returnRequest);
            }

            List<Long> returnIds = returnRequests.stream()
                .map(ReturnRequest::getId)
                .toList();

            em.flush();
            em.clear();

            String rejectReason = "반품 기간 만료";

            // when
            sut.decision(returnIds, seller.getId(), DecisionType.REJECT, rejectReason);

            // then
            em.flush();
            em.clear();

            List<ReturnRequest> rejectedReturns = returnRequestRepository.findAllById(returnIds);
            assertThat(rejectedReturns)
                .hasSize(3)
                .allMatch(r -> r.getStatus() == ReturnRequestRequestStatus.REJECTED)
                .allMatch(r -> r.getSellerComment().equals(rejectReason));

            long historyCount = orderItemHistoryRepository.count();
            assertThat(historyCount).isGreaterThanOrEqualTo(3);
        }
    }

    @Nested
    @DisplayName("decision() - 권한 검증 시나리오")
    class AuthorizationTests {

        @Test
        @DisplayName("다른 판매자의 반품은 수정할 수 없다")
        void decision_fails_when_seller_different() {
            // given
            Store otherStore = storeRepository.save(StoreFixture.defaultStore());
            Seller otherSeller = sellerRepository.save(SellerFixture.defaultSeller(otherStore));

            List<ReturnRequest> returnRequests = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                OrderItem orderItem = orderItemRepository.save(
                    OrderItemFixture.returnRequestedWithProduct(product)
                );
                ReturnRequest returnRequest = returnRequestRepository.save(
                    ReturnRequestFixture.requested(orderItem)
                );
                returnRequests.add(returnRequest);
            }

            List<Long> returnIds = returnRequests.stream()
                .map(ReturnRequest::getId)
                .toList();

            em.flush();
            em.clear();

            // when & then
            assertThatThrownBy(
                () -> sut.decision(returnIds, otherSeller.getId(), DecisionType.APPROVE, "사유")
            ).isInstanceOf(BbangleException.class);

            // 상태가 변경되지 않았는지 확인
            List<ReturnRequest> unchangedReturns = returnRequestRepository.findAllById(returnIds);
            assertThat(unchangedReturns)
                .allMatch(r -> r.getStatus() == ReturnRequestRequestStatus.REQUESTED);
        }
    }

    @Nested
    @DisplayName("decision() - 부분 일치 시나리오")
    class PartialMatchTests {

        @Test
        @DisplayName("일부만 판매자 소유일 때 예외가 발생한다")
        void decision_fails_when_partial_ownership() {
            // given - 판매자A의 반품 3개, 판매자B의 반품 2개 생성
            Store otherStore = storeRepository.save(StoreFixture.defaultStore());
            Seller otherSeller = sellerRepository.save(SellerFixture.defaultSeller(otherStore));
            Product otherProduct = productRepository.save(
                ProductFixture.defaultProductWithStore(otherStore)
            );

            List<Long> allReturnIds = new java.util.ArrayList<>();

            // 판매자A의 반품 3개
            for (int i = 0; i < 3; i++) {
                OrderItem orderItem = orderItemRepository.save(
                    OrderItemFixture.returnRequestedWithProduct(product)
                );
                ReturnRequest returnRequest = returnRequestRepository.save(
                    ReturnRequestFixture.requested(orderItem)
                );
                allReturnIds.add(returnRequest.getId());
            }

            // 판매자B의 반품 2개
            for (int i = 0; i < 2; i++) {
                OrderItem orderItem = orderItemRepository.save(
                    OrderItemFixture.returnRequestedWithProduct(otherProduct)
                );
                ReturnRequest returnRequest = returnRequestRepository.save(
                    ReturnRequestFixture.requested(orderItem)
                );
                allReturnIds.add(returnRequest.getId());
            }

            em.flush();
            em.clear();

            // when & then - 판매자A로 5개 모두 처리하려 하면 실패
            assertThatThrownBy(
                () -> sut.decision(allReturnIds, seller.getId(), DecisionType.APPROVE, "사유")
            ).isInstanceOf(BbangleException.class);

            // 어떤 상태도 변경되지 않았는지 확인
            List<ReturnRequest> unchangedReturns = returnRequestRepository.findAllById(allReturnIds);
            assertThat(unchangedReturns)
                .allMatch(r -> r.getStatus() == ReturnRequestRequestStatus.REQUESTED);
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

    @Nested
    @DisplayName("refuseReturn() - 성공 시나리오")
    class RefuseReturnSuccessTests {

        @Test
        @DisplayName("REQUESTED 상태의 반품을 거절하면 REJECTED로 변경되고 거절 사유와 히스토리가 저장된다")
        void refuseReturn_fromRequested_success() {
            // given
            long historyCountBefore = orderItemHistoryRepository.count();

            OrderItem orderItem = orderItemRepository.save(
                OrderItemFixture.returnRequestedWithProduct(product)
            );
            ReturnRequest returnRequest = returnRequestRepository.save(
                ReturnRequestFixture.requested(orderItem)
            );

            Long returnId = returnRequest.getId();
            Long orderItemId = orderItem.getId();
            String refusalReason = "사용 흔적이 있는 상품입니다.";

            em.flush();
            em.clear();

            // when
            sut.refuseReturn(returnId, seller.getId(), refusalReason);

            // then
            em.flush();
            em.clear();

            ReturnRequest refused = returnRequestRepository.findById(returnId).orElseThrow();
            assertThat(refused.getStatus()).isEqualTo(ReturnRequestRequestStatus.REJECTED);
            assertThat(refused.getSellerComment()).isEqualTo(refusalReason);

            OrderItem refusedItem = orderItemRepository.findById(orderItemId).orElseThrow();
            assertThat(refusedItem.getOrderStatus()).isEqualTo(OrderStatus.RETURN_REJECTED);

            assertThat(orderItemHistoryRepository.count()).isEqualTo(historyCountBefore + 1);
        }

        @Test
        @DisplayName("PICKUP_SCHEDULED 상태의 반품을 거절하면 REJECTED로 변경되고 히스토리가 저장된다")
        void refuseReturn_fromPickupScheduled_success() {
            // given
            long historyCountBefore = orderItemHistoryRepository.count();

            OrderItem orderItem = orderItemRepository.save(
                OrderItemFixture.returnApprovedWithProduct(product)
            );
            ReturnRequest returnRequest = returnRequestRepository.save(
                ReturnRequestFixture.pickupScheduled(orderItem)
            );

            Long returnId = returnRequest.getId();
            Long orderItemId = orderItem.getId();
            String refusalReason = "수거 중 파손이 확인되어 반품 불가합니다.";

            em.flush();
            em.clear();

            // when
            sut.refuseReturn(returnId, seller.getId(), refusalReason);

            // then
            em.flush();
            em.clear();

            ReturnRequest refused = returnRequestRepository.findById(returnId).orElseThrow();
            assertThat(refused.getStatus()).isEqualTo(ReturnRequestRequestStatus.REJECTED);
            assertThat(refused.getSellerComment()).isEqualTo(refusalReason);

            OrderItem refusedItem = orderItemRepository.findById(orderItemId).orElseThrow();
            assertThat(refusedItem.getOrderStatus()).isEqualTo(OrderStatus.RETURN_REJECTED);

            assertThat(orderItemHistoryRepository.count()).isEqualTo(historyCountBefore + 1);
        }
    }

    @Nested
    @DisplayName("refuseReturn() - 실패 시나리오")
    class RefuseReturnFailureTests {

        @Test
        @DisplayName("다른 판매자의 반품은 거절할 수 없다")
        void refuseReturn_fails_when_seller_mismatch() {
            // given
            Store otherStore = storeRepository.save(StoreFixture.defaultStore());
            Seller otherSeller = sellerRepository.save(SellerFixture.defaultSeller(otherStore));

            OrderItem orderItem = orderItemRepository.save(
                OrderItemFixture.returnRequestedWithProduct(product)
            );
            ReturnRequest returnRequest = returnRequestRepository.save(
                ReturnRequestFixture.requested(orderItem)
            );

            Long returnId = returnRequest.getId();

            em.flush();
            em.clear();

            // when & then
            assertThatThrownBy(
                () -> sut.refuseReturn(returnId, otherSeller.getId(), "거절 사유")
            ).isInstanceOf(BbangleException.class);

            // 상태가 변경되지 않았는지 확인
            ReturnRequest unchanged = returnRequestRepository.findById(returnId).orElseThrow();
            assertThat(unchanged.getStatus()).isEqualTo(ReturnRequestRequestStatus.REQUESTED);
        }

        @Test
        @DisplayName("COMPLETED 상태의 반품을 거절하려 하면 CLAIM_INVALID_STATUS 예외가 발생한다")
        void refuseReturn_fails_when_already_completed() {
            // given
            OrderItem orderItem = orderItemRepository.save(
                OrderItemFixture.returnApprovedWithProduct(product)
            );
            ReturnRequest returnRequest = returnRequestRepository.save(
                ReturnRequestFixture.completed(orderItem)
            );

            Long returnId = returnRequest.getId();

            em.flush();
            em.clear();

            // when & then
            assertThatThrownBy(
                () -> sut.refuseReturn(returnId, seller.getId(), "거절 사유")
            ).isInstanceOf(BbangleException.class);
        }
    }

    @Nested
    @DisplayName("processReturn() - 성공 시나리오")
    class ProcessReturnSuccessTests {

        @Test
        @DisplayName("INSPECTION_APPROVED 상태의 반품을 처리하면 COMPLETED로 변경되고 히스토리가 저장된다")
        void processReturn_success() {
            // given
            long historyCountBefore = orderItemHistoryRepository.count();

            OrderItem orderItem = orderItemRepository.save(
                OrderItemFixture.returnApprovedWithProduct(product)
            );
            ReturnRequest returnRequest = returnRequestRepository.save(
                ReturnRequestFixture.inspectionApproved(orderItem)
            );

            Long returnId = returnRequest.getId();
            Long orderItemId = orderItem.getId();

            em.flush();
            em.clear();

            // when
            sut.processReturn(returnId, seller.getId());

            // then
            em.flush();
            em.clear();

            ReturnRequest completed = returnRequestRepository.findById(returnId).orElseThrow();
            assertThat(completed.getStatus()).isEqualTo(ReturnRequestRequestStatus.COMPLETED);

            OrderItem completedItem = orderItemRepository.findById(orderItemId).orElseThrow();
            assertThat(completedItem.getOrderStatus()).isEqualTo(OrderStatus.RETURN_COMPLETED);

            assertThat(orderItemHistoryRepository.count()).isEqualTo(historyCountBefore + 1);
        }
    }

    @Nested
    @DisplayName("processReturn() - 실패 시나리오")
    class ProcessReturnFailureTests {

        @Test
        @DisplayName("다른 판매자의 반품은 처리할 수 없다")
        void processReturn_fails_when_seller_mismatch() {
            // given
            Store otherStore = storeRepository.save(StoreFixture.defaultStore());
            Seller otherSeller = sellerRepository.save(SellerFixture.defaultSeller(otherStore));

            OrderItem orderItem = orderItemRepository.save(
                OrderItemFixture.returnApprovedWithProduct(product)
            );
            ReturnRequest returnRequest = returnRequestRepository.save(
                ReturnRequestFixture.inspectionApproved(orderItem)
            );

            Long returnId = returnRequest.getId();

            em.flush();
            em.clear();

            // when & then
            assertThatThrownBy(
                () -> sut.processReturn(returnId, otherSeller.getId())
            ).isInstanceOf(BbangleException.class);

            // 상태가 변경되지 않았는지 확인
            ReturnRequest unchanged = returnRequestRepository.findById(returnId).orElseThrow();
            assertThat(unchanged.getStatus()).isEqualTo(ReturnRequestRequestStatus.INSPECTION_APPROVED);
        }

        @Test
        @DisplayName("REQUESTED 상태의 반품을 최종 처리하려 하면 CLAIM_INVALID_STATUS 예외가 발생한다")
        void processReturn_fails_when_invalid_status() {
            // given
            OrderItem orderItem = orderItemRepository.save(
                OrderItemFixture.returnRequestedWithProduct(product)
            );
            ReturnRequest returnRequest = returnRequestRepository.save(
                ReturnRequestFixture.requested(orderItem)
            );

            Long returnId = returnRequest.getId();

            em.flush();
            em.clear();

            // when & then
            assertThatThrownBy(
                () -> sut.processReturn(returnId, seller.getId())
            ).isInstanceOf(BbangleException.class);
        }
    }
}
