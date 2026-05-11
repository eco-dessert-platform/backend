package com.bbangle.bbangle.store.admin.service.model;

import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreApplication;
import lombok.Builder;

@Builder
public record RegisterApproveResult(
    StoreApplication storeApplication,
    Store store,
    Seller seller
) {}