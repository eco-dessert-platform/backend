package com.bbangle.bbangle.board.domain;

import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.review.domain.Review;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.wishlist.domain.WishListBoard;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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

  @OneToMany(mappedBy = "board")
  private List<Product> products;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
  private BoardStatistic boardStatistic;

  @OneToMany(mappedBy = "board")
  List<Review> reviews;

  public Board (Long boardId) { // 임시 조치
    this.id = boardId;
  }
  
  public Board updateProfile(String profile) {
    this.profile = profile;
    return this;
  }

  public List<String> getTags() {
    Set<String> tags = new HashSet<>();

    products.forEach(product -> tags.addAll(product.getStringTag()));

    return tags.stream().toList();
  }

  public boolean isSoldout() {
    return products.stream().allMatch(product -> Boolean.TRUE.equals(product.isSoldout()));
  }

  public boolean isNotification() {
    return products.stream().anyMatch(product -> Objects.nonNull(product.getOrderStartDate()));
  }

  public boolean isBundled() {
    return products.stream()
        .map(Product::getCategory)
        .distinct()
        .count() > 1;
  }

  public int getReviewCount() {
    return reviews.size();
  }

}
