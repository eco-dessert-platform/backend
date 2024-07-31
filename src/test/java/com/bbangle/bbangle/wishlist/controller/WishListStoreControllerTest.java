package com.bbangle.bbangle.wishlist.controller;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.ranking.BoardStatisticConfig;
import com.bbangle.bbangle.fixture.MemberFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.token.jwt.TokenProvider;
import com.bbangle.bbangle.wishlist.domain.WishListStore;
import com.bbangle.bbangle.wishlist.repository.WishListStoreRepository;
import com.bbangle.bbangle.wishlist.repository.impl.WishListStoreRepositoryImpl;
import com.bbangle.bbangle.wishlist.service.WishListStoreService;
import java.time.Duration;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//TODO: 이 어노테이션을 사용하니 이 다음 차례의 test 클래스에서 DB가 아예 삭제되는 문제가 발생해 주석처리했습니다.
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class WishListStoreControllerTest extends AbstractIntegrationTest {

    @Autowired
    WishListStoreService wishListStoreService;

    @Autowired
    WishListStoreRepository wishListStoreRepository;

    @Autowired
    WishListStoreRepositoryImpl wishListStoreRepositoryImpl;

    @MockBean
    BoardStatisticConfig boardWishListConfig;

    @Autowired
    ResponseService responseService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    StoreRepository storeRepository;

    @Autowired
    TokenProvider tokenProvider;

    /**
     * TODO: test 수정 확인필요 @동석님
     * 이 부분을 넣으면 오히려 안되는 경우가 있어서 주석처리 했습니다
     * store가 1부터 시작하지 않는 경우가 있어 store/1로 하는 경우 CRD가 안되는 경우가 있습니다
     * 이로 인해 firstSaveId, LastSavedId를 명시해 테스트 진행했습니다.
     * Member를 선언한 곳이 있어서 이 Member로 실제 테스트를 해봤습니다(@CustomMockUser 안쓰는 방식)
     * -> 이 부분은 테스트하다 변경한 내용이라 원상복구하셔도 됩니다!
     */

//    @BeforeEach
//    void setUpMockMvc() {
//        this.mockMvc = MockMvcBuilders.standaloneSetup(
//                new WishListStoreController(wishListStoreService, responseService))
//            .build();
//    }

    Long memberId;
    Long firstSavedId;
    Long lastSavedId;

    @BeforeEach
    void createData() {
        wishListStoreRepository.deleteAll();
        Member member = MemberFixture.createKakaoMember();
        member = memberRepository.save(member);
        memberId = member.getId();
        createWishListStore(member);
    }

    private void createWishListStore(Member member) {
        for (int i = 1; i <= 25; i++) {
            Store store = Store.builder()
                .name("test" + i)
                .introduce("introduce" + i)
                .isDeleted(false)
                .build();
            Store save = storeRepository.save(store);
            if (i == 1) {
                firstSavedId = save.getId();
            }

            if (i == 25) {
                lastSavedId = save.getId();
            }
            if (i != 25) {
                WishListStore wishlistStore = WishListStore.builder()
                    .member(member)
                    .store(store)
                    .build();
                wishListStoreRepository.save(wishlistStore);
            }
        }
    }

    @DisplayName("위시리스트 스토어 전체 조회를 시행한다")
    @Test
    void getWishListStores() throws Exception {
        String authentication = getAuthentication(memberId);
        mockMvc.perform(get("/api/v1/likes/stores")
                .header(HttpHeaders.AUTHORIZATION, authentication))
            .andExpect(jsonPath("$.result.hasNext").value(true))
            .andExpect(jsonPath("$.result.nextCursor").value(4))
            .andExpect(status().isOk())
            .andDo(print());
    }

    @DisplayName("위시리스트 삭제를 시행한다")
    @Test
    void deleteWishListStore() throws Exception {
        String authentication = getAuthentication(memberId);
        mockMvc.perform(patch("/api/v1/likes/store/" + firstSavedId)
                .header(HttpHeaders.AUTHORIZATION, authentication))
            .andExpect(status().isOk())
            .andDo(print());
    }

    @DisplayName("위시리스트 추가를 시행한다")
    @Test
    void addWishListStore() throws Exception {
        String authentication = getAuthentication(memberId);
        mockMvc.perform(post("/api/v1/likes/store/" + lastSavedId)
                .header(HttpHeaders.AUTHORIZATION, authentication))
            .andExpect(status().isOk())
            .andDo(print());
    }

    private String getAuthentication(Long memberId) {
        String token = tokenProvider.generateToken(memberId, Duration.ofMinutes(1));
        return "Bearer " + token;
    }

}
