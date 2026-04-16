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

@DisplayName("[단위 테스트] UpdateStoreNamesInfo")
class UpdateStoreNamesInfoTest {

    @Nested
    @DisplayName("UpdateStoreNames.from() 테스트")
    class UpdateStoreNamesFromTest {

        @Test
        @DisplayName("from() 메서드는 requestId를 포함한 모든 필드를 올바르게 매핑한다.")
        void from_mapsAllFieldsIncludingRequestId() {

            // given
            Store store = StoreFixture.withId(StoreFixture.defaultStore(), 10L);
            Seller seller = SellerFixture.defaultSeller(store);
            StoreNameRequest storeNameRequest = StoreNameRequestFixture.withId(
                StoreNameRequestFixture.defaultStoreNameRequest(seller, store), 42L
            );
            ReflectionTestUtils.setField(storeNameRequest, "createdAt",
                java.time.LocalDateTime.of(2026, 1, 1, 1, 23, 45));

            // when
            UpdateStoreNames result = UpdateStoreNames.from(storeNameRequest);

            // then
            assertThat(result.requestId()).isEqualTo(42L);
            assertThat(result.storeId()).isEqualTo(10L);
            assertThat(result.currentName()).isEqualTo(DEFAULT_STORE_NAME);
            assertThat(result.newName()).isEqualTo(NEW_STORE_NAME);
            assertThat(result.createdAt()).isNotNull();
        }

        @Test
        @DisplayName("from() 메서드는 requestId가 null인 경우 null을 반환한다.")
        void from_withNullRequestId_returnsNullRequestId() {

            // given
            Store store = StoreFixture.withId(StoreFixture.defaultStore(), 5L);
            Seller seller = SellerFixture.defaultSeller(store);
            // ID를 설정하지 않아 null 상태
            StoreNameRequest storeNameRequest = StoreNameRequestFixture.defaultStoreNameRequest(seller, store);

            // when
            UpdateStoreNames result = UpdateStoreNames.from(storeNameRequest);

            // then
            assertThat(result.requestId()).isNull();
            assertThat(result.storeId()).isEqualTo(5L);
            assertThat(result.currentName()).isEqualTo(DEFAULT_STORE_NAME);
            assertThat(result.newName()).isEqualTo(NEW_STORE_NAME);
        }

        @Test
        @DisplayName("from() 메서드는 currentName과 newName을 정확하게 매핑한다.")
        void from_mapsCurrentNameAndNewName() {

            // given
            String customStoreName = "커스텀 스토어";
            Store store = StoreFixture.withId(StoreFixture.defaultStore(customStoreName), 20L);
            Seller seller = SellerFixture.defaultSeller(store);
            StoreNameRequest storeNameRequest = StoreNameRequestFixture.withId(
                StoreNameRequestFixture.defaultStoreNameRequest(seller, store), 99L
            );

            // when
            UpdateStoreNames result = UpdateStoreNames.from(storeNameRequest);

            // then
            assertThat(result.currentName()).isEqualTo(customStoreName);
            assertThat(result.newName()).isEqualTo(NEW_STORE_NAME);
            assertThat(result.requestId()).isEqualTo(99L);
        }
    }
}