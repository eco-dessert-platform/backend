package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.customer.dto.AiLearningProductDto;
import com.bbangle.bbangle.board.customer.dto.TitleDto;
import com.bbangle.bbangle.board.customer.dto.orders.ProductDtoAtBoardDetail;
import com.bbangle.bbangle.push.domain.Push;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProductQueryDSLRepository {

    Map<Long, Set<Category>> getCategoryInfoByBoardId(List<Long> boardIds);

    List<TitleDto> findAllTitle();

    List<Product> findByBoardId(Long boardId);

    Map<Long, Push> findPushByProductIds(List<Long> productIds, Long memberId);

    List<ProductDtoAtBoardDetail> findProductDtoById(Long memberId, Long boardId);

    List<AiLearningProductDto> findAiLearningData();
}
