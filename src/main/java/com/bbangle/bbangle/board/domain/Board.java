package com.bbangle.bbangle.board.domain;

import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.common.domain.SoftDeleteBaseEntity;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.store.domain.Store;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class Board extends SoftDeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Store store;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "product_info_notice_id")
    private ProductInfoNotice productInfoNotice;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "board_detail_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private BoardDetail boardDetail;

    @Column(name = "title")
    private String title;

    @Column(name = "price")
    private int price;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type")
    private DiscountType discountType;

    @Column(name = "discount_value")
    private int discountValue;

    @Column(name = "discount_rate")
    private int discountRate;

    @Column(name = "is_soldout", columnDefinition = "tinyint")
    private Boolean status;

    @Column(name = "purchase_url")
    private String purchaseUrl;

    @Column(name = "delivery_fee")
    private Integer deliveryFee;

    @Column(name = "free_shipping_conditions")
    private Integer freeShippingConditions;

    @Column(name = "view")
    private Integer view;

    @Column(name = "discount_price")
    private int discountPrice;

    @Column(name = "courier")
    private String courier;

    @Column(name = "delivery_condition")
    private String deliveryCondition;

    @Column(name = "is_crawling", columnDefinition = "tinyint")
    private Boolean isCrawling;

    // 신선식품 여부
    @Column(name = "is_fresh", columnDefinition = "tinyint")
    private Boolean isFresh;

    @Enumerated(EnumType.STRING)
    @Column(name = "production_start_time")
    private ProductionStartTime productionStartTime;

    @OneToMany(mappedBy = "board", cascade = CascadeType.PERSIST)
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "board", cascade = CascadeType.PERSIST)
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

    public void addProductImgs(List<ProductImg> productImgs) {
        this.productImgs.addAll(productImgs);
        productImgs.forEach(img -> img.updateBoard(this));
    }

    public static Board sellerCreate(
        Store store,
        String title,
        Integer price,
        String discountType,
        Integer discountValue,
        Integer deliveryFee,
        Integer freeShippingConditions,
        Boolean isFresh,
        String productionStartTime,
        String deliveryCondition,
        String deliveryCompany,
        ProductInfoNotice productInfoNotice,
        BoardDetail boardDetail
    ) {
        DiscountType parsedDiscountType = DiscountType.from(discountType);
        ProductionStartTime parsedProductionStartTime = ProductionStartTime.from(productionStartTime);
        validate(title, price, parsedDiscountType, discountValue, deliveryFee);

        Board board = new Board();
        board.store = store;
        board.title = title;
        board.price = price;
        board.discountType = parsedDiscountType;
        board.discountValue = discountValue;
        board.discountRate = calculateDiscountRate(price, parsedDiscountType, discountValue);
        board.discountPrice = calculateDiscountPrice(price, parsedDiscountType, discountValue);
        board.deliveryFee = deliveryFee;
        board.freeShippingConditions = freeShippingConditions;
        board.isFresh = isFresh;
        board.productionStartTime = parsedProductionStartTime;
        board.deliveryCondition = deliveryCondition;
        board.courier = deliveryCompany;
        board.productInfoNotice = productInfoNotice;
        board.isCrawling = false;
        board.view = 0;
        board.status = false;
        board.addBoardDetails(boardDetail);
        return board;
    }

    private static void validate(String title, int price, DiscountType discountType, Integer discountValue, Integer deliveryFee) {
        if (title == null || title.isBlank()) {
            throw new BbangleException(BbangleErrorCode.INVALID_BOARD_TITLE);
        }
        if (price < 0) {
            throw new BbangleException(BbangleErrorCode.INVALID_BOARD_PRICE);
        }
        if (discountType == DiscountType.RATE && (discountValue < 0 || discountValue > 100)) {
            throw new BbangleException(BbangleErrorCode.INVALID_BOARD_DISCOUNT);
        }
        if (discountType == DiscountType.AMOUNT && (discountValue < 0 || discountValue > price)) {
            throw new BbangleException(BbangleErrorCode.INVALID_BOARD_DISCOUNT);
        }
        if (deliveryFee < 0) {
            throw new BbangleException(BbangleErrorCode.INVALID_DELIVERY_FEE);
        }
    }

    private static int calculateDiscountRate(Integer price, DiscountType discountType, Integer discountValue) {
        if (discountType == DiscountType.RATE) {
            return discountValue;
        }
        if (price == 0) {
            return 0;
        }
        return (discountValue * 100) / price;
    }

    private static int calculateDiscountPrice(Integer price, DiscountType discountType, Integer discountValue) {
        if (discountType == DiscountType.RATE) {
            return price - (price * discountValue / 100);
        }
        return price - discountValue;
    }

    public void update(
        String title,
        Integer price,
        String discountType,
        Integer discountValue,
        Integer deliveryFee,
        Integer freeShippingConditions,
        Boolean isFresh,
        String productionStartTime,
        String deliveryCondition,
        String deliveryCompany
    ) {
        DiscountType parsedDiscountType = DiscountType.from(discountType);
        ProductionStartTime parsedProductionStartTime = ProductionStartTime.from(productionStartTime);
        validate(title, price, parsedDiscountType, discountValue, deliveryFee);

        this.title = title;
        this.price = price;
        this.discountType = parsedDiscountType;
        this.discountValue = discountValue;
        this.discountRate = calculateDiscountRate(price, parsedDiscountType, discountValue);
        this.discountPrice = calculateDiscountPrice(price, parsedDiscountType, discountValue);
        this.deliveryFee = deliveryFee;
        this.freeShippingConditions = freeShippingConditions;
        this.isFresh = isFresh;
        this.productionStartTime = parsedProductionStartTime;
        this.deliveryCondition = deliveryCondition;
        this.courier = deliveryCompany;
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
