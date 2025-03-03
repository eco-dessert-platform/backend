package com.bbangle.bbangle.store.repository;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.wishlist.domain.WishListStore;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

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

        void createWishListStore() {
            member = memberRepository.save(Member.builder().build());
            wishListStoreRepository.save(WishListStore.builder()
                .store(store)
                .member(member)
                .build());
        }
    }
}
