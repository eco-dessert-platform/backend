package com.bbangle.bbangle.store.repository;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.dto.StoreDetailStoreDto;

import com.bbangle.bbangle.wishlist.domain.WishListStore;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class StoreRepositoryTest extends AbstractIntegrationTest {

    private static final String TEST_TITLE = "TestTitle";
    private static final Long NULL_MEMBER_ID = null;

    @Nested
    @DisplayName("getStoreResponse 메서드는")
    class GetStoreResponse {
        private Store store;
        private Member member;

        @BeforeEach
        void init() {
            store = fixtureStore(Map.of("name", TEST_TITLE));
            createWishListStore();
        }

        @Test
        @DisplayName("유효한 StoreId로 스토어 정보를 가져올 수 있다")
        void validStoreId() {
            StoreDetailStoreDto storeResponse = storeRepository.getStoreResponse(NULL_MEMBER_ID, store.getId());

            assertThat(storeResponse.getStoreName(), is(TEST_TITLE));
            assertThat(storeResponse.getIsWished(), is(false));
        }

        @Test
        @DisplayName("스토어 위시리스트를 등록한 MemberId로 스토어 위시리스트 True를 가져올 수 있다")
        void validWishListStore() {
            StoreDetailStoreDto storeResponse = storeRepository.getStoreResponse(member.getId(), store.getId());

            assertThat(storeResponse.getIsWished(), is(true));;
        }

        @Test
        @DisplayName("스토어 위시리스트를 등록한 MemberId로 스토어 위시리스트 True를 가져올 수 있다")
        void validNonWishListStore() {
            Long notWishedMemberId = member.getId() + 1L;
            StoreDetailStoreDto storeResponse = storeRepository.getStoreResponse(notWishedMemberId, store.getId());

            assertThat(storeResponse.getIsWished(), is(false));
        }

        void createWishListStore() {
             member = memberRepository.save(Member.builder().build());
             wishListStoreRepository.save(WishListStore.builder()
                     .store(store)
                     .member(member)
                     .build());
        }
    }
}
