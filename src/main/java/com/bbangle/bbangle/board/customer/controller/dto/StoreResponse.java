package com.bbangle.bbangle.board.customer.controller.dto;

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
