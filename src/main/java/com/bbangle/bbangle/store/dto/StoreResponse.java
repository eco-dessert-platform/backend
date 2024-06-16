package com.bbangle.bbangle.store.dto;

import lombok.Builder;

@Builder
public record StoreResponse(
    Long storeId,
    String storeProfile,
    String storeTitle,
    String storeIntroduce,
    Boolean isWished
) {

}