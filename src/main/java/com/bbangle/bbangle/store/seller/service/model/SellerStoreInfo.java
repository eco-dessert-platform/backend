package com.bbangle.bbangle.store.seller.service.model;

import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreStatus;
import java.util.Collection;
import lombok.Builder;
import lombok.Getter;


public class SellerStoreInfo {

    public record StoreInfo(Long id, String name, boolean isDeleted,
                            StoreStatus status) {

        @Builder
        public StoreInfo(Long id, String name,
            boolean isDeleted, StoreStatus status) {
            this.id = id;
            this.name = name;
            this.isDeleted = isDeleted;
            this.status = status;
        }

        public static StoreInfo from(Store store) {
            return StoreInfo.builder()
                .id(store.getId())
                .name(store.getName())
                .isDeleted(store.isDeleted())
                .status(store.getStatus())
                .build();
        }

    }

}
