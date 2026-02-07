package com.bbangle.bbangle.store.seller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.common.page.CursorPagination;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreStatus;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.SellerStoreDetail;
import com.bbangle.bbangle.store.seller.controller.mapper.SellerStoreMapper;
import com.bbangle.bbangle.store.seller.service.model.SellerStoreInfo.StoreInfo;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("[단위테스트] SellerStoreService")
@ExtendWith(MockitoExtension.class)
class SellerStoreServiceUnitTest {

    private final String profile = "test/s3/seller";
    private final String introduce = "testIntroduce";
    private final String phone = "01012346789";
    private final String subPhone = "01098765432";
    private final String email = "test1234@gmail.com";
    private final String address = "경기도 수원시 팔달구";
    private final String detailAddress = "화성행궁 12번지";

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private SellerStoreMapper sellerStoreMapper;

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

    @Test
    @DisplayName("스토어가 존재하고 상태가 NONE이면 사용 가능하다.")
    void checkStoreName_storeExists_available() {

        // given
        String originalStoreName = "   StoreA ";
        String normalizedStoreName = "StoreA";

        Store store = Store.createForSeller(normalizedStoreName, profile, introduce, phone, subPhone, email, address, detailAddress);
        SellerStoreDetail detail = mock(SellerStoreDetail.class);

        given(storeRepository.findByStoreNameAndIsNotDeleted(normalizedStoreName)).willReturn(Optional.of(store));
        given(sellerStoreMapper.toSellerStoreDetail(store)).willReturn(detail);

        // when
        StoreResponse.StoreNameCheck result = sellerStoreService.checkStoreName(originalStoreName);

        // then
        assertThat(result.available()).isTrue();
        assertThat(result.store()).isEqualTo(detail);

        verify(storeRepository).findByStoreNameAndIsNotDeleted(normalizedStoreName);
        verify(sellerStoreMapper).toSellerStoreDetail(store);
    }

    @Test
    @DisplayName("스토어가 존재하지만 상태가 NONE이 아니면 사용 불가")
    void checkStoreName_storeExists_Unavailable() {

        // given
        String storeName = "StoreA";

        Store store = Store.createForSeller(storeName, profile, introduce, phone, subPhone, email, address, detailAddress);
        store.changeStatus(StoreStatus.ACTIVE);

        given(storeRepository.findByStoreNameAndIsNotDeleted(storeName)).willReturn(Optional.of(store));
        given(sellerStoreMapper.toSellerStoreDetail(store)).willReturn(mock(SellerStoreDetail.class));

        // when
        StoreResponse.StoreNameCheck result = sellerStoreService.checkStoreName(storeName);

        // then
        assertThat(result.available()).isFalse();
    }

    @Test
    @DisplayName("스토어가 존재하지 않으면 사용 가능하고 store는 null을 반환한다.")
    void checkStoreName_storeNotExists() {

        // given
        String storeName = "UnknownStore";

        given(storeRepository.findByStoreNameAndIsNotDeleted(storeName)).willReturn(Optional.empty());

        // when
        StoreResponse.StoreNameCheck result = sellerStoreService.checkStoreName(storeName);

        // then
        assertThat(result.available()).isTrue();
        assertThat(result.store()).isNull();

        verify(sellerStoreMapper, never()).toSellerStoreDetail(any());
    }
}