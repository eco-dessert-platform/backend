package com.bbangle.bbangle.board.domain;

import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.store.domain.Store;
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
import org.hibernate.annotations.BatchSize;

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

    @Column(name = "status", columnDefinition = "tinyint")
    private boolean status;

    @Column(name = "profile")
    private String profile;

    @Column(name = "purchase_url")
    private String purchaseUrl;

    @Column(name = "view")
    private int view;

    @Column(name = "wish_cnt")
    private int wishCnt;

    @Column(name = "sunday", columnDefinition = "tinyint")
    private boolean sunday;

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

    @Column(name = "is_deleted", columnDefinition = "tinyint")
    private boolean isDeleted;

    @Builder.Default
    @OneToMany(mappedBy = "board")
    private List<Product> productList = new ArrayList<>();

    public void updateWishCnt(boolean status) {
        if (status) {
            this.wishCnt++;
        }
        if (!status) {
            this.wishCnt--;
        }
    }

    public Board updateProfile(String profile) {
        this.profile = profile;
        return this;
    }

}
