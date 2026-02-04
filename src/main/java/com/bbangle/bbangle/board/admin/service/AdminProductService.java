package com.bbangle.bbangle.board.admin.service;

import com.bbangle.bbangle.board.domain.EditStockFlag;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AdminProductService {
    private final ProductRepository productRepository;

    @Transactional
    public void editProductStock(Long productId, int amount, EditStockFlag editStockFlag) {
        // Pessimistic Lock으로 조회하여 동시성 제어
        Product product = productRepository.findWithLockById(productId).orElseThrow(
            () -> new BbangleException(BbangleErrorCode.NOT_FOUND_OPTION)
        );
        product.editStock(amount, editStockFlag);
    }

}
