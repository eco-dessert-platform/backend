package com.bbangle.bbangle.board.customer.dto.orders.abstracts;

import com.bbangle.bbangle.board.domain.OrderTypeEnum;
import com.bbangle.bbangle.board.customer.dto.Nutrient;
import com.bbangle.bbangle.board.customer.dto.orders.ProductDtoAtBoardDetail;
import lombok.Getter;

@Getter
public abstract class ProductOrderResponseBase {

    protected Long id;
    protected String title;
    protected Boolean glutenFreeTag;
    protected Boolean highProteinTag;
    protected Boolean sugarFreeTag;
    protected Boolean veganTag;
    protected Boolean ketogenicTag;
    protected Integer price;
    protected Nutrient nutrient;
    protected OrderTypeEnum orderType;
    protected Boolean isSoldout;

    protected ProductOrderResponseBase(ProductDtoAtBoardDetail product) {
        this.nutrient = Nutrient.from(product);
        this.id = product.getId();
        this.title = product.getTitle();
        this.price = product.getPrice();
        this.glutenFreeTag = product.getGlutenFreeTag();
        this.highProteinTag = product.getHighProteinTag();
        this.sugarFreeTag = product.getSugarFreeTag();
        this.veganTag = product.getVeganTag();
        this.ketogenicTag = product.getKetogenicTag();
    }
}
