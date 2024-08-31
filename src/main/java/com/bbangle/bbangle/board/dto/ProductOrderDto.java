package com.bbangle.bbangle.board.dto;

import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.push.domain.Push;
import com.bbangle.bbangle.push.domain.PushType;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ProductOrderDto {

    private Long id;
    private String title;
    private Integer price;
    private Category category;
    private Boolean glutenFreeTag;
    private Boolean highProteinTag;
    private Boolean sugarFreeTag;
    private Boolean veganTag;
    private Boolean ketogenicTag;
    private Integer sugars;
    private Integer protein;
    private Integer carbohydrates;
    private Integer fat;
    private Integer weight;
    private Integer calories;
    private Boolean monday;
    private Boolean tuesday;
    private Boolean wednesday;
    private Boolean thursday;
    private Boolean friday;
    private Boolean saturday;
    private Boolean sunday;
    private LocalDateTime orderStartDate;
    private LocalDateTime orderEndDate;
    private Boolean soldout;
    private PushType pushType;
    private String days;
    private Boolean isActive;

    public static ProductOrderDto of(Product product) {
        return of(product, null);
    }

    public static ProductOrderDto of(Product product, Push push) {
        ProductOrderDtoBuilder productOrderDtoBuilder = ProductOrderDto.builder()
            .id(product.getId())
            .title(product.getTitle())
            .price(product.getPrice())
            .category(product.getCategory())
            .glutenFreeTag(product.isGlutenFreeTag())
            .highProteinTag(product.isHighProteinTag())
            .sugarFreeTag(product.isSugarFreeTag())
            .veganTag(product.isVeganTag())
            .ketogenicTag(product.isKetogenicTag())
            .sugars(product.getSugars())
            .protein(product.getProtein())
            .carbohydrates(product.getCarbohydrates())
            .fat(product.getFat())
            .weight(product.getWeight())
            .calories(product.getCalories())
            .monday(product.isMonday())
            .tuesday(product.isTuesday())
            .wednesday(product.isWednesday())
            .thursday(product.isThursday())
            .friday(product.isFriday())
            .saturday(product.isSaturday())
            .sunday(product.isSunday())
            .orderStartDate(product.getOrderStartDate())
            .orderEndDate(product.getOrderEndDate())
            .soldout(product.isSoldout());

        if (Objects.nonNull(push)) {
            productOrderDtoBuilder.pushType(push.getPushType())
                .days(push.getDays())
                .isActive(push.isActive());
        } else {
            productOrderDtoBuilder.pushType(null)
                .days("")
                .isActive(false);
        }

        return productOrderDtoBuilder.build();
    }
}
