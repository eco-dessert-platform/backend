package com.bbangle.bbangle.board.seller.controller.dto.request;

import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.MainCategory;
import com.bbangle.bbangle.board.domain.SaleStatus;
import com.bbangle.bbangle.board.domain.SortType;
import com.bbangle.bbangle.board.seller.service.command.SearchSellerBoardCommand;

public class ProductBoardRequest {

    public record ProductBoardSearchRequest(
        SaleStatus saleStatus,
        MainCategory mainCategory,
        Category category,
        String keyword,
        int page,
        int size,
        SortType sortBy
    ) {

        public SearchSellerBoardCommand toCommand(Long sellerId) {
            return new SearchSellerBoardCommand(
                sellerId,
                saleStatus,
                mainCategory,
                category,
                keyword,
                sortBy != null ? sortBy : SortType.LATEST,
                page,
                size > 0 ? size : 10
            );
        }
    }
}
