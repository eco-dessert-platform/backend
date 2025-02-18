package com.bbangle.bbangle.board.domain;

import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.store.domain.Store;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "product_board")
@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
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

    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products = new ArrayList<>();

    public Board updateProfile(String profile) {
        this.profile = profile;
        return this;
    }

    public void addProducts(List<Product> products) {
        this.products.addAll(products);
        products.forEach(product -> product.setBoard(this));  // 양방향 관계 설정
    }

    public Board(Store store, String title, int price, int discountRate,
                 int deliveryFee, Integer freeShippingConditions) {
        validate(price, discountRate, deliveryFee);

        this.store = store;
        this.title = title;
        this.price = price;
        this.discountRate = discountRate;
        this.deliveryFee = deliveryFee;
        this.freeShippingConditions = freeShippingConditions;
        this.isDeleted = false;
    }

    private void validate(int price, int discountRate, int deliveryFee) {
        if (price < 0) {
            throw new BbangleException(BbangleErrorCode.INVALID_BOARD_PRICE);
        }
        if (discountRate < 0 || discountRate > 100) {
            throw new BbangleException(BbangleErrorCode.INVALID_BOARD_DISCOUNT);
        }
        if (deliveryFee < 0) {
            throw new BbangleException(BbangleErrorCode.INVALID_DELIVERY_FEE);
        }
    }
}
