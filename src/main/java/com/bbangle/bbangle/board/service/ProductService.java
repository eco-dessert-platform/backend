package com.bbangle.bbangle.board.service;

import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.dto.orders.ProductDtoAtBoardDetail;
import com.bbangle.bbangle.board.dto.orders.abstracts.ProductOrderResponseBase;
import com.bbangle.bbangle.board.service.factory.ProductOrderFactory;
import com.bbangle.bbangle.board.service.helper.ProductDtoAtBoardDetailsHelper;
import com.bbangle.bbangle.board.dto.orders.ProductResponse;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.board.service.util.ProductsUtil;
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

        List<Long> productIds = ProductsUtil.getIds(products);
        boolean isBundled = ProductsUtil.getIsBundled(products);
        boolean isSoldOut = ProductsUtil.getIsSoldOut(products);

        Map<Long, Push> pushMap = productRepository.findPushByProductIds(productIds, memberId);

        List<ProductDtoAtBoardDetail> productOrderResponses = ProductDtoAtBoardDetailsHelper.getDtoList(products, pushMap);
        List<ProductOrderResponseBase> responseBases = productOrderResponses.stream()
            .map(productDto -> ProductOrderFactory.resolve(productDto, memberId))
            .toList();

        return new ProductResponse(
            responseBases,
            isBundled,
            isSoldOut
        );
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductResponse(Long boardId) {
        List<Product> products = productRepository.findByBoardId(boardId);

        if (products.isEmpty()) {
            throw new BbangleException(BbangleErrorCode.BOARD_NOT_FOUND);
        }

        boolean isBundled = ProductsUtil.getIsBundled(products);
        boolean isSoldOut = ProductsUtil.getIsSoldOut(products);

        List<ProductDtoAtBoardDetail> productOrderResponses = ProductDtoAtBoardDetailsHelper.getDtoList(products);
        List<ProductOrderResponseBase> responseBases = productOrderResponses.stream()
            .map(ProductOrderFactory::resolve)
            .toList();

        return new ProductResponse(
            responseBases,
            isBundled,
            isSoldOut
        );
    }
}
