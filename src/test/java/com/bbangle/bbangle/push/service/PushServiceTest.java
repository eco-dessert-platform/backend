package com.bbangle.bbangle.push.service;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.MemberFixture;
import com.bbangle.bbangle.fixture.ProductFixture;
import com.bbangle.bbangle.fixture.PushFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.push.domain.Push;
import com.bbangle.bbangle.push.domain.PushCategory;
import com.bbangle.bbangle.push.domain.PushType;
import com.bbangle.bbangle.push.dto.CreatePushRequest;
import com.bbangle.bbangle.push.dto.FcmRequest;
import com.bbangle.bbangle.push.dto.PushRequest;
import com.bbangle.bbangle.push.dto.PushResponse;
import com.bbangle.bbangle.store.domain.Store;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PushServiceTest extends AbstractIntegrationTest {


    @Test
    @Order(0)
    @DisplayName("신규 푸시 알림 신청이 정상적으로 등록된다.")
    void createPushTest() {
        // given
        Store store = createStore();
        Board board = createBoard(store);
        Product product = createProduct(board);
        Member member = createMember();
        CreatePushRequest request = createPushRequest(product.getId());

        // when
        pushService.createPush(request, member.getId());
        long pushCount = pushRepository.count();
        Push resultPush = null;
        List<Push> pushList = pushRepository.findAll();
        for (Push p : pushList) {
            resultPush = p;
        }

        // then
        assertThat(pushCount).isOne();
        assertThat(pushList).hasSize(1);
        assertThat(resultPush.isActive()).isTrue();
    }



    @Test
    @Order(1)
    @DisplayName("푸시 알림 신청이 정상적으로 해제된다.")
    void cancelPushTest() {
        // given
        Member newMember = createMember();
        Member member = memberRepository.save(newMember);
        Push newPush = createPush(newMember);
        Push push = pushRepository.save(newPush);
        PushRequest request = new PushRequest(push.getProductId(), String.valueOf(PushType.DATE), PushCategory.BBANGCKETING);

        // when
        pushService.cancelPush(request, member.getId());
        long pushCount = pushRepository.count();
        Push resultPush = null;
        List<Push> pushList = pushRepository.findAll();
        for (Push p : pushList) {
            resultPush = p;
        }

        // then
        assertThat(pushCount).isOne();
        assertThat(pushList).hasSize(1);
        assertThat(resultPush.isActive()).isFalse();
    }


    @Test
    @Order(2)
    @DisplayName("해제한 푸시 알림에 대해 재신청이 정상적으로 등록된다.")
    void resubscribePushTest() {
        // given
        Member newMember = createMember();
        Member member = memberRepository.save(newMember);
        Push newPush = createCanceledPush(member);
        Push push = pushRepository.save(newPush);
        CreatePushRequest request = createPushRequest(push.getProductId());

        // when
        pushService.createPush(request, member.getId());
        long pushCount = pushRepository.count();
        Push resultPush = null;
        List<Push> pushList = pushRepository.findAll();
        for (Push p : pushList) {
            resultPush = p;
        }

        // then
        assertThat(pushCount).isOne();
        assertThat(pushList).hasSize(1);
        assertThat(resultPush.isActive()).isTrue();
    }


    @Test
    @Order(3)
    @DisplayName("푸시 알림 신청이 정상적으로 삭제된다.")
    void deletePushTest() {
        // given
        Member newMember = createMember();
        Member member = memberRepository.save(newMember);
        Push newPush = createPush(newMember);
        Push push = pushRepository.save(newPush);
        PushRequest request = new PushRequest(push.getProductId(), String.valueOf(PushType.DATE), PushCategory.BBANGCKETING);

        // when
        pushService.deletePush(request, member.getId());
        long pushCount = pushRepository.count();

        // then
        assertThat(pushCount).isZero();
    }


    @Test
    @Order(4)
    @DisplayName("신청한 푸시 알림이 정상적으로 조회된다.")
    void getPushTest() {
        // given
        Member newMember = createMember();
        Member member = memberRepository.save(newMember);
        create20Pushes(member);

        // when
        List<PushResponse> bbangketingPushList = pushService.getPushes(PushCategory.BBANGCKETING, member.getId());
        List<PushResponse> restockPushList = pushService.getPushes(PushCategory.RESTOCK, member.getId());
        long pushCount = pushRepository.count();

        // then
        assertThat(pushCount).isEqualTo(20);
        assertThat(bbangketingPushList).hasSize(10);
        assertThat(restockPushList).hasSize(10);
    }


    @Test
    @Order(5)
    @DisplayName("푸시 알림이 나가야 하는 선별된 모든 요청이 정상적으로 조회된다.")
    void selectPushListTest() {
        // given
        Store store = createStore();
        Board board = createBoard(store);
        Product product = createProduct(board);
        Member member = createMember();
        CreatePushRequest request = createPushRequest(product.getId());
        pushService.createPush(request, member.getId());

        // when
        List<FcmRequest> requestList = pushService.getPushesForNotification();

        // then
        assertThat(requestList).hasSize(1);
        assertThat(requestList.get(0).getFcmToken()).isEqualTo("testFcmToken1");
        assertThat(requestList.get(0).getPushCategory()).isEqualTo("입고");
    }


    @Test
    @Order(6)
    @DisplayName("푸시 알림의 제목과 내용이 정상적으로 편집된다.")
    void editMessageTest() {
        // given
        Store store = createStore();
        Board board = createBoard(store);
        Product product = createProduct(board);
        Member member = createMember();
        CreatePushRequest request = createPushRequest(product.getId());
        pushService.createPush(request, member.getId());
        List<FcmRequest> requestList = pushService.getPushesForNotification();

        // when
        pushService.editMessage(requestList);

        // then
        assertThat(requestList).hasSize(1);
        assertThat(requestList.get(0).getTitle()).contains("님이 기다리던 상품이 입고되었어요!");
        assertThat(requestList.get(0).getBody()).contains("곧 품절될 수 있으니 지금 확인해보세요.");
    }


    private Push createPush(Member member) {
        Store store = createStore();
        Board board = createBoard(store);
        Product product = createProduct(board);
        return PushFixture.newBbangketingPush(member.getId(), product.getId());
    }


    private Push createCanceledPush(Member member) {
        Store store = createStore();
        Board board = createBoard(store);
        Product product = createProduct(board);
        return PushFixture.newCanceledPush(member.getId(), product.getId());
    }


    private void create20Pushes(Member member) {
        Store store = createStore();
        Board board = createBoard(store);
        List<Push> pushList = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            Product product = createProduct(board);
            Push bbangketingPush = PushFixture.newBbangketingPush(member.getId(), product.getId());
            Push restockPush = PushFixture.newRestockPush(member.getId(), product.getId());
            pushList.add(bbangketingPush);
            pushList.add(restockPush);
        }

        pushRepository.saveAll(pushList);
    }


    private Store createStore() {
        return storeRepository.save(StoreFixture.storeGenerator());
    }

    private Board createBoard(Store store) {
        return boardRepository.save(BoardFixture.randomBoard(store));
    }

    private Product createProduct(Board board) {
        return productRepository.save(ProductFixture.randomProductWithOrderDate(board));
    }

    private Member createMember() {
        return memberRepository.save(MemberFixture.createKakaoMember());
    }

    private CreatePushRequest createPushRequest(Long productId) {
        return new CreatePushRequest("testFcmToken1", PushType.DATE, null,
                PushCategory.BBANGCKETING, null, productId);
    }


}