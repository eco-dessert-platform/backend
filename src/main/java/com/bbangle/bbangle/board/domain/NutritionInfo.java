package com.bbangle.bbangle.board.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "nutrition_info")
@Entity
public class NutritionInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    // 영양 성분 필드들
    @Column(name = "sugars")
    private int sugars; // 당

    @Column(name = "protein")
    private int protein; // 단백질

    @Column(name = "carbohydrates")
    private int carbohydrates; // 탄수화물

    @Column(name = "fat")
    private int fat; // 지방

    @Column(name = "weight")
    private int weight; // 중량 (g)

    @Column(name = "calories")
    private int calories; // 칼로리 (kcal)

    @Column(name = "serving_weight")
    private int servingWeight; // 1회 제공량 (g)
}
