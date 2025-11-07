package com.bbangle.bbangle.board.domain;

import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "product_info_notice_id")
    private ProductInfoNotice productInfoNotice;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_detail_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private BoardDetail boardDetail;

    @Column(name = "title", length = 50)
    private String title;

    @Column(name = "price")
    private int price;

    @Column(name = "is_soldout", columnDefinition = "tinyint(1)")
    private Boolean status;

    @Column(name = "purchase_url", columnDefinition = "varchar(255)")
    private String purchaseUrl;

    @Column(name = "view")
    private int view;

    @Column(name = "wish_cnt")
    private int wish_cnt;

    @Column(name = "discount_rate")
    private int discountRate;

    @Column(name = "discount_price")
    private Integer discountPrice;

    @Column(name = "is_deleted", columnDefinition = "tinyint(1)", nullable = false)
    private boolean isDeleted;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
    private List<ProductImg> productImgs = new ArrayList<>();

    @OneToOne(mappedBy = "board", cascade = CascadeType.ALL)
    // Board가 더 많이 호출되므로 연관관계 주인을 board로 하는게 더 적합해 보임
    private BoardStatistic boardStatistic;

    public void addProducts(List<Product> products) {
        this.products.addAll(products);
        products.forEach(product -> product.setBoard(this));
    }

    public void addBoardDetails(BoardDetail boardDetail) {
        this.boardDetail = boardDetail;
        boardDetail.updateBoard(this);
    }

    public Board(Store store, String title, int price, int discountRate,
                 int discountPrice, ProductInfoNotice productInfoNotice) {
//        validate(price, discountRate, deliveryFee);

        this.store = store;
        this.title = title;
        this.price = price;
        this.discountRate = discountRate;
        this.discountPrice = discountPrice;
        this.isDeleted = false;
        this.productInfoNotice = productInfoNotice;
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

//    public List<String> getTags() {
//        return products.stream()
//            .map(Product::getTags)
//            .flatMap(List::stream)
//            .distinct()
//            .toList();
//    }

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

    public String getThumbnail() {
        return productImgs.stream()
            .filter(ProductImg::isThumbnail)
            .findFirst()
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.BOARD_WITH_IMAGE_NOTFOUND))
            .getUrl();
    }

    public List<String> getSupportingImages() {
        return productImgs.stream()
            .filter(img -> !img.isThumbnail())
            .map(ProductImg::getUrl)
            .toList();
    }
}
