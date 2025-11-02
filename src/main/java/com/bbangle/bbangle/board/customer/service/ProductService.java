package com.bbangle.bbangle.board.customer.service;

import com.bbangle.bbangle.board.customer.dto.orders.ProductDtoAtBoardDetail;
import com.bbangle.bbangle.board.customer.dto.orders.ProductResponse;
import com.bbangle.bbangle.board.customer.dto.orders.abstracts.ProductOrderResponseBase;
import com.bbangle.bbangle.board.customer.service.factory.ProductOrderFactory;
import com.bbangle.bbangle.board.customer.service.helper.ProductDtoAtBoardDetailsHelper;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.push.domain.Push;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// new로 API 변경 시 삭제 예정
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private static final int ONE_CATEGORY = 1;
    private final ProductRepository productRepository;


    public ProductResponse getProductResponseWithPush(long memberId, Long boardId) {
        List<Product> products = productRepository.findByBoardId(boardId);

        if (products.isEmpty()) {
            throw new BbangleException(BbangleErrorCode.BOARD_NOT_FOUND);
        }

        List<Long> productIds = getIds(products);
        boolean isBundled = getIsBundled(products);
        boolean isSoldOut = getIsSoldOut(products);

        Map<Long, Push> pushMap = productRepository.findPushByProductIds(productIds, memberId);

        List<ProductDtoAtBoardDetail> productOrderResponses = ProductDtoAtBoardDetailsHelper.getDtoList(
            products, pushMap);
        List<ProductOrderResponseBase> responseBases = productOrderResponses.stream()
            .map(productDto -> ProductOrderFactory.resolve(productDto, memberId))
            .toList();

        return new ProductResponse(
            responseBases,
            isBundled,
            isSoldOut
        );
    }

    public ProductResponse getProductResponse(Long boardId) {
        List<Product> products = productRepository.findByBoardId(boardId);

        if (products.isEmpty()) {
            throw new BbangleException(BbangleErrorCode.BOARD_NOT_FOUND);
        }

        boolean isBundled = getIsBundled(products);
        boolean isSoldOut = getIsSoldOut(products);

        List<ProductDtoAtBoardDetail> productOrderResponses = ProductDtoAtBoardDetailsHelper.getDtoList(
            products);
        List<ProductOrderResponseBase> responseBases = productOrderResponses.stream()
            .map(ProductOrderFactory::resolve)
            .toList();

        return new ProductResponse(
            responseBases,
            isBundled,
            isSoldOut
        );
    }

    private static List<Long> getIds(List<Product> products) {
        return products.stream()
            .map(Product::getId)
            .toList();
    }

    private static boolean getIsSoldOut(List<Product> products) {
        return products.stream()
            .noneMatch(product -> !product.isSoldout());
    }

    private static boolean getIsBundled(List<Product> products) {
        return products.stream()
            .map(Product::getCategory)
            .distinct()
            .count() > ONE_CATEGORY;
    }
}
