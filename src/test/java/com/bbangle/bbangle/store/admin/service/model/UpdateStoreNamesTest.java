package com.bbangle.bbangle.store.admin.service.model;

import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_STORE_NAME;
import static com.bbangle.bbangle.fixture.store.domain.StoreNameRequestFixture.NEW_STORE_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreNameRequestFixture;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.store.admin.service.model.UpdateStoreNamesInfo.UpdateStoreNames;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreNameRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

@DisplayName("[단위 테스트] UpdateStoreNames")
class UpdateStoreNamesTest {

    @Nested
    @DisplayName("from() 테스트")
    class FromTest {

        @Test
        @DisplayName("StoreNameRequest로부터 requestId가 올바르게 매핑된다.")
        void from_mapsRequestIdCorrectly() {

            // given
            Store store = StoreFixture.withId(StoreFixture.defaultStore(), 5L);
            Seller seller = SellerFixture.defaultSeller(store);
            StoreNameRequest storeNameRequest = StoreNameRequestFixture.withId(
                StoreNameRequestFixture.defaultStoreNameRequest(seller, store), 42L
            );
            ReflectionTestUtils.setField(storeNameRequest, "createdAt", LocalDateTime.of(2026, 1, 1, 0, 0));

            // when
            UpdateStoreNames result = UpdateStoreNames.from(storeNameRequest);

            // then
            assertThat(result.requestId()).isEqualTo(42L);
        }

        @Test
        @DisplayName("StoreNameRequest로부터 모든 필드가 올바르게 매핑된다.")
        void from_mapsAllFieldsCorrectly() {

            // given
            Store store = StoreFixture.withId(StoreFixture.defaultStore(), 10L);
            Seller seller = SellerFixture.defaultSeller(store);
            StoreNameRequest storeNameRequest = StoreNameRequestFixture.withId(
                StoreNameRequestFixture.defaultStoreNameRequest(seller, store), 7L
            );
            LocalDateTime createdAt = LocalDateTime.of(2026, 3, 15, 12, 30);
            ReflectionTestUtils.setField(storeNameRequest, "createdAt", createdAt);

            // when
            UpdateStoreNames result = UpdateStoreNames.from(storeNameRequest);

            // then
            assertThat(result.requestId()).isEqualTo(7L);
            assertThat(result.storeId()).isEqualTo(10L);
            assertThat(result.currentName()).isEqualTo(DEFAULT_STORE_NAME);
            assertThat(result.newName()).isEqualTo(NEW_STORE_NAME);
            assertThat(result.createdAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("requestId가 null인 경우(미저장 엔티티)에도 null로 매핑된다.")
        void from_mapsNullRequestId_whenEntityNotPersisted() {

            // given
            Store store = StoreFixture.withId(StoreFixture.defaultStore(), 1L);
            Seller seller = SellerFixture.defaultSeller(store);
            // withId를 호출하지 않으면 id는 null (미저장 엔티티)
            StoreNameRequest storeNameRequest = StoreNameRequestFixture.defaultStoreNameRequest(seller, store);

            // when
            UpdateStoreNames result = UpdateStoreNames.from(storeNameRequest);

            // then
            assertThat(result.requestId()).isNull();
            assertThat(result.storeId()).isEqualTo(1L);
            assertThat(result.currentName()).isEqualTo(DEFAULT_STORE_NAME);
            assertThat(result.newName()).isEqualTo(NEW_STORE_NAME);
        }
    }
}