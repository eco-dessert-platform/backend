package com.bbangle.bbangle.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public final class StoreResponseDto {

    private final Long storeId;
    private final String storeName;
    private final String introduce;
    private final String profile;
    private Boolean isWished;
    
    public StoreResponseDto(Long storeId, String storeName, String introduce, String profile) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.introduce = introduce;
        this.profile = profile;
        this.isWished = false;
    }

    public void isWishStore() {
        this.isWished = true;
    }

}
