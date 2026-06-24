package com.bbangle.bbangle.cart.domain;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.common.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Set;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "cart_item",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"cart_id", "item_id"}
        )
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem extends BaseEntity {

    @OneToMany(
        mappedBy = "cartItem",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private final List<CartOption> options = new ArrayList<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Board item;

    private String request;

    @Builder
    private CartItem(
        Cart cart,
        Board item
    ){
        this.cart = cart;
        this.item = item;
        this.request = null;
    }

    public static CartItem create(
        Cart cart,
        Board item
    ) {
        return CartItem.builder()
            .cart(cart)
            .item(item)
            .build();
    }

    public void changeRequest(String request) {
        this.request = request;
    }

    public void removeOptions(Set<Long> optionIds) {
        options.removeIf(option -> optionIds.contains(option.getId()));
    }

    public boolean hasNoOptions() {
        return options.isEmpty();
    }
}
