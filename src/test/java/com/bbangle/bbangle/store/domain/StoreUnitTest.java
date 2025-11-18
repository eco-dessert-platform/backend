package com.bbangle.bbangle.store.domain;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;


public class StoreUnitTest {

    @Test
    @DisplayName("셀러 생성을 위한 스토어 객체 생성에 성공한다")
    void success_create_store_for_seller() {
        // assert
        String storeName = "testStore";
        Store store = Store.createForSeller(storeName);

        assertThat(store).isNotNull();
        assertThat(store.getName()).isEqualTo(storeName);
        assertThat(store.isDeleted()).isFalse();
        assertThat(store.getStatus()).isEqualTo(StoreStatus.NONE);

    }

    @ParameterizedTest
    @DisplayName("유효하지 않은 스토어 명으로 인해 객체 생성에 실패한다")
    @NullAndEmptySource
    void fail_create_store_for_seller(String storeName) {
        // assert
        assertThatThrownBy(() -> Store.createForSeller(storeName))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.INVALID_STORE_NAME.getMessage());
    }


}
