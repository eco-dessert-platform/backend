package com.bbangle.bbangle.seller.seller.facade;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.image.customer.service.S3Service;
import com.bbangle.bbangle.seller.seller.service.SellerService;
import com.bbangle.bbangle.seller.seller.service.command.SellerCreateCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerFacade {

    private final SellerService sellerService;
    private final S3Service s3Service;

    private static final String SELLER_IMAGE_FOLDER = "seller-images";

    public void registerSeller(SellerCreateCommand command, MultipartFile profileImage, Long storeId) {
        // 1. S3에 이미지 업로드 (먼저 수행)
        String profileImagePath = s3Service.saveAndReturnWithCdn(SELLER_IMAGE_FOLDER, profileImage);
        try {
            // 2. 셀러 생성 (DB 작업)
            sellerService.createSeller(command, profileImagePath, storeId);
        } catch (Exception e) {
            // 3. [보상 트랜잭션] DB 저장 실패 시, S3에 올라간 이미지 삭제
            log.error("Seller 생성 실패로 인한 S3 이미지 롤백: {}", profileImagePath);
            s3Service.deleteImage(profileImagePath);
            // 4. 예외를 다시 던져서 Transactional이 동작하게 함
            throw new BbangleException(BbangleErrorCode.SELLER_CREATION_FAILED);
        }
    }
}
