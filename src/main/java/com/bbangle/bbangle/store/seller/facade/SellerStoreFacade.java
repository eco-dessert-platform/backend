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

// TODO : Seller/Seller/Facade에서 이동하였으므로 테스트 코드 수정하기
@Slf4j
@Service
@RequiredArgsConstructor
public class SellerStoreFacade {

    private static final String SELLER_IMAGE_FOLDER = "seller-images";
    private final S3Service s3Service;
    private final SellerStoreService sellerStoreService;
    private final SellerService sellerService;
    private final SellerStoreMapper sellerStoreMapper;

    // TODO : Test
    public StoreRegisterResult registerStoreForSeller(
        Long sellerId,
        StoreRequest.StoreCreateRequest request,
        MultipartFile profileImage
    ) {
        Seller seller = sellerService.getSellerById(sellerId);
        if (seller.getCertificationStatus() == CertificationStatus.APPROVED || seller.getCertificationStatus() == CertificationStatus.PENDING)
            throw new BbangleException(BbangleErrorCode.ALREADY_REGISTER_STORE);

        String profileImagePath = s3Service.saveAndReturnWithCdn(SELLER_IMAGE_FOLDER, profileImage);
        // String profileImagePath = "https://www.figma.com/design/us0vz52fFMfDRgBuIPDWhx/-Seller--Design?node-id=16124-3399&m=dev";

        try {
            Store store = sellerStoreService.findStore(request, profileImagePath);

            if (store.getStatus() != StoreStatus.NONE) throw new BbangleException(BbangleErrorCode.ALREADY_RESERVED_STORE);

            sellerStoreService.registerStore(seller, store);

            return StoreResponse.StoreRegisterResult.builder()
                .sellerId(seller.getId())
                .store(sellerStoreMapper.toSellerStoreDetail(store))
                .build();

        } catch (Exception e) {
            log.error(e.getMessage());
            log.error("Seller 생성 실패로 인한 S3 이미지 롤백: {}", profileImagePath);
            s3Service.deleteImage(profileImagePath);
            throw new BbangleException(BbangleErrorCode.SELLER_CREATION_FAILED);
        }
    }
}
