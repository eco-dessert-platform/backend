package com.bbangle.bbangle.cart.domain;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.member.domain.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    name = "cart",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"member_id", "item_id"}
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
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Board item;

    private String request;

    @Builder
    private CartItem(
        Member member,
        Board item
    ){
        this.member = member;
        this.item = item;
        this.request = null;
    }

    public static CartItem create(
        Member member,
        Board item
    ) {
        return CartItem.builder()
            .member(member)
            .item(item)
            .build();
    }

    // TODO : Test
    public void addOption(Product option, int quantity) {
        CartOption cartOption = CartOption.create(this, option, quantity);
        this.options.add(cartOption);
    }

    public void changeRequest(String request) {
        this.request = request;
    }
}
