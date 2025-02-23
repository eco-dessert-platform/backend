package com.bbangle.bbangle.board.domain;

import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.store.domain.Store;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Objects;

@Table(name = "product_board")
@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Store store;

    @Column(name = "title")
    private String title;

    @Column(name = "price")
    private int price;

    @Column(name = "discount_rate")
    private int discountRate;

    @Column(name = "is_soldout", columnDefinition = "tinyint")
    private Boolean status;

    @Column(name = "profile")
    private String profile;

    @Column(name = "purchase_url")
    private String purchaseUrl;

    @Column(name = "delivery_fee")
    private Integer deliveryFee;

    @Column(name = "free_shipping_conditions")
    private Integer freeShippingConditions;

    @Column(name = "is_deleted", columnDefinition = "tinyint")
    private boolean isDeleted;

    @OneToOne(mappedBy = "board", fetch = FetchType.LAZY) // Board가 더 많이 호출되므로 연관관계 주인을 board로 하는게 더 적합해 보임
    private BoardStatistic boardStatistic;

    @OneToMany(mappedBy = "board")
    private List<Product> products;

    public Board updateProfile(String profile) {
        this.profile = profile;
        return this;
    }

    public List<String> getTags() {
        return products.stream()
            .map(Product::getTags)
            .flatMap(List::stream)
            .distinct()
            .toList();
    }

    public boolean isSoldOut() {
        return products.stream().allMatch(Product::isSoldout);
    }

    public boolean isBbangketing() {
        return products.stream().allMatch(product -> Objects.nonNull(product.getOrderStartDate()));
    }

    public boolean isBundled() {
        return products.stream().map(Product::getCategory)
            .distinct()
            .count() > 1;
    }

}
