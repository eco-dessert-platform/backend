package com.bbangle.bbangle.board.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Random;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardDetailProductDto {
    private Long id;
    private String title;
    private Boolean glutenFreeTag;
    private Boolean highProteinTag;
    private Boolean sugarFreeTag;
    private Boolean veganTag;
    private Boolean ketogenicTag;
    private NutrientDto nutrient;
    private String orderType;
    @JsonInclude(Include.NON_NULL)
    private OrderAvailableWeek orderAvailableWeek;
    @JsonInclude(Include.NON_NULL)
    private OrderAvailableDate orderAvailableDate;

    public static BoardDetailProductDto from(ProductDto productDto) {
        return BoardDetailProductDto.builder()
            .id(productDto.productId())
            .title(productDto.productTitle())
            .glutenFreeTag(productDto.glutenFreeTag())
            .highProteinTag(productDto.highProteinTag())
            .sugarFreeTag(productDto.sugarFreeTag())
            .veganTag(productDto.veganTag())
            .ketogenicTag(productDto.ketogenicTag())
            .build();
    }

    // fixed: 프론트 API 연동 테스트를 위한 값입니다. 해당 기능 구현 시 삭제됩니다.
    public BoardDetailProductDto toFixture() {
        Random random = new Random();

        if (random.nextBoolean()) {
            return BoardDetailProductDto.builder()
                .id(random.nextLong())
                .title("테스트 상품")
                .glutenFreeTag(random.nextBoolean())
                .highProteinTag(random.nextBoolean())
                .sugarFreeTag(random.nextBoolean())
                .veganTag(random.nextBoolean())
                .ketogenicTag(random.nextBoolean())
                .nutrient(NutrientDto.builder().build().toFixture())
                .orderType("WEEK")
                .orderAvailableWeek(OrderAvailableWeek.builder().build().toFixture())
                .build();
        } else {
            return BoardDetailProductDto.builder()
                .id(random.nextLong())
                .title("테스트 상품")
                .glutenFreeTag(random.nextBoolean())
                .highProteinTag(random.nextBoolean())
                .sugarFreeTag(random.nextBoolean())
                .veganTag(random.nextBoolean())
                .ketogenicTag(random.nextBoolean())
                .nutrient(NutrientDto.builder().build().toFixture())
                .orderType("DATE")
                .orderAvailableDate(OrderAvailableDate.builder().build().toFixture())
                .build();
        }
    }

}
