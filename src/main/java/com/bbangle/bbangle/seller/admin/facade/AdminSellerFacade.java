package com.bbangle.bbangle.seller.admin.facade;

import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplication;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplication.SellerDTO;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplication.SellerStoreDTO;
import com.bbangle.bbangle.seller.admin.service.AdminSellerService;
import com.bbangle.bbangle.seller.admin.service.model.AdminSellerInfo;
import com.bbangle.bbangle.seller.admin.service.model.AdminSellerInfo.SellerApplicationInfoList;
import com.bbangle.bbangle.util.AesEncryptionUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminSellerFacade {

    private final AdminSellerService adminSellerService;
    private final AesEncryptionUtil aesEncryptionUtil;

    public AdminSellerResponse.AdminSellerApplicationList getAdminSellerApplicationList(int page) {

        SellerApplicationInfoList raw = adminSellerService.getAdminSellerApplicationList(page);

        List<AdminSellerApplication> content = raw.sellerApplicationInfoList().stream()
            .map(this::toResponse)
            .toList();

        return AdminSellerResponse.AdminSellerApplicationList.builder()
            .adminSellerApplicationList(content)
            .totalElements(raw.totalElements())
            .totalPages(raw.totalPages())
            .hasPrevious(raw.hasPrevious())
            .hasNext(raw.hasNext())
            .build();
    }

    private AdminSellerResponse.AdminSellerApplication toResponse(
        AdminSellerInfo.SellerApplicationInfoList.SellerApplicationInfo raw
    ) {
        String decryptedAccountNumber = aesEncryptionUtil.decrypt(raw.sellerInfo().accountNumber());

        return AdminSellerApplication.builder()
            .storeApplicationId(raw.storeApplicationId())
            .sellerStoreDTO(
                SellerStoreDTO.builder()
                    .storeName(raw.sellerStoreInfo().storeName())
                    .phone(raw.sellerStoreInfo().phone())
                    .subPhone(raw.sellerStoreInfo().subPhone())
                    .email(raw.sellerStoreInfo().email())
                    .originAddressLine(raw.sellerStoreInfo().originAddressLine())
                    .originAddressDetail(raw.sellerStoreInfo().originAddressDetail())
                    .build()
            )
            .sellerDTO(
                SellerDTO.builder()
                    .sellerId(raw.sellerInfo().sellerId())
                    .bankCode(raw.sellerInfo().bankCode())
                    .accountHolder(raw.sellerInfo().accountHolder())
                    .accountNumber(decryptedAccountNumber)
                    .createdAt(raw.sellerInfo().createdAt())
                    .build()
            )
            .build();
    }
}
