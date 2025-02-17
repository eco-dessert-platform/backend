package com.bbangle.bbangle.board.domain;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Embedded;
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
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "product")
@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter // board 에 product 세팅해서 저장해도 product 는 저장안되서 수기로 product 에서 board 세팅해줘야함...
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_board_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Board board;

    @Column(name = "title")
    private String title;

    @Column(name = "price")
    private int price;

    @Column(name = "category", columnDefinition = "varchar")
    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(name = "gluten_free_tag", columnDefinition = "tinyint")
    private boolean glutenFreeTag;

    @Column(name = "high_protein_tag", columnDefinition = "tinyint")
    private boolean highProteinTag;

    @Column(name = "sugar_free_tag", columnDefinition = "tinyint")
    private boolean sugarFreeTag;

    @Column(name = "vegan_tag", columnDefinition = "tinyint")
    private boolean veganTag;

    @Column(name = "ketogenic_tag", columnDefinition = "tinyint")
    private boolean ketogenicTag;

    @Column(name = "sugars")
    private Integer sugars;

    @Column(name = "protein")
    private Integer protein;

    @Column(name = "carbohydrates")
    private Integer carbohydrates;

    @Column(name = "fat")
    private Integer fat;

    @Column(name = "weight")
    private Integer weight;

    @Column(name = "calories")
    private Integer calories;

    @Column(name = "monday", columnDefinition = "tinyint")
    private boolean monday;

    @Column(name = "tuesday", columnDefinition = "tinyint")
    private boolean tuesday;

    @Column(name = "wednesday", columnDefinition = "tinyint")
    private boolean wednesday;

    @Column(name = "thursday", columnDefinition = "tinyint")
    private boolean thursday;

    @Column(name = "friday", columnDefinition = "tinyint")
    private boolean friday;

    @Column(name = "saturday", columnDefinition = "tinyint")
    private boolean saturday;

    @Column(name = "sunday", columnDefinition = "tinyint")
    private boolean sunday;

    @Column(name = "order_start_date")
    private LocalDateTime orderStartDate;

    @Column(name = "order_end_date")
    private LocalDateTime orderEndDate;

    @NotNull
    @Column(name = "is_soldout", columnDefinition = "tinyint")
    private boolean soldout;

    private int stock;

    @Embedded
    private Nutrition nutrition;

    public Product(Board board, String title, int price, Category category, int stock,
                   boolean glutenFreeTag, boolean highProteinTag, boolean sugarFreeTag, boolean veganTag,
                   boolean ketogenicTag, boolean monday, boolean tuesday, boolean wednesday,
                   boolean thursday, boolean friday, boolean saturday, boolean sunday,
                   Nutrition nutrition) {

        validate(title, monday, tuesday, wednesday, thursday, friday, saturday, sunday);

        this.board = board;
        this.title = title;
        this.price = price;
        this.category = category;
        this.stock = stock;
        this.glutenFreeTag = glutenFreeTag;
        this.highProteinTag = highProteinTag;
        this.sugarFreeTag = sugarFreeTag;
        this.veganTag = veganTag;
        this.ketogenicTag = ketogenicTag;
        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
        this.saturday = saturday;
        this.sunday = sunday;
        this.nutrition = nutrition;
        this.soldout = false;
    }

    private void validate(String title,
                          boolean monday, boolean tuesday, boolean wednesday,
                          boolean thursday, boolean friday, boolean saturday, boolean sunday) {
        if(title.length() < 3 || title.length() > 50) {
            throw new BbangleException(BbangleErrorCode.INVALID_PRODUCT_NAME);
        }
        if (!monday && !tuesday && !wednesday && !thursday && !friday && !saturday && !sunday) {
            throw new BbangleException(BbangleErrorCode.INVALID_PRODUCT_DELIVERY_DAY);
        }
    }
}
