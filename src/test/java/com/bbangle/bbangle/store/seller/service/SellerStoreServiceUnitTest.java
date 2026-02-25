package com.bbangle.bbangle.store.seller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.common.page.CursorPagination;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.store.seller.service.model.SellerStoreInfo.StoreInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("[단위테스트] SellerStoreService")
@ExtendWith(MockitoExtension.class)
class SellerStoreServiceUnitTest {

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private SellerStoreService sellerStoreService;

    @Test
    @DisplayName("스토어 이름 검색 시 모든 공백을 제거한 이름으로 조회한다.")
    void selectStoreNameForSeller_normalizedStoreName() {

        // given
        String originalStoreName = "   Sto   r  e  Nam   e ";
        String normalized = "StoreName";
        Long cursorId = 10L;

        CursorPagination<StoreInfo> mockResult = mock(CursorPagination.class);

        given(storeRepository.findByStoreNameWithCursor(normalized, cursorId)).willReturn(mockResult);

        // when
        CursorPagination<StoreInfo> result = sellerStoreService.selectStoreNameForSeller(originalStoreName, cursorId);

        // then
        assertThat(result).isSameAs(mockResult);

        verify(storeRepository).findByStoreNameWithCursor(normalized, cursorId);
    }
}