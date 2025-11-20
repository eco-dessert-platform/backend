package com.bbangle.bbangle.seller.seller.facade;

import com.bbangle.bbangle.image.customer.service.S3Service;
import com.bbangle.bbangle.seller.seller.service.SellerService;
import com.bbangle.bbangle.seller.seller.service.command.SellerCreateCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class SellerFacade {

    private final SellerService sellerService;
    private final S3Service s3Service;

    private static final String SElLER_IMAGE_FOLDER = "seller-images";

    public void registerSeller(SellerCreateCommand command, MultipartFile profileImage, Long storeId) {
        // 1. S3에 이미지 업로드
        // 2. 셀러 생성
        String profileImagePath = s3Service.saveAndReturnWithCdn(SElLER_IMAGE_FOLDER,profileImage);
        sellerService.createSeller(command, profileImagePath, storeId);
    }
}
