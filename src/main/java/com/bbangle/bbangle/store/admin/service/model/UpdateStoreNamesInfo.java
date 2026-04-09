package com.bbangle.bbangle.store.admin.service.model;

import com.bbangle.bbangle.store.domain.StoreNameRequest;
import java.time.LocalDateTime;
import lombok.Builder;

public class UpdateStoreNamesInfo {

    @Builder
    public record UpdateStoreNames(
        Long storeId,
        String currentName,
        String newName,
        LocalDateTime createdAt
    ) {

        public static UpdateStoreNames from(StoreNameRequest storeNameRequest) {
            return UpdateStoreNames.builder()
                .storeId(storeNameRequest.getStore().getId())
                .currentName(storeNameRequest.getCurrentName())
                .newName(storeNameRequest.getNewName())
                .createdAt(storeNameRequest.getCreatedAt())
                .build();
        }
    }
}
