package com.bbangle.bbangle.claim.seller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.claim.domain.ReturnRequest;
import com.bbangle.bbangle.claim.domain.constant.DecisionType;
import com.bbangle.bbangle.claim.domain.constant.ReturnRequestRequestStatus;
import com.bbangle.bbangle.claim.repository.ClaimRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.board.domain.ProductFixture;
import com.bbangle.bbangle.fixture.claim.ReturnRequestFixture;
import com.bbangle.bbangle.fixture.order.OrderItemFixture;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.order.repository.OrderItemRepository;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] SellerClaimService")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SellerClaimServiceIntegrationTest {

    @Autowired
    private SellerClaimService sut;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

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
    private OrderItem orderItem;
    private ReturnRequest returnRequest;

    @BeforeEach
    void setUp() {
        // Store → Product → OrderItem → ReturnRequest, Store ← Seller
        store = storeRepository.save(StoreFixture.defaultStore());
        seller = sellerRepository.save(SellerFixture.defaultSeller(store));
        product = productRepository.save(ProductFixture.defaultProductWithStore(store));
        orderItem = orderItemRepository.save(OrderItemFixture.returnRequestedWithProduct(product));
        returnRequest = claimRepository.save(ReturnRequestFixture.requested(orderItem));

        em.flush();
        em.clear();
    }

    @Nested
    @DisplayName("decision()")
    class Decision {

        @Test
        @DisplayName("판매자가 반품 요청을 승인하면 ReturnRequest는 APPROVED, OrderItem은 RETURN_APPROVED가 된다")
        void given_validSellerAndClaim_when_approve_then_statusChangedToApproved() {
            // when
            sut.decision(returnRequest.getId(), seller.getId(), DecisionType.APPROVE, "검수 완료");
            em.flush();
            em.clear();

            // then
            ReturnRequest found = (ReturnRequest) claimRepository.findById(returnRequest.getId()).get();
            OrderItem foundOrderItem = orderItemRepository.findById(orderItem.getId()).get();

            assertThat(found.getStatus()).isEqualTo(ReturnRequestRequestStatus.APPROVED);
            assertThat(foundOrderItem.getOrderStatus()).isEqualTo(OrderStatus.RETURN_APPROVED);
        }

        @Test
        @DisplayName("판매자가 반품 요청을 거절하면 ReturnRequest는 REJECTED, OrderItem은 RETURN_REJECTED가 된다")
        void given_validSellerAndClaim_when_reject_then_statusChangedToRejected() {
            // when
            sut.decision(returnRequest.getId(), seller.getId(), DecisionType.REJECT, "상품 하자 없음");
            em.flush();
            em.clear();

            // then
            ReturnRequest found = (ReturnRequest) claimRepository.findById(returnRequest.getId()).get();
            OrderItem foundOrderItem = orderItemRepository.findById(orderItem.getId()).get();

            assertThat(found.getStatus()).isEqualTo(ReturnRequestRequestStatus.REJECTED);
            assertThat(foundOrderItem.getOrderStatus()).isEqualTo(OrderStatus.RETURN_REJECTED);
        }

        @Test
        @DisplayName("판매자와 반품 요청이 매칭되지 않으면 SELLER_CLAIM_MISMATCH 예외가 발생한다")
        void given_mismatchedSeller_when_decision_then_throwSellerClaimMismatch() {
            // given
            Long otherSellerId = 99999L;

            // when & then
            assertThatThrownBy(() ->
                sut.decision(returnRequest.getId(), otherSellerId, DecisionType.APPROVE, "승인"))
                .isInstanceOf(BbangleException.class)
                .satisfies(ex -> {
                    BbangleException bex = (BbangleException) ex;
                    assertThat(bex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.SELLER_CLAIM_MISMATCH);
                });
        }

        @Test
        @DisplayName("존재하지 않는 반품 요청 ID로 호출하면 SELLER_CLAIM_MISMATCH 예외가 발생한다")
        void given_nonExistentClaimId_when_decision_then_throwException() {
            // given
            Long nonExistentReturnId = 99999L;

            // when & then
            assertThatThrownBy(() ->
                sut.decision(nonExistentReturnId, seller.getId(), DecisionType.APPROVE, "승인"))
                .isInstanceOf(BbangleException.class)
                .satisfies(ex -> {
                    BbangleException bex = (BbangleException) ex;
                    assertThat(bex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.SELLER_CLAIM_MISMATCH);
                });
        }

        @Test
        @DisplayName("이미 승인된 반품 요청을 다시 승인하면 CLAIM_INVALID_STATUS 예외가 발생한다")
        void given_alreadyApprovedClaim_when_approveAgain_then_throwInvalidStatus() {
            // given
            sut.decision(returnRequest.getId(), seller.getId(), DecisionType.APPROVE, "검수 완료");
            em.flush();
            em.clear();

            // when & then
            assertThatThrownBy(() ->
                sut.decision(returnRequest.getId(), seller.getId(), DecisionType.APPROVE, "재승인"))
                .isInstanceOf(BbangleException.class)
                .satisfies(ex -> {
                    BbangleException bex = (BbangleException) ex;
                    assertThat(bex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.CLAIM_INVALID_STATUS);
                });
        }

        @Test
        @DisplayName("이미 거절된 반품 요청을 승인하면 CLAIM_INVALID_STATUS 예외가 발생한다")
        void given_alreadyRejectedClaim_when_approve_then_throwInvalidStatus() {
            // given
            sut.decision(returnRequest.getId(), seller.getId(), DecisionType.REJECT, "거절 사유");
            em.flush();
            em.clear();

            // when & then
            assertThatThrownBy(() ->
                sut.decision(returnRequest.getId(), seller.getId(), DecisionType.APPROVE, "승인"))
                .isInstanceOf(BbangleException.class)
                .satisfies(ex -> {
                    BbangleException bex = (BbangleException) ex;
                    assertThat(bex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.CLAIM_INVALID_STATUS);
                });
        }
    }
}
