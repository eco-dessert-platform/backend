package com.bbangle.bbangle.store.controller.dto;

public class StoreResponse {

        public record StoreDetail(
            Long storeId,
            String profile,
            String storeName,
            String introduce,
            Boolean isWished
        ) {
        }

}
