package com.bbangle.bbangle.push.controller;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.DatabaseCleaner;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.ProductFixture;
import com.bbangle.bbangle.fixture.PushFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.mock.WithCustomMockUser;
import com.bbangle.bbangle.push.domain.Push;
import com.bbangle.bbangle.push.domain.PushCategory;
import com.bbangle.bbangle.push.dto.CreatePushRequest;
import com.bbangle.bbangle.push.dto.PushRequest;
import com.bbangle.bbangle.store.domain.Store;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PushControllerTest extends AbstractIntegrationTest {

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    DatabaseCleaner databaseCleaner;


    @AfterEach
    void tearDown() {
        databaseCleaner.clearMember();
    }


    @Test
    @Order(0)
    @DisplayName("신규 푸시 알림 신청이 정상적으로 등록된다.")
    @WithCustomMockUser
    void createPushTest() throws Exception {
        // given
        Store store = createStore();
        Board board = createBoard(store);
        Product product = createProduct(board);
        Member member = createMember();

        CreatePushRequest request = new CreatePushRequest("testFcmToken1", String.valueOf(PushCategory.BBANGCKETING), store.getId(), board.getId(), product.getId());
        String requestBody = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(post("/api/v1/push")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andDo(print());
    }


    @Test
    @Order(1)
    @DisplayName("푸시 알림 신청이 정상적으로 해제된다.")
    @WithCustomMockUser
    void cancelPushTest() throws Exception {
        // given
        Member member = createMember();
        Push newPush = createPush(member);
        Push push = pushRepository.save(newPush);

        PushRequest request = new PushRequest(push.getProductId(), String.valueOf(PushCategory.BBANGCKETING));
        String requestBody = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(patch("/api/v1/push")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andDo(print());
    }


    @Test
    @Order(2)
    @DisplayName("해제한 푸시 알림에 대해 재신청이 정상적으로 등록된다.")
    @WithCustomMockUser
    void resubscribePushTest() throws Exception {
        // given
        Member member = createMember();
        Push newPush = createCanceledPush(member);
        Push push = pushRepository.save(newPush);

        CreatePushRequest request = new CreatePushRequest("testFcmToken1", String.valueOf(PushCategory.BBANGCKETING), push.getStoreId(), push.getBoardId(), push.getProductId());
        String requestBody = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(post("/api/v1/push")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andDo(print());
    }


    @Test
    @Order(3)
    @DisplayName("푸시 알림 신청이 정상적으로 삭제된다.")
    @WithCustomMockUser
    void deletePushTest() throws Exception {
        // given
        Member member = createMember();
        Push newPush = createPush(member);
        Push push = pushRepository.save(newPush);

        PushRequest request = new PushRequest(push.getProductId(), String.valueOf(PushCategory.BBANGCKETING));
        String requestBody = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(delete("/api/v1/push")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andDo(print());
    }


    @Test
    @Order(4)
    @DisplayName("신청한 푸시 알림이 정상적으로 조회된다.")
    @WithCustomMockUser
    void getPushTest() throws Exception {
        // given
        Member member = createMember();
        create20Pushes(member);

        // when & then
        mockMvc.perform(get("/api/v1/push?pushCategory=BBANGCKETING"))
                .andExpect(status().isOk())
                .andDo(print());
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
        Member member = Member.builder()
                .id(2L)
                .build();

        return memberRepository.save(member);
    }

}