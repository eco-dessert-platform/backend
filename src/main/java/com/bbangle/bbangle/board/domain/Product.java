package com.bbangle.bbangle.board.domain;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.push.domain.PushType;
import com.google.firebase.database.annotations.NotNull;
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
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "product")
@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_board_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Board board;

    @Column(name = "title", length = 50, columnDefinition = "varchar(50)", nullable = false)
    private String title;

    @Column(name = "price", nullable = false)
    private int price;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 20, nullable = false)
    private Category category;

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

    @Column(name = "stock")
    private int stock;

    /**
     * 유효성 검사
     */
    private void validate(String title,
                          boolean monday, boolean tuesday, boolean wednesday,
                          boolean thursday, boolean friday, boolean saturday, boolean sunday) {
        if (title.length() < 3 || title.length() > 50) {
            throw new BbangleException(BbangleErrorCode.INVALID_PRODUCT_NAME);
        }
        if (!monday && !tuesday && !wednesday && !thursday && !friday && !saturday && !sunday) {
            throw new BbangleException(BbangleErrorCode.INVALID_PRODUCT_DELIVERY_DAY);
        }
    }

    public Product(Board board, String title, int price, Category category,
                   boolean monday, boolean tuesday, boolean wednesday,
                   boolean thursday, boolean friday, boolean saturday, boolean sunday,
                   int stock) {

        validate(title, monday, tuesday, wednesday, thursday, friday, saturday, sunday);

        this.board = board;
        this.title = title;
        this.price = price;
        this.category = category;
        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
        this.saturday = saturday;
        this.sunday = sunday;
        this.stock = stock;
        this.soldout = false;
    }

    /**
     * 주문 타입 결정
     */
    public PushType getOrderType() {
        return Objects.nonNull(orderStartDate) ? PushType.DATE : PushType.WEEK;
    }
}
