package com.bbangle.bbangle.push.service;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.fixture.*;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.push.domain.Push;
import com.bbangle.bbangle.push.domain.PushCategory;
import com.bbangle.bbangle.push.dto.CreatePushRequest;
import com.bbangle.bbangle.push.dto.PushRequest;
import com.bbangle.bbangle.push.dto.PushResponse;
import com.bbangle.bbangle.store.domain.Store;
import org.junit.jupiter.api.*;

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
        CreatePushRequest request = new CreatePushRequest("testFcmToken1", String.valueOf(PushCategory.BBANGCKETING), store.getId(), board.getId(), product.getId());

        // when
        pushService.createPush(request, member.getId());
        long pushCount = pushRepository.count();
        Push push = pushRepository.findById(1L).get();

        // then
        assertThat(pushCount).isOne();
        assertThat(push.isSubscribed()).isTrue();
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
        PushRequest request = new PushRequest(push.getProductId(), String.valueOf(PushCategory.BBANGCKETING));

        // when
        pushService.cancelPush(request, member.getId());
        long pushCount = pushRepository.count();
        Push foundPush = pushRepository.findById(2L).get();

        // then
        assertThat(pushCount).isOne();
        assertThat(foundPush.isSubscribed()).isFalse();
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
        CreatePushRequest request = new CreatePushRequest("testFcmToken1", String.valueOf(PushCategory.BBANGCKETING), push.getStoreId(), push.getBoardId(), push.getProductId());

        // when
        pushService.createPush(request, member.getId());
        long pushCount = pushRepository.count();
        Push foundPush = pushRepository.findById(3L).get();

        // then
        assertThat(pushCount).isOne();
        assertThat(foundPush.isSubscribed()).isTrue();
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
        PushRequest request = new PushRequest(push.getProductId(), String.valueOf(PushCategory.BBANGCKETING));

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
        List<PushResponse> bbangketingPushList = pushService.getPush(String.valueOf(PushCategory.BBANGCKETING), member.getId());
        List<PushResponse> restockPushList = pushService.getPush(String.valueOf(PushCategory.RESTOCK), member.getId());
        long pushCount = pushRepository.count();

        // then
        assertThat(pushCount).isEqualTo(20);
        assertThat(bbangketingPushList).hasSize(10);
        assertThat(restockPushList).hasSize(10);
    }


    private Push createPush(Member member) {
        Store store = createStore();
        Board board = createBoard(store);
        Product product = createProduct(board);
        return PushFixture.newBbangketingPush(member.getId(), store.getId(), board.getId(), product.getId());
    }


    private Push createCanceledPush(Member member) {
        Store store = createStore();
        Board board = createBoard(store);
        Product product = createProduct(board);
        return PushFixture.newCanceledPush(member.getId(), store.getId(), board.getId(), product.getId());
    }


    private void create20Pushes(Member member) {
        Store store = createStore();
        Board board = createBoard(store);
        List<Push> pushList = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            Product product = createProduct(board);
            Push bbangketingPush = PushFixture.newBbangketingPush(member.getId(), store.getId(), board.getId(), product.getId());
            Push restockPush = PushFixture.newRestockPush(member.getId(), store.getId(), board.getId(), product.getId());
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
        return productRepository.save(ProductFixture.randomProduct(board));
    }


    private Member createMember() {
        return memberRepository.save(MemberFixture.createKakaoMember());
    }

}