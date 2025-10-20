package com.bbangle.bbangle.seller.service;

import com.bbangle.bbangle.seller.controller.dto.SellerAccountUpdateRequest_v2;
import com.bbangle.bbangle.seller.controller.dto.SellerStoreNameUpdateRequest_v2;
import com.bbangle.bbangle.seller.controller.dto.SellerUpdateRequest_v2;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SellerService_v2 {

    public void updateSeller(SellerUpdateRequest_v2 request, Long sellerId) {
        // TODO: 실제 비즈니스 로직 구현
    }

    public void updateStoreName(SellerStoreNameUpdateRequest_v2 request, Long sellerId) {

        // TODO: 실제 비즈니스 로직 구현

    }

    public void updateAccount(SellerAccountUpdateRequest_v2 request, Long sellerId) {

        // TODO: 실제 비즈니스 로직 구현

    }
}
