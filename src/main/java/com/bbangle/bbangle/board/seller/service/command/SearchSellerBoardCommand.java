package com.bbangle.bbangle.board.seller.service.command;

import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.MainCategory;
import com.bbangle.bbangle.board.domain.SaleStatus;
import com.bbangle.bbangle.board.domain.SortType;

public record SearchSellerBoardCommand(
    Long sellerId,
    SaleStatus saleStatus,
    MainCategory mainCategory,
    Category category,
    String keyword,
    SortType sortBy,
    int page,
    int size
) {

}
