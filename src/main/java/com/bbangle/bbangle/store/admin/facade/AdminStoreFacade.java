package com.bbangle.bbangle.store.admin.facade;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.image.customer.service.S3Service;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreRequest.StoreDetailRequest;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.StoreDetailResponse;
import com.bbangle.bbangle.store.admin.controller.mapper.AdminStoreMapper;
import com.bbangle.bbangle.store.admin.service.AdminStoreService;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.seller.service.SellerStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

// TODO : Test
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminStoreFacade {

    private static final String STORE_IMAGE_FOLDER = "store";
    private final S3Service s3Service;
    private final AdminStoreService adminStoreService;
    private final SellerStoreService sellerStoreService;
    private final AdminStoreMapper adminStoreMapper;

    @Transactional
    public StoreDetailResponse createStoreForAdmin(
        StoreDetailRequest request,
        MultipartFile profileImage
    ) {
        // Store Name이 중복되는지 검증
        if (sellerStoreService.findStoreByStoreName(request.storeName()).isPresent()) {
            throw new BbangleException(BbangleErrorCode.INVALID_STORE_NAME);
        }

        // Profile 사진을 업로드 했는지 검증
        if (profileImage == null) {
            throw new BbangleException(BbangleErrorCode.INVALID_PROFILE);
        }

        // Store Profile 업로드
        String profileImagePath = s3Service.saveAndReturnWithCdn(STORE_IMAGE_FOLDER + "/" + request.identifier(), profileImage);

        try {
            Store store = adminStoreService.createStore(adminStoreMapper.toAdminStoreInfo(request, profileImagePath));
            return adminStoreMapper.toStoreDetailResponse(store);
        } catch (BbangleException e) {
            if (profileImagePath != null) {
                log.warn("Store 생성 실패로 인한 S3 이미지 롤백: {}", profileImagePath);
                s3Service.deleteImage(profileImagePath);
            }
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);

            if (profileImagePath != null) {
                log.error("Store 생성 실패로 인한 S3 이미지 롤백: {}", profileImagePath);
                s3Service.deleteImage(profileImagePath);
            }

            throw new BbangleException(BbangleErrorCode.STORE_CREATION_FAILED);
        }
    }

    @Transactional
    public StoreDetailResponse updateStoreForAdmin(StoreDetailRequest request, long storeId) {

        // Store Name이 중복되는지 검증
        if (sellerStoreService.findStoreByStoreName(request.storeName()).isPresent()) {
            throw new BbangleException(BbangleErrorCode.INVALID_STORE_NAME);
        }

        Store store = sellerStoreService.findStore(storeId);
        Store updateStore = adminStoreService.updateStore(
            adminStoreMapper.toAdminStoreInfo(request, null),
            store
        );

        return adminStoreMapper.toStoreDetailResponse(updateStore);
    }
}
