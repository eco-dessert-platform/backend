package com.bbangle.bbangle.board.seller.service.info;

import com.bbangle.bbangle.board.domain.SaleStatus;
import java.util.Map;
import org.springframework.data.domain.Page;

public record SellerBoardListInfo(
    Page<SellerBoardItemInfo> boards,
    Map<SaleStatus, Long> tabCounts
) {

}
