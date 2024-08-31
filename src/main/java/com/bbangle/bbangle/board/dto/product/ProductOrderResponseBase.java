package com.bbangle.bbangle.board.dto.product;

import com.bbangle.bbangle.board.dto.ProductOrderDto;

public abstract class ProductOrderResponseBase implements IProductOrder {

    protected ProductOrderDto product;

    protected ProductOrderResponseBase(ProductOrderDto product) {
        this.product = product;
    }

    @Override
    public ProductOrderResponse getProductOrderResponse() {
        return getOrderType();
    }

    protected abstract ProductOrderResponse getOrderType();
}
