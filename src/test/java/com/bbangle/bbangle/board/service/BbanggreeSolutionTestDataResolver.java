package com.bbangle.bbangle.board.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.SimilarityTypeEnum;
import com.bbangle.bbangle.board.service.domain.SolutionEnum;
import com.bbangle.bbangle.board.service.dto.SimilarityBoardTestDto;
import com.bbangle.bbangle.board.service.solution.Solution;
import com.bbangle.bbangle.board.service.solution.SolutionContext;
import com.bbangle.bbangle.board.service.solution.SolutionDataResolver;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class BbanggreeSolutionTestDataResolver extends AbstractIntegrationTest {

    @Autowired
    SolutionDataResolver solutionDataResolver;

    @Autowired
    Solution solution;

    @Test
    void reflectionTest() {
        List<SimilarityBoardTestDto> similarityBoardTestDtoList = List.of(
            new SimilarityBoardTestDto(
                1L,                     // boardId
                101L,                   // storeId
                "https://example.com/thumbnail.jpg", // thumbnail
                "Healthy Store",        // storeName
                "Organic Protein Bar",  // title
                20,                     // discountRate (20%)
                1500,                   // price (in cents or lowest unit of currency)
                new BigDecimal("4.5"),  // reviewRate (4.5 stars)
                250L,                   // reviewCount
                true,                   // glutenFreeTag
                true,                   // highProteinTag
                false,                  // sugarFreeTag
                true,                   // veganTag
                false,                  // ketogenicTag
                false,                  // isSoldOut
                Category.COOKIE,
                SimilarityTypeEnum.DEFAULT,
                true
            ),
            new SimilarityBoardTestDto(
                1L,                     // boardId
                101L,                   // storeId
                "https://example.com/thumbnail.jpg", // thumbnail
                "Healthy Store",        // storeName
                "Organic Protein Bar",  // title
                20,                     // discountRate (20%)
                1500,                   // price (in cents or lowest unit of currency)
                new BigDecimal("4.5"),  // reviewRate (4.5 stars)
                250L,                   // reviewCount
                true,                   // glutenFreeTag
                true,                   // highProteinTag
                false,                  // sugarFreeTag
                true,                   // veganTag
                false,                  // ketogenicTag
                false,                  // isSoldOut
                Category.COOKIE,
                // category (assuming Category is an enum with FOOD as a value)
                SimilarityTypeEnum.DEFAULT,
                // similarityType (assuming PRODUCT is a value in SimilarityTypeEnum)
                true
                // isWished (indicates if this item is wished or bookmarked by the user)
            ));

        Map<Long, List<SimilarityBoardTestDto>> inputData = new HashMap<>();
        inputData.put(1L, similarityBoardTestDtoList);

        List<SolutionEnum> solutionEnums = List.of(
            SolutionEnum.태그,
            SolutionEnum.묶음상품,
            SolutionEnum.품절여부
        );

        SolutionContext context = SolutionContext.builder()
            .solutionEnums(solutionEnums)
            .build();

        solutionDataResolver.resolver(
            context,
            inputData
        );

        solution.resolver(context);

        context.getOutputData().forEach(boardData ->
            boardData.forEach((boardId, enumMap) ->
                enumMap.forEach((solutionEnum, value) -> {
                    switch (solutionEnum) {
                        case 태그:
                            assertThat(List.of("glutenFree","highProtein","vegan")).isEqualTo(value);
                            break;
                        case 묶음상품:
                            assertThat(Boolean.FALSE).isEqualTo(value);
                            break;
                        case 품절여부:
                            assertThat(Boolean.FALSE).isEqualTo(value);
                            break;
                    }
                })
            )
        );
    }
}
