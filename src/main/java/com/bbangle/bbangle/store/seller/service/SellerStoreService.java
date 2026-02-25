package com.bbangle.bbangle.store.seller.service;

import com.bbangle.bbangle.common.page.CursorPagination;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.store.seller.controller.dto.StoreRequest;
import com.bbangle.bbangle.store.seller.service.model.SellerStoreInfo.StoreInfo;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerStoreService {

    private final StoreRepository storeRepository;

    @Transactional(readOnly = true)
    public Store findStore(Long storeId) {
        return storeRepository.findById(storeId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.STORE_NOT_FOUND));
    }

    // TODO : Store 생성 메서드 삭제하기 > Admin으로 이동할 예정
    @Transactional
    public Store createStore(StoreRequest.StoreCreateRequest request, String profileImagePath) {
        // 1. DB에 존재하는 스토어를 사용하는 경우
        if (request.storeId() != null) {
            Store store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> new BbangleException(BbangleErrorCode.STORE_NOT_FOUND));

            // 1-1. 새로 업로드한 이미지 파일이 없을 경우 기존의 스토어 프로필을 사용
            String newProfile = profileImagePath == null ? request.profile() : profileImagePath;

            store.updateDetail(
                newProfile,
                request.introduce(),
                request.phoneNumber(),
                request.subPhoneNumber(),
                request.email(),
                request.originAddress(),
                request.originAddressDetail()
            );

            return store;
        }

        // 2. 새로운 스토어 생성하는 경우
        if (profileImagePath == null || profileImagePath.isEmpty()) {
            throw new BbangleException(BbangleErrorCode.INVALID_PROFILE);
        }

        return storeRepository.save(
            Store.createForSeller(
                request.storeName(),
                profileImagePath,
                request.introduce(),
                request.phoneNumber(),
                request.subPhoneNumber(),
                request.email(),
                request.originAddress(),
                request.originAddressDetail()
            )
        );
    }

    // TODO : Admin으로 이동
    @Transactional
    public void registerStore(Seller seller, Store store) {
        seller.registerStore(store);
    }

    @Transactional(readOnly = true)
    public CursorPagination<StoreInfo> selectStoreNameForSeller(String storeName, Long cursorId) {
        String normalizedStoreName = storeName.replaceAll("\\s+", "");

        return storeRepository.findByStoreNameWithCursor(normalizedStoreName, cursorId);
    }

    // TODO : 테스트 코드 수정
    @Transactional(readOnly = true)
    public Optional<Store> checkStoreName(String storeName) {
        String normalizedStoreName = storeName.strip();
        return storeRepository.findByStoreNameAndIsNotDeleted(normalizedStoreName);
    }
}
