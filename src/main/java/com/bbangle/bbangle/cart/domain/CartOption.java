package com.bbangle.bbangle.cart.domain;

import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(
    name = "cart_option",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"cart_id", "option_id"}
        )
    }
)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private Product option;

    @Column(nullable = false)
    private int quantity;

    @Builder
    private CartOption(
        Cart cart,
        Product option,
        int quantity
    ){
        this.cart = cart;
        this.option = option;
        this.quantity = quantity;
    }

    public static CartOption create(
        Cart cart,
        Product option,
        int quantity
    ) {
        if (quantity < 1) throw new BbangleException(BbangleErrorCode.INVALID_REQUEST_QUANTITY);
        
        return CartOption.builder()
            .cart(cart)
            .option(option)
            .quantity(quantity)
            .build();
    }

    public void updateQuantity(int quantity) {
        if (quantity < 0) throw new BbangleException(BbangleErrorCode.INVALID_REQUEST_QUANTITY);
        this.quantity = quantity;
    }
}
