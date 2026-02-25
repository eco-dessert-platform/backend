package com.bbangle.bbangle.store.seller.facade;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.image.customer.service.S3Service;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.seller.seller.service.SellerService;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreApplication;
import com.bbangle.bbangle.store.seller.controller.dto.StoreApplicationRequest.StoreApplicationCreateRequest;
import com.bbangle.bbangle.store.seller.controller.dto.StoreApplicationResponse.StoreApplicationDetail;
import com.bbangle.bbangle.store.seller.controller.mapper.SellerStoreApplicationMapper;
import com.bbangle.bbangle.store.seller.service.SellerStoreApplicationService;
import com.bbangle.bbangle.store.seller.service.SellerStoreService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

// TODO : 테스트
@Slf4j
@Service
@RequiredArgsConstructor
public class SellerStoreApplicationFacade {

    private static final String SELLER_IMAGE_FOLDER = "seller-images";
    private final S3Service s3Service;
    private final SellerService sellerService;
    private final SellerStoreService sellerStoreService;
    private final SellerStoreApplicationService sellerStoreApplicationService;
    private final SellerStoreApplicationMapper sellerStoreApplicationMapper;

    public StoreApplicationDetail registerStoreForSeller(
        Long sellerId,
        StoreApplicationCreateRequest request,
        MultipartFile profileImage
    ) {
        if ((request.profile() == null ||  request.profile().isBlank()) && profileImage == null) {
            throw new BbangleException(BbangleErrorCode.INVALID_PROFILE);
        }

        // 판매자 계정 조회 -> 만약 판매자 계정이 [승인 / 대기] 상태일 경우 등록 불가
        Seller seller = sellerService.getSellerById(sellerId);
        if (!seller.isRegisterAvailable()) {
            throw new BbangleException(BbangleErrorCode.ALREADY_REGISTER_STORE);
        }

        // Request의 StoreId가 null인데 Store 테이블에 해당 이름이 존재할 경우
        if (request.storeId() == null && sellerStoreService.checkStoreName(request.storeName()).isPresent()) {
            throw new BbangleException(BbangleErrorCode.INVALID_STORE_NAME);
        }

        // 해당 스토어를 등록한 판매자가 있을 경우 등록 불가
        if (request.storeId() != null && sellerService.existsSellerByStoreId(request.storeId())) {
            throw new BbangleException(BbangleErrorCode.ALREADY_RESERVED_STORE);
        }

        // request에 storeId가 있을 경우 조회
        Store store = null;
        if (request.storeId() != null) {
            store = sellerStoreService.findStore(request.storeId());
        }

        String profileImagePath = null;
        if (profileImage != null) profileImagePath = s3Service.saveAndReturnWithCdn(
            SELLER_IMAGE_FOLDER + "/" + seller.getId(), profileImage);

        try {
            StoreApplication storeApplication = sellerStoreApplicationService.save(request, profileImagePath, seller, store);
            sellerService.updateSellerStatus(seller, CertificationStatus.PENDING);
            return sellerStoreApplicationMapper.toStoreApplicationDetail(storeApplication);
        } catch (BbangleException e) {
            if (profileImagePath != null) {
                log.warn("Seller 생성 실패로 인한 S3 이미지 롤백: {}", profileImagePath);
                s3Service.deleteImage(profileImagePath);
            }

            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);

            if (profileImagePath != null) {
                log.error("Seller 생성 실패로 인한 S3 이미지 롤백: {}", profileImagePath);
                s3Service.deleteImage(profileImagePath);
            }

            throw new BbangleException(BbangleErrorCode.SELLER_CREATION_FAILED);
        }
    }

    public StoreApplicationDetail findStoreApplication (Long sellerId) {
        Optional<StoreApplication> storeApplication = sellerStoreApplicationService.findStoreApplicationBySellerId(sellerId);
        return storeApplication.map(sellerStoreApplicationMapper::toStoreApplicationDetail).orElse(null);
    }
}
