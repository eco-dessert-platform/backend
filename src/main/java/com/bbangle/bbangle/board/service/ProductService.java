package com.bbangle.bbangle.board.service;

import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.dto.product.ProductOrderResponseBase;
import com.bbangle.bbangle.board.service.container.ProductOrderDtos;
import com.bbangle.bbangle.board.dto.ProductResponse;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.board.service.container.Products;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.push.domain.Push;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public ProductResponse getProductResponseWithPush(long memberId, Long boardId) {
        List<Product> products = productRepository.findByBoardId(boardId);

        if (products.isEmpty()) {
            throw new BbangleException(BbangleErrorCode.BOARD_NOT_FOUND);
        }

        Products productList = Products.fromList(products);

        List<Long> productIds = productList.getIds();
        boolean isBundled = productList.getIsBundled();
        boolean isSoldOut = productList.getIsSoldOut();

        Map<Long, Push> pushMap = productRepository.findPushByProductIds(productIds, memberId);

        List<ProductOrderResponseBase> productOrderResponses = ProductOrderDtos.of(productList, pushMap)
            .convertToProductOrderResponse();

        return ProductResponse.builder()
            .products(productOrderResponses)
            .isSoldOut(isSoldOut)
            .boardIsBundled(isBundled)
            .build();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductResponse(Long boardId) {
        List<Product> products = productRepository.findByBoardId(boardId);

        if (products.isEmpty()) {
            throw new BbangleException(BbangleErrorCode.BOARD_NOT_FOUND);
        }

        Products productList = Products.fromList(products);

        boolean isBundled = productList.getIsBundled();
        boolean isSoldOut = productList.getIsSoldOut();

        List<ProductOrderResponseBase> productOrderResponses = ProductOrderDtos.of(productList)
            .convertToProductOrderNonMemberResponse();

        return ProductResponse.builder()
            .products(productOrderResponses)
            .isSoldOut(isSoldOut)
            .boardIsBundled(isBundled)
            .build();
    }
}
