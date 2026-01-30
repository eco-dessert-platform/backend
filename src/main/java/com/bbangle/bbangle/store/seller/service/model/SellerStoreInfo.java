package com.bbangle.bbangle.store.seller.service.model;

import com.bbangle.bbangle.store.domain.Store;
import lombok.Builder;


public class SellerStoreInfo {

    public record StoreInfo(
        Long id,
        String name
    ) {

        @Builder
        public StoreInfo(
            Long id,
            String name
        ) {
            this.id = id;
            this.name = name;
        }

        public static StoreInfo from(Store store) {
            return StoreInfo.builder()
                .id(store.getId())
                .name(store.getName())
                .build();
        }
    }
}
