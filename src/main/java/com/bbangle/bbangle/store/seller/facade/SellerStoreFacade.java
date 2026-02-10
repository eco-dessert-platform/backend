package com.bbangle.bbangle.store.seller.facade;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.image.customer.service.S3Service;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.seller.seller.service.SellerService;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreStatus;
import com.bbangle.bbangle.store.seller.controller.dto.StoreRequest;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.StoreRegisterResult;
import com.bbangle.bbangle.store.seller.controller.mapper.SellerStoreMapper;
import com.bbangle.bbangle.store.seller.service.SellerStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerStoreFacade {

    private static final String SELLER_IMAGE_FOLDER = "seller-images";
    private final S3Service s3Service;
    private final SellerStoreService sellerStoreService;
    private final SellerService sellerService;
    private final SellerStoreMapper sellerStoreMapper;

    public StoreRegisterResult registerStoreForSeller(
        Long sellerId,
        StoreRequest.StoreCreateRequest request,
        MultipartFile profileImage
    ) {
        // 이미지 파일과 기존 스토어의 profile 경로 둘 다 없을 경우 예외 던짐
        if ((request.profile() == null ||  request.profile().isBlank()) && profileImage == null) {
            throw new BbangleException(BbangleErrorCode.INVALID_PROFILE);
        }

        // 판매자 계정 조회 -> 만약 판매자 계정이 [승인 / 대기] 상태일 경우 등록 불가
        Seller seller = sellerService.getSellerById(sellerId);
        if (seller.getCertificationStatus() == CertificationStatus.APPROVED || seller.getCertificationStatus() == CertificationStatus.PENDING) {
            throw new BbangleException(BbangleErrorCode.ALREADY_REGISTER_STORE);
        }

        // 만약 이미지 파일이 존재할 경우 S3에 이미지 파일 업로드
        String profileImagePath = null;
        if (profileImage != null) profileImagePath = s3Service.saveAndReturnWithCdn(
            SELLER_IMAGE_FOLDER + "/" + seller.getId(), profileImage);

        try {
            Store store = sellerStoreService.createStore(request, profileImagePath);

            // 스토어 상태가 비선점 상태가 아닐 경우 누군가 등록한 상태이므로 예외 던짐
            if (store.getStatus() != StoreStatus.NONE) throw new BbangleException(BbangleErrorCode.ALREADY_RESERVED_STORE);

            sellerStoreService.registerStore(seller, store);

            return StoreResponse.StoreRegisterResult.builder()
                .sellerId(seller.getId())
                .store(sellerStoreMapper.toSellerStoreDetail(store))
                .build();

        } catch (BbangleException e) {
            // BbangleException일 경우 s3에 업로드한 이미지 파일을 삭제 후 BbangleException을 다시 던짐
            if (profileImagePath != null) {
                log.warn("Seller 생성 실패로 인한 S3 이미지 롤백: {}", profileImagePath);
                s3Service.deleteImage(profileImagePath);
            }

            throw e;
        } catch (Exception e) {
            // BbangleException 이외의 예상치 못한 예외일 경우 s3에 업로드한 이미지 파일을 삭제 후 BbangleException을 새로 던짐
            log.error(e.getMessage(), e);

            if (profileImagePath != null) {
                log.error("Seller 생성 실패로 인한 S3 이미지 롤백: {}", profileImagePath);
                s3Service.deleteImage(profileImagePath);
            }

            throw new BbangleException(BbangleErrorCode.SELLER_CREATION_FAILED);
        }
    }
}
