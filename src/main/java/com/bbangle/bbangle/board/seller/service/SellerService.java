package com.bbangle.bbangle.board.seller.service;

import com.bbangle.bbangle.board.seller.controller.dto.SellerRequest.SellerAccountUpdateRequest;
import com.bbangle.bbangle.board.seller.controller.dto.SellerRequest.SellerStoreNameUpdateRequest;
import com.bbangle.bbangle.board.seller.controller.dto.SellerRequest.SellerUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SellerService {

    public void updateSeller(SellerUpdateRequest request, Long sellerId) {
        // TODO: 실제 비즈니스 로직 구현
    }

    public void updateStoreName(SellerStoreNameUpdateRequest request, Long sellerId) {

        // TODO: 실제 비즈니스 로직 구현

    }

    public void updateAccount(SellerAccountUpdateRequest request, Long sellerId) {

        // TODO: 실제 비즈니스 로직 구현

    }
}
