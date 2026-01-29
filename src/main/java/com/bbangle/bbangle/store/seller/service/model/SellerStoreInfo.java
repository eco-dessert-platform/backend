package com.bbangle.bbangle.store.seller.service.model;

import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreStatus;
import lombok.Builder;


public class SellerStoreInfo {

    public record StoreInfo(
        Long id,
        String name,
        StoreStatus status
    ) {

        @Builder
        public StoreInfo(
            Long id,
            String name,
            StoreStatus status
        ) {
            this.id = id;
            this.name = name;
            this.status = status;
        }

        public static StoreInfo from(Store store) {
            return StoreInfo.builder()
                .id(store.getId())
                .name(store.getName())
                .status(store.getStatus())
                .build();
        }
    }
}
